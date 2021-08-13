/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2021 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.mcuosmipcuter.orcc.soundvis.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Supplier;

import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * @author Michael Heinzelmann
 *
 */
public class FileConfiguration {

	public static final String SOUNDVIS_PROPERTY_APP_DIR = "appDir";
	public static final String SOUNDVIS_PROPERTY_ASK_APP_DIR_ON_STARTUP = "askAppDirOnStartup";
	public static final String SOUNDVIS_PROPERTY_LOOK_AND_FEEL = "lookAndFeel";
	
	public static final String SOUNDVIS_PROPERTIES_FILE_NAME = "soundvis.properties";
	public static final String SOUNDVIS_CONFIG_DIR_NAME = "soundvis";
	
	// system settings
	private static final String userHomeDir = System.getProperty("user.home");
	private static final String sep = System.getProperty("file.separator");
	private static final String  tempDir = System.getProperty("java.io.tmpdir");
	private static final String TARGET_CONF_DIR = userHomeDir + sep + ".config";
	private static final String SOUNDVIS_CONF_DIR = TARGET_CONF_DIR + sep + SOUNDVIS_CONFIG_DIR_NAME;
	private static final String SOUNDVIS_CONF_FILE = SOUNDVIS_CONF_DIR + sep + SOUNDVIS_PROPERTIES_FILE_NAME;
	
	private static String bootDir;
	private static String appDir;

	/**
	 * 
	 */
	public static void init() {

		// current directory
		bootDir = new File("").getAbsolutePath();

		File targetConfDir = new File(TARGET_CONF_DIR);
		if(!targetConfDir.exists()) {
			targetConfDir.mkdir();
		}

		Properties cp = getProperties();

		if (!cp.isEmpty()) {
			boolean ask = "true".equals(cp.getProperty(SOUNDVIS_PROPERTY_ASK_APP_DIR_ON_STARTUP));
			if (!ask) {
				appDir = cp.getProperty(SOUNDVIS_PROPERTY_APP_DIR);
			}
		}

	}

	public static void ensureAppDir(Supplier<File> appDirUserSupplier) {

		final boolean usrConfigWritable = new File(TARGET_CONF_DIR).canWrite();
		if (appDir == null && usrConfigWritable) {
			// write initial
			File ad = appDirUserSupplier.get();
			if(ad != null) {
				appDir = ad.getAbsolutePath();
				File soundvisConfDir = new File(SOUNDVIS_CONF_DIR);
				boolean dirCreated = soundvisConfDir.mkdir();
				IOUtil.log("config dir created: " + dirCreated);
				
				Properties cp = getProperties();
				if(cp.isEmpty()){
					IOUtil.log("creating new config file");
					cp.put(SOUNDVIS_PROPERTY_ASK_APP_DIR_ON_STARTUP, "false");
				}
				cp.put(SOUNDVIS_PROPERTY_APP_DIR, appDir);
				storeProperties(cp);
			}
		}
		if (appDir == null) { // conf dir not writable or no user selection
			final boolean bootDirWriteable = bootDir != null && new File(bootDir).canWrite();
			if(bootDirWriteable) {
				// 'classic' conf
				appDir = bootDir;
			}
			else {
				appDir = tempDir;
			}
		}
	}

	public static Properties getProperties() {
		Properties cp = new Properties();
		File confFile = new File(SOUNDVIS_CONF_FILE);
		final boolean usrConfigExists = userHomeDir != null && confFile.canWrite();
		if (usrConfigExists) {
			// load config
			try (FileReader fr = new FileReader(confFile)) {
				cp.load(fr);
			} catch (IOException e) {
				IOUtil.log("IOException: " + e.getMessage());
			}
			IOUtil.log(cp.size() + " properties loaded from file config: " + SOUNDVIS_CONF_FILE);
		}
		else {
			IOUtil.log(SOUNDVIS_CONF_FILE + " does not exist or not writeable, returning empty properties");
		}
		return cp;
	}
	public static void storeProperties(Properties cp) {
		File config = new File(SOUNDVIS_CONF_FILE);
		try(FileWriter fw = new FileWriter(config)){
			cp.store(fw, "GNU General Public License");
			IOUtil.log("stored " + SOUNDVIS_CONF_FILE);
		} catch (IOException e) {
			IOUtil.log("IOException: " + e.getMessage());
		}
	}
	
	public static String getBootDir() {
		return bootDir;
	}

	public static String getAppDir() {
		return appDir;
	}
	public static String getSep() {
		return sep;
	}

}
