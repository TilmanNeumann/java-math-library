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
package de.tilman_neumann.jml.roots;

import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import de.tilman_neumann.test.junit.PackageTest;
import de.tilman_neumann.util.ReflectionUtil;

/**
 * All tests for the actual package.
 * @author Tilman Neumann
 */
public class PackageTests extends PackageTest {

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(PackageTests.class);
	private static final String TEST_PACKAGE = ReflectionUtil.getPackageName(PackageTests.class);
	
	/**
	 * @return All tests for the actual package
	 */
    public static TestSuite suite() {
        //LOG.info("Add Test package " + TEST_PACKAGE);
        TestSuite suite = new TestSuite("Test package " + TEST_PACKAGE);
        suite.addTest(new TestSuite(SqrtTest.class));
        // XXX: Add more tests here...

        return suite;
    }
}
