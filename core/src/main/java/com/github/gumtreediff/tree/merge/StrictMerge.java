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

    class LockedNodes extends ArrayList<ITree> {}

    private ITree merge(ITree baseTree, ITree leftTree, ITree rightTree) throws ConflictException {
        ITree merged = createContainer(baseTree, leftTree, rightTree);

        // remove nodes missing in right from left
        // remove nodes missing in left from right

        // add start marker to left
        // add end marker to left
        // for each addition in left,

        // TODO this leads directly to merge list that Lindholm invented
        // so, like, just use that

       return merged;
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
