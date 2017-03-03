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

package com.github.gumtreediff.client.merge;

import com.github.gumtreediff.client.Option;
import com.github.gumtreediff.client.Client;
import com.github.gumtreediff.client.Register;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.TreeContext;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

@Register(name = "merge", description = "Performs 3-way merge",
        options = MergeClient.Options.class)
public class MergeClient<O extends MergeClient.Options> extends Client {

    protected O opts;
    public static final String SYNTAX = "Syntax: merge [options] baseFile aFile bFile";
    private TreeContext baseTree;
    private TreeContext aTree;
    private TreeContext bTree;

    public static class Options implements Option.Context {
        public String matcher;
        public ArrayList<String> generators = new ArrayList<>();
        public String baseFile;
        public String aFile;
        public String bFile;

        @Override
        public Option[] values() {
            return new Option[] {
                    new Option("-m", "The qualified name of the class implementing the matcher.", 1) {
                        @Override
                        protected void process(String name, String[] args) {
                            matcher = args[0];
                        }
                    },
                    new Option("-g", "Preferred generator to use (can be used more than once).", 1) {
                        @Override
                        protected void process(String name, String[] args) {
                            generators.add(args[0]);
                        }
                    },
                    new Option.Help(this) {
                        @Override
                        public void process(String name, String[] args) {
                            System.out.println(SYNTAX);
                            super.process(name, args);
                        }
                    }
            };
        }

        void dump(PrintStream out) {
            out.printf("Current path: %s\n", System.getProperty("user.dir"));
            out.printf("Merge: %s %s %s\n", baseFile, aFile, bFile);
        }
    }

    public MergeClient(String[] args) {
        super(args);
        Options opts = new Options();
        args = Option.processCommandLine(args, opts);

        if (args.length < 2)
            throw new Option.OptionException("arguments required." + SYNTAX, opts);

        opts.baseFile = args[0];
        opts.aFile = args[1];
        opts.bFile = args[2];

        if (Option.Verbose.verbose) {
            opts.dump(System.out);
        }
    }

    private Matcher matcher;


    @Override
    public void run() throws Exception {
        System.out.println("now I would merge");
    }

//    protected Matcher matchTrees() {
//        Matchers matchers = Matchers.getInstance();
//        if (matcher != null)
//            return matcher;
//        matcher = (opts.matcher == null)
//                ? matchers.getMatcher(getSrcTreeContext().getRoot(), getDstTreeContext().getRoot())
//                : matchers.getMatcher(opts.matcher, getSrcTreeContext().getRoot(), getDstTreeContext().getRoot());
//        matcher.match();
//        return matcher;
//    }

    protected TreeContext getBaseTreeContext() {
        if (baseTree == null)
            baseTree = getTreeContext(opts.baseFile);
        return baseTree;
    }

    protected TreeContext getATreeContext() {
        if (aTree == null)
            aTree = getTreeContext(opts.aFile);
        return aTree;
    }

    protected TreeContext getBTreeContext() {
        if (bTree == null)
            bTree = getTreeContext(opts.bFile);
        return bTree;
    }

    private TreeContext getTreeContext(String file) {
        try {
            TreeContext t;
            if (opts.generators.isEmpty())
                t = Generators.getInstance().getTree(file);
            else
                t = Generators.getInstance().getTree(opts.generators.get(0), file);
            return t;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
