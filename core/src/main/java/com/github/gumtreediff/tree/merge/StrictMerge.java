package com.github.gumtreediff.tree.merge;

import com.github.gumtreediff.actions.LeavesClassifier;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.MergeMapping;
import com.github.gumtreediff.tree.AbstractTree;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;

import java.util.*;

/**
 * Created by mikulas on 09/03/2017.
 */
public class StrictMerge {

    private ITree baseTree;
    private ITree leftTree;
    private ITree rightTree;

    private MergeMapping mappings;

    public StrictMerge(ITree base, ITree left, ITree right, MergeMapping mappings) {
        this.baseTree = base;
        this.leftTree = left;
        this.rightTree = right;
        this.mappings = mappings;
    }

    public ITree merge() throws ConflictException {
        return merge(baseTree, leftTree, rightTree);
    }

    private class MergeListEntry {
        boolean lockedWithRight = false;
    }

    private class StartMarker extends MergeListEntry {}

    private class MergeListEntryTree extends MergeListEntry {
        ITree node;

        MergeListEntryTree(ITree node) {
            this.node = node;
        }
    }

    private ITree merge(ITree baseTree, ITree leftTree, ITree rightTree) throws ConflictException {
        ITree merged = createContainer(baseTree, leftTree, rightTree);

        Set<ITree> deleted = new HashSet<>(); // nodes in base
        deleted.addAll(getDeleted(baseTree, leftTree, mappings.getBaseToLeft()));
        deleted.addAll(getDeleted(baseTree, rightTree, mappings.getBaseToRight()));

        List<MergeListEntry> leftList = makeMergeList(baseTree, leftTree, mappings.getBaseToLeft(), deleted);
        List<MergeListEntry> rightList = makeMergeList(baseTree, rightTree, mappings.getBaseToRight(), deleted);

        Iterator<MergeListEntry> leftPtr = leftList.iterator();
        Iterator<MergeListEntry> rightPtr = rightList.iterator();

        // process starting node locks
        MergeListEntry leftNode = leftPtr.next();
        MergeListEntry rightNode = rightPtr.next();
        boolean lastLeftIsLocked = leftNode.lockedWithRight;
        boolean lastRightIsLocked = rightNode.lockedWithRight;

        // move away from starting node
        leftNode = leftPtr.hasNext() ? leftPtr.next() : null;
        rightNode = rightPtr.hasNext() ? rightPtr.next() : null;

        while (leftNode != null && rightNode != null) {
            assert leftNode instanceof MergeListEntryTree;
            assert rightNode instanceof MergeListEntryTree;

            ITree left = leftNode == null ? new AbstractTree.FakeTree() : ((MergeListEntryTree) leftNode).node;
            ITree right = rightNode == null ? new AbstractTree.FakeTree() : ((MergeListEntryTree) rightNode).node;

            if (lastLeftIsLocked && lastRightIsLocked || mappedNodes(left, right)) {
                // Locks require the nodes to be mapped, or the nodes are mapped.
                // Either way we merge those two sides into one node and advance both pointers.

                if (!mappedNodes(left, right)) {
                    throw new ConflictException();
                }

                ITree parent = getCommonBaseForParentOrFakeIt(left, right);
                merged.addChild(merge(parent, left, right));

                // advance both
                lastLeftIsLocked = leftNode.lockedWithRight;
                leftNode = leftPtr.hasNext() ? leftPtr.next() : null;
                lastRightIsLocked = rightNode.lockedWithRight;
                rightNode = rightPtr.hasNext() ? rightPtr.next() : null;

            } else if (lastLeftIsLocked) {
                assert !mappedNodes(left, right);

                // add only left node
                merged.addChild(convertToSideAware(left, Side.LEFT));

                // advance left
                lastLeftIsLocked = leftNode.lockedWithRight;
                leftNode = leftPtr.hasNext() ? leftPtr.next() : null;

            } else if (lastRightIsLocked) {
                assert !mappedNodes(left, right);

                // add only right node
                merged.addChild(convertToSideAware(right, Side.RIGHT));

                // advance right
                lastRightIsLocked = rightNode.lockedWithRight;
                rightNode = rightPtr.hasNext() ? rightPtr.next() : null;

            } else {
                // neither locked
                // insertions and moves both lock, so next nodes should be same?
                assert mappedNodes(left, right);

                // add node from both sides
                ITree parent = getCommonBaseForParentOrFakeIt(left, right);
                merged.addChild(merge(parent, left, right));

                // advance both
                lastLeftIsLocked = leftNode.lockedWithRight;
                leftNode = leftPtr.hasNext() ? leftPtr.next() : null;
                lastRightIsLocked = rightNode.lockedWithRight;
                rightNode = rightPtr.hasNext() ? rightPtr.next() : null;
            }
        }

        return merged;
    }

    private ITree convertToSideAware(ITree tree, Side side) {
        ITree converted = new SideAwareTree(tree, side);
        for (ITree child : tree.getChildren()) {
            converted.addChild(convertToSideAware(child, side));
        }
        return converted;
    }

    private ITree getCommonBaseForParentOrFakeIt(ITree left, ITree right) {
        ITree parent = mappings.getBaseToLeft().getSrc(left);
        if (parent == null) {
            // this is unlikely, if left and right are mapped, parent will be mapped on both sides
            parent = mappings.getBaseToRight().getSrc(right);
        }
        if (parent == null) {
            parent = new AbstractTree.FakeTree();
        }
        return parent;
    }

    private boolean mappedNodes(ITree leftNode, ITree rightNode) {
        if (leftNode == null || rightNode == null) {
            return false;
        }
        return mappings.getLeftToRight().getDst(leftNode) == rightNode;
    }

    private Set<ITree> getDeleted(ITree baseTree, ITree sideTree, MappingStore mapping) {
        // Base nodes that are missing in side tree
        Set<ITree> deleted = new HashSet<>();
        for (ITree child : baseTree.getChildren()) {
            ITree childInSide = mapping.getDst(child);
            if (childInSide == null || !sideTree.getChildren().contains(childInSide)) {
                deleted.add(child);
            }
        }
        return deleted;
    }

    private List<MergeListEntry> makeMergeList(ITree baseTree, ITree sideTree, MappingStore mapping, Set<ITree> deletedBaseNodes) {
        List<MergeListEntry> mergelist = new ArrayList<>();
        MergeListEntry startMarker = new StartMarker();
        mergelist.add(startMarker);

        List<ITree> movedNodes = getMovedNodes(baseTree, sideTree, mapping, deletedBaseNodes);

        for (ITree child : sideTree.getChildren()) {
            ITree childInBase = mapping.getSrc(child);
            if (childInBase != null && deletedBaseNodes.contains(childInBase)) {
                // node deleted in other side
                continue;
            }

            MergeListEntry last = mergelist.get(mergelist.size() - 1);
            MergeListEntry current = new MergeListEntryTree(child);
            mergelist.add(current);

            if (childInBase == null || !baseTree.getChildren().contains(childInBase)) {
                // node inserted, lock both sides of it
                last.lockedWithRight = true;
                current.lockedWithRight = true;
            }
            if (movedNodes.contains(childInBase)) {
                last.lockedWithRight = true;
                current.lockedWithRight = true;
            }
            // TODO is this all?
        }

        return mergelist;
    }

    class Positions {
        Integer base;
        Integer side;
        public Positions(Integer base, Integer side) {
            this.base = base;
            this.side = side;
        }
    }

    private List<ITree> getMovedNodes(ITree baseTree, ITree sideTree, MappingStore mapping, Set<ITree> deletedBaseNodes) {
        Map<ITree, Positions> positions = getPositions(baseTree, sideTree, mapping, deletedBaseNodes);

        List<ITree> moved = new ArrayList<>();
        for (Map.Entry<ITree, Positions> entry : positions.entrySet()) {
            if (entry.getValue().side < entry.getValue().base) {
                moved.add(entry.getKey());
            }
        }
        return moved;
    }

    private Map<ITree, Positions> getPositions(ITree baseTree, ITree sideTree, MappingStore mapping, Set<ITree> deletedBaseNodes) {
        List<ITree> baseList = new ArrayList<>();
        for (ITree child : baseTree.getChildren()) {
            if (deletedBaseNodes.contains(child)) {
                continue;
            }
            baseList.add(child);
        }

        List<ITree> sideList = new ArrayList<>();
        for (ITree child : sideTree.getChildren()) {
            ITree childInBase = mapping.getSrc(child);
            if (childInBase == null || deletedBaseNodes.contains(childInBase)) {
                continue;
            }
            if (!baseTree.getChildren().contains(childInBase)) {
                // node mapped, but not to this baseTree as parent
                continue;
            }
            sideList.add(childInBase);
        }

        assert baseList.size() == sideList.size();


        Map<ITree, Positions> positions = new HashMap<>();

        int position = 0;
        for (ITree child : baseList) {
            positions.put(child, new Positions(
                position, sideList.indexOf(child)
            ));
        }

        return positions;
    }

    public enum Side {
        BASE, LEFT, RIGHT
    };

    public class SideAwareTree extends Tree {
        private Side side;

        public SideAwareTree(int type, String label, Side side) {
            super(type, label);
            this.side = side;
        }

        public SideAwareTree(ITree tree, Side side) {
            super(tree.getType(), tree.getLabel());
            setPos(tree.getPos());
            setLength(tree.getLength());
            this.side = side;
        }

        public Side getSide() {
            return side;
        }
    }

    private ITree createContainer(ITree baseTree, ITree leftTree, ITree rightTree) throws ConflictException {
        int type;
        String label;
        Side side;

        // This works correctly even with FakeTrees during insert

        if (leftTree.getType() == rightTree.getType()) {
            // Nothing changed or both changed from base to same value
            type = leftTree.getType();
            if (leftTree.getLabel().equals(rightTree.getLabel())) {
                // Nothing changed or both changed from base to same value
                label = leftTree.getLabel();
                side = Side.LEFT;

            } else if (baseTree.getLabel().equals(leftTree.getLabel())) {
                // Left did not change, use right value, which is different
                label = rightTree.getLabel();
                side = Side.RIGHT;

            } else if (baseTree.getLabel().equals(rightTree.getLabel())) {
                // Right did not change, use left value, which is different
                label = leftTree.getLabel();
                side = Side.LEFT;

            } else {
                // all 3 changed, we cannot pick one
                throw new ConflictException(); // TODO text
            }

        } else if (baseTree.getType() == leftTree.getType()) {
            // Left did not change, use right value, which is different
            type = rightTree.getType();
            label = rightTree.getLabel();
            side = Side.RIGHT;

        } else if (baseTree.getType() == rightTree.getType()) {
            // Right did not change, use left value, which is different
            type = leftTree.getType();
            label = leftTree.getLabel();
            side = Side.LEFT;

        } else {
            // all 3 changed, we cannot pick one
            throw new ConflictException(); // TODO text
        }

        ITree tree = new SideAwareTree(type, label, side);
        tree.setLength(side == Side.LEFT ? leftTree.getLength() : rightTree.getLength());
        tree.setPos(side == Side.LEFT ? leftTree.getPos() : rightTree.getPos());
        return tree;
    }


    class ConflictException extends Exception {}

}
