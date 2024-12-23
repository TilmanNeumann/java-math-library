/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.util;

import java.io.File;
import java.net.URL;
import java.util.Objects;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

/**
 * Global configuration tasks.
 * 
 * @author Tilman Neumann
 */
public class ConfigUtil {

	private static final Logger LOG = LogManager.getLogger(ConfigUtil.class);
	
	private static boolean initialized = false;
	
	/** File separator used on this OS. */
	public static String FILE_SEPARATOR;
	/** Path separator used on this OS. */
	public static String PATH_SEPARATOR;
	/** The root directory of the current project. */
	public static String PROJECT_ROOT;
	/** The base folder for all configuration files in this project. */
	public static String CONF_ROOT;
	/** Java class path */
	public static String JAVA_CLASS_PATH;
	/** Java library path */
	public static String JAVA_LIBRARY_PATH;
	/** Java temp directory */
	public static String JAVA_TMP_DIR;
	/** user home directory */
	public static String USER_HOME;

	/** number of processors to use for parallel implementations */
	public static int NUMBER_OF_PROCESSORS;
	
	private ConfigUtil() {
		// static class
	}

	/**
	 * Run project configuration being quiet about the initialization.
	 * @param verbose
	 */
	public static void initProject() {
		initProject(false);
	}

	/**
	 * Run project configuration, permitting to switch on verbose initialization.
	 * @param verbose
	 */
	public static void initProject(boolean verbose) {
		// avoid re-initialization from junit tests
		if (initialized) return;
		
		FILE_SEPARATOR = System.getProperty("file.separator");
		if (verbose) System.out.println("system file separator = " + FILE_SEPARATOR);
		PATH_SEPARATOR = System.getProperty("path.separator");
		if (verbose) System.out.println("system path separator = " + PATH_SEPARATOR);
		// user.dir is
		// * the jml project root directory if jml is run as a project
		// * the project root directory of a custom project using jml as a jar
		// * the folder containing the jar file if jml is run as a runnable jar
		// So this is a good place to put the data and configuration files required by jml
		PROJECT_ROOT = System.getProperty("user.dir");
		if (verbose) System.out.println("project root directory (user.dir) = " + PROJECT_ROOT);
		CONF_ROOT = PROJECT_ROOT + FILE_SEPARATOR + "conf";
		if (verbose) System.out.println("conf root directory = " + CONF_ROOT);
		JAVA_CLASS_PATH = System.getProperty("java.class.path");
		if (verbose) System.out.println("java.class.path = " + JAVA_CLASS_PATH);
		JAVA_LIBRARY_PATH = System.getProperty("java.library.path");
		if (verbose) System.out.println("java.library.path = " + JAVA_LIBRARY_PATH);
		JAVA_TMP_DIR = System.getProperty("java.io.tmpdir");
		if (verbose) System.out.println("java.io.tmpdir = " + JAVA_TMP_DIR);
		USER_HOME = System.getProperty("user.home");
		if (verbose) System.out.println("user.home = " + USER_HOME);
		NUMBER_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();
		if (verbose) System.out.println("number of processors = " + NUMBER_OF_PROCESSORS);
		
		String confFileStr = CONF_ROOT + FILE_SEPARATOR + "log4j2-test.xml";
		File confFile = new File(confFileStr);
		if (confFile.exists()) {
			// initialize Log4j from xml configuration
	    	//DOMConfigurator.configure(confFileStr);
			System.setProperty("log4j.configurationFile", confFileStr);
			Configurator.reconfigure(); // TODO do we need this?
	    	if (verbose) LOG.info("log4j configuration successfully loaded from file " + confFileStr + ".");
	    	if (verbose) LOG.info("project initialization finished.");
	    	initialized = true;
	    	return;
		}
		
		// Not finding the log4j config file would be bad when not run from runnable jar
		boolean runningFromJar = Objects.equals(getClassResourceProtocol(verbose), "rsrc");
		if (!runningFromJar) {
			System.err.println("WARNING: Unable to find log4j configuration file " + confFileStr + ".");
			System.err.println("WARNING: An emergency logger with limited capabilities will be used.");
			System.err.println("WARNING: Please put a proper log4jconf.xml file into the designated folder if this message is annoying you...");
		}
		
		// Create default logger, following https://www.baeldung.com/log4j2-programmatic-config
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		// create appenders to console and log.txt
		AppenderComponentBuilder console = builder.newAppender("stdout", "Console"); 
		builder.add(console);
		AppenderComponentBuilder file = builder.newAppender("log", "File"); 
		file.addAttribute("fileName", "log.txt");
		builder.add(file);
		// declare layouts
		LayoutComponentBuilder standard = builder.newLayout("PatternLayout");
		standard.addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable");
		console.add(standard);
		file.add(standard);
		// set up root logger with debug level
		RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG);
		rootLogger.add(builder.newAppenderRef("stdout"));
		builder.add(rootLogger);
		// let it be used
		Configurator.initialize(builder.build());
		LOG.info("Created default logger:\n" + builder.toXmlConfiguration());
    	initialized = true;
	}
	
	// derived from https://stackoverflow.com/questions/482560/can-you-tell-on-runtime-if-youre-running-java-from-within-a-jar
	private static String getClassResourceProtocol(boolean verbose) {
		//URL classResource = ConfigUtil.class.getResource(ConfigUtil.class.getName() + ".class"); // may be null
		URL classResource = ConfigUtil.class.getResource("");
		if (verbose) System.out.println("classResource = " + classResource);
		String protocol = classResource.getProtocol();
		if (verbose) System.out.println("protocol = " + protocol);
		return protocol;
	}
}
