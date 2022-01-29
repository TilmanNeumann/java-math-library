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

import de.tilman_neumann.util.ConfigUtil;

import junit.framework.TestCase;
import junit.framework.TestResult;

// TODO get rid of Junit3-style classes junit.framework.* like TestCase
public class ClassTest extends TestCase {
	
	static {
		// early initializer, works no matter if run as junit test or as java application
		ConfigUtil.initProject();
	}
	
	private static final Logger LOG = Logger.getLogger(ClassTest.class);
	
	// Constructor is called for each test method in the test class!
	public ClassTest() {
		//LOG.info("create test for " + this.getClass());
	}
	
	public void run(TestResult result) {
		LOG.info("Run " + this.getClass().getName() + "." + this.getName() + "()");
		super.run(result);
	}
}
