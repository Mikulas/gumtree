package tdm.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RootSuite extends TestCase {

    public RootSuite() {
        super("3dm full test suite");
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(MergeTest.class);
        suite.addTestSuite(DiffPatch.class);
        return suite;
    }

}
