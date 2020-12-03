/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann (www.tilman-neumann.de)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */
package de.tilman_neumann.test.junit;

import org.apache.log4j.Logger;

import junit.framework.TestSuite;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Collection of all unit tests in the project.
 *
 * @author Tilman Neumann
 */
public class ProjectTest extends TestSuite {
	
	static {
		// early initializer, works no matter if run as junit test or as java application
		ConfigUtil.initProject();
	}

	private static final Logger LOG = Logger.getLogger(ProjectTest.class);

    /**
     * Collects the tests from all packages in the project.
     * @return TestSuite
     */
	public static TestSuite suite() {
		// create project test suite
    	LOG.info("Start Project JUnit tests...");
		TestSuite suite = new TestSuite("Project JUnit tests");
        suite.addTest(de.tilman_neumann.jml.precision.PackageTests.suite());
        suite.addTest(de.tilman_neumann.jml.roots.PackageTests.suite());
        suite.addTest(de.tilman_neumann.jml.transcendental.PackageTests.suite());
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