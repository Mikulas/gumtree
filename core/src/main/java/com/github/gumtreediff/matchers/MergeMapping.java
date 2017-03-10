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

        baseToLeft = getMappings(matchers, base, left);
        baseToRight = getMappings(matchers, base, right);
        leftToRight = getMappings(matchers, left, right);
        rightToLeft = leftToRight.invert();
    }

    private MappingStore getMappings(Matchers matchers, ITree a, ITree b) {
        Matcher matcher = matchers.getMatcher(a, b);
        matcher.match();
        return matcher.getMappings();
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
