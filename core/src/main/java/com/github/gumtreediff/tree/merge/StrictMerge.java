package com.github.gumtreediff.tree.merge;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.MergeMapping;
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

        // remove nodes missing in right from left
        List<MergeListEntry> leftList = makeMergeList(baseTree, leftTree, mappings.getBaseToLeft(), deleted);
        // remove nodes missing in left from right

       return merged;
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
        MergeListEntry startMarker = new MergeListEntry();
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

            if (childInBase == null) { // TODO or if no in baseTree.children !
                // node inserted, lock both sides of it
                last.lockedWithRight = true;
                current.lockedWithRight = true;
            }
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

    private ITree createContainer(ITree baseTree, ITree leftTree, ITree rightTree) throws ConflictException {
        if (leftTree.getType() == rightTree.getType()) {
            // Nothing changed or both changed from base to same value
            return new Tree(leftTree.getType(), leftTree.getLabel());
            // TODO throw if base left and right have different labels

        } else if (baseTree.getType() == leftTree.getType()) {
            // Left did not change, use right value, which is different
            return new Tree(rightTree.getType(), rightTree.getLabel());

        } else if (baseTree.getType() == rightTree.getType()) {
            // Right did not change, use left value, which is different
            return new Tree(leftTree.getType(), leftTree.getLabel());

        } else {
            // all 3 changed, we cannot pick one
            throw new ConflictException(); // TODO text
        }
    }

    class ConflictException extends Exception {}

}
