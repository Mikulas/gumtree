package com.github.gumtreediff.client;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.merge.StrictMerge;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class ResultPrinter {
    private PrintStream out;
    private Map<StrictMerge.Side, Integer> positions = new HashMap<>();

    public ResultPrinter(PrintStream out) {
        this.out = out;
        positions.put(StrictMerge.Side.LEFT, 0);
        positions.put(StrictMerge.Side.RIGHT, 0);
    }


    public void outputMergedCode(StrictMerge.SideAwareTree mergedTree, String leftSource, String rightSource) {
        String source = mergedTree.getSide() == StrictMerge.Side.LEFT ? leftSource : rightSource;

        if (mergedTree.getChildren().size() == 0) {
            out.print(source.substring(mergedTree.getPreviousTokenEnd(), mergedTree.getEndPos()));
            return;
        }

        // print all tokens between side and token start

        // update positions to what?!

//        ITree firstChild = mergedTree.getChild(0);
//        assert firstChild instanceof StrictMerge.SideAwareTree;
//        int start = firstChild
//        int end = firstChild.getPos();
//        if (start >= 0 && end >= 0) {
//            out.print(source.substring(start, end));
//        }

        for (ITree child : mergedTree.getChildren()) {
            assert child instanceof StrictMerge.SideAwareTree;
            outputMergedCode((StrictMerge.SideAwareTree) child, leftSource, rightSource);
        }

//        ITree lastChild = mergedTree.getChild(mergedTree.getChildren().size() - 1);
//        start = lastChild.getEndPos();
//        end = mergedTree.getEndPos();
//        if (start >= 0 && end >= 0) {
//            out.print(source.substring(start, end));
//        }
    }
}
