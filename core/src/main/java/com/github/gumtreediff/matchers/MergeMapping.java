package com.github.gumtreediff.matchers;

import com.github.gumtreediff.tree.ITree;

/**
 * Created by mikulas on 09/03/2017.
 */
public class MergeMapping {

    private final MappingStore baseToLeft;
    private final MappingStore baseToRight;
    private final MappingStore leftToRight;
    private final MappingStore rightToLeft;

    public MergeMapping(ITree base, ITree left, ITree right) {
        Matchers matchers = Matchers.getInstance();

        baseToLeft = matchers.getMatcher(base, left).getMappings();
        baseToRight = matchers.getMatcher(base, right).getMappings();
        leftToRight = matchers.getMatcher(left, right).getMappings();
        rightToLeft = leftToRight.invert();
    }

    public MappingStore getBaseToLeft() {
        return baseToLeft;
    }

    public MappingStore getBaseToRight() {
        return baseToRight;
    }

    public MappingStore getLeftToRight() {
        return leftToRight;
    }

    public MappingStore getRightToLeft() {
        return rightToLeft;
    }
}
