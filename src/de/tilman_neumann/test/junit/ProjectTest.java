package de.tilman_neumann.test.junit;

import org.apache.log4j.Logger;

import junit.framework.TestSuite;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Collection of all unit tests in the project.
 *
 * @author Tilman Neumann
 * @since 2011-08-23
 */
public class ProjectTest extends TestSuite {
	
	static {
		// early initializer, works no matter if run as junit test or as java application
		ConfigUtil.initProject();
	}

	private static final Logger LOG = Logger.getLogger(ProjectTest.class);

    /**
     * Collects the tests from all packages in the project.
     */
	public static TestSuite suite() {
		// create project test suite
    	LOG.info("Start Project JUnit tests...");
		TestSuite suite = new TestSuite("Project JUnit tests");
        suite.addTest(de.tilman_neumann.jml.precision.PackageTests.suite());
        // XXX: Add more packages here
        
		return suite;
	}
	
    /**
     * Main junit test application.
     * @param args ignored
     */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
    	LOG.info("Finished Project JUnit tests...");
	}
}