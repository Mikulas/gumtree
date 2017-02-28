/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com>
 */

package com.github.gumtreediff.gen.antlr4;

import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public abstract class AbstractAntlr4TreeGenerator extends TreeGenerator {

    private Deque<ITree> trees = new ArrayDeque<ITree>();

    protected static Map<Integer, Integer> chars;

    protected CommonTokenStream tokens;

    public AbstractAntlr4TreeGenerator() {
    }

    protected abstract ParseTree getTree(Reader r) throws RecognitionException, IOException;

    @Override
    public TreeContext generate(Reader r) throws IOException {
        ParseTree tree = getTree(r);

        TreeContext context = new TreeContext();
        ITree root = context.createTree(1, "label", "typeLabel");
        context.setRoot(root);

        try {
            buildTree(context, root, tree);

        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return context;
    }

    protected abstract String[] getTokenNames();

    protected String getTokenName(int tokenType) {
        String[] names = getTokenNames();
        if (tokenType < 0 || tokenType >= names.length)
            return ITree.NO_LABEL;
        return names[tokenType];
    }

    @SuppressWarnings("unchecked")
    abstract protected void buildTree(TreeContext context, ITree root, ParseTree ct);

    private int startPos(int tkPosition) {
        if (tkPosition == -1) return 0;
        Token tk = tokens.get(tkPosition);
        if (tk instanceof CommonToken)
            return ((CommonToken)tk).getStartIndex();
        return 0;
    }

    private int stopPos(int tkPosition) {
        if (tkPosition == -1) return 0;
        Token tk = tokens.get(tkPosition);
        if (tk instanceof CommonToken)
            return ((CommonToken)tk).getStopIndex();
        return 0;
    }
}
