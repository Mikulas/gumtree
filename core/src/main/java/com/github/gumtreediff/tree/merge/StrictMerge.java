package com.github.gumtreediff.tree.merge;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.MergeMapping;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.utils.Pair;

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

    private ITree merge(ITree baseTree, ITree leftTree, ITree rightTree) throws ConflictException {
        ITree merged = createContainer(baseTree, leftTree, rightTree);

        // Core of the algorithm. Apply right changes onto
        // left changes and then vice versa. If the results
        // are equal, there is no collision. Otherwise throw.
        List<ITree> leftThenRight = applyChanges(baseTree, leftTree, rightTree, new Mappings(
            mappings.getBaseToLeft(), mappings.getBaseToRight(), mappings.getLeftToRight()
        ));
        List<ITree> rightThenLeft = applyChanges(baseTree, rightTree, leftTree, new Mappings(
            mappings.getBaseToRight(), mappings.getBaseToLeft(), mappings.getRightToLeft()
        ));

        if (! leftThenRight.equals(rightThenLeft)) {
            throw new ConflictException();
        }

        for (ITree child : leftThenRight) {
            merged.addChild(child);
            // TODO recurse
        }
        return merged;
    }

    class Mappings {
        public final MappingStore baseToFirst;
        public final MappingStore baseToSecond;
        public final MappingStore firstToSecond;

        Mappings(MappingStore baseToFirst, MappingStore baseToSecond, MappingStore firstToSecond) {

            this.baseToFirst = baseToFirst;
            this.baseToSecond = baseToSecond;
            this.firstToSecond = firstToSecond;
        }
    }

    private Map<Integer, List<ITree>> applyChanges(ITree baseTree, ITree first, ITree second, Mappings mappings) {
        List<ITree> baseChildren = baseTree.getChildren();


        // find nodes deleted in $first
        List<ITree> firstChildrenInBase = new ArrayList<>();
        for (ITree child : first.getChildren()) {
            ITree childInBase = mappings.baseToFirst.getSrc(child);
            if (childInBase != null) {
                firstChildrenInBase.add(childInBase);
            }
        }

        Set<ITree> deleted = new HashSet<>(); // of base nodes
        for (ITree child : baseChildren) {
            // Node is deleted if it exists in base but not in branch
            if (! firstChildrenInBase.contains(child)) {
                // no child in $first mapped to this base node
                deleted.add(child);
            }
        }


        // find nodes deleted in $second
        List<ITree> secondChildrenInBase = new ArrayList<>();
        for (ITree child : second.getChildren()) {
            ITree childInBase = mappings.baseToSecond.getSrc(child);
            if (childInBase != null) {
                secondChildrenInBase.add(childInBase);
            }
        }

        for (ITree child : baseChildren) {
            // Node is deleted if it exists in base but not in branch
            if (! secondChildrenInBase.contains(child)) {
                // no child in $first mapped to this base node
                deleted.add(child);
            }
        }


        // Left is fully applied just by setting it as new state.
        // If this was a 2-way merge, we would just take $first.

        Map<Integer, List<ITree>> result = new HashMap<>();
        Integer index = 0;
        for (ITree child : first.getChildren()) {
            // Do not add nodes deleted in $second
            ITree childInBase = mappings.baseToFirst.getSrc(child);
            if (childInBase != null && deleted.contains(childInBase)) {
                continue;
            }

            List<ITree> list = new ArrayList<>();
            list.add(child);
            result.put(index, list);
            index++;
        }

        index = 0;
        for (ITree child : second.getChildren()) {
            // Do not add nodes deleted in $second
            ITree childInBase = mappings.baseToSecond.getSrc(child);
            if (childInBase != null && deleted.contains(childInBase)) {
                continue;
            }

            // TODO only add inserts

            List<ITree> list = new ArrayList<>();
            list.add(child);
            result.put(index, list);
            index++;
        }

// TODO tohle je potreba prepsat. Je nutny trackovat jake nody se posunuly (bez vlivu insertu).
// Pokud obe strany node posunout na jinou pozici, je to kolize. Pokud jedna strana neposune,
// vem zmenu.

        return result;
    }

    private ITree createContainer(ITree baseTree, ITree leftTree, ITree rightTree) throws ConflictException {
        if (leftTree.getType() == rightTree.getType()) {
            // Nothing changed or both changed from base to same value
            return new Tree(leftTree.getType(), leftTree.getLabel());
            // TODO maybe throw if base left and right have different labels

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
