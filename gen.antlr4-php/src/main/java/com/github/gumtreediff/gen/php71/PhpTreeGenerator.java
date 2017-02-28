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

package com.github.gumtreediff.gen.php71;

import com.github.gumtreediff.gen.Register;
import com.github.gumtreediff.gen.antlr4.AbstractAntlr4TreeGenerator;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.io.Reader;

@Register(id = "php-antlr", accept = "\\.php.?$")
public class PhpTreeGenerator extends AbstractAntlr4TreeGenerator {

    @Override
    protected ParseTree getTree(Reader r) throws RecognitionException, IOException {
        System.out.println("MY PARSER getParser");

        ANTLRInputStream stream = new ANTLRInputStream(r);
        PHPLexer l = new PHPLexer(stream);

        CommonTokenStream tokens = new CommonTokenStream(l);
        PHPParser p = new PHPParser(tokens);
        p.setBuildParseTree(true);

        return p.topStatement();
    }

    @Override
    protected final String[] getTokenNames() {
        System.out.println("MY PARSER getTokenNames");
        return PHPParser.tokenNames;
    }

    @Override
    protected void buildTree(TreeContext context, ITree root, ParseTree ct, int _depth) {
        ITree tree;
        if (ct instanceof PHPParser.FunctionDeclarationContext) {
            tree = context.createTree(3, "attributes", "attributes");
            ((PHPParser.FunctionDeclarationContext) ct).attributes();

        } else {
            tree = context.createTree(2, ct.getText(), "childType");
            for (int i = 0; i < _depth; i++) {
                System.out.print("  ");
            }
            System.out.print(ct.getClass());
            System.out.print("  ");
            System.out.println(ct.getText());
        }

        tree.setParentAndUpdateChildren(root);

        int childrenCount = ct.getChildCount();
        for (int childIndex = 0; childIndex < childrenCount; childIndex++) {
            ParseTree cct = ct.getChild(childIndex);

            buildTree(context, tree, cct, _depth + 1);
        }
    }
}
