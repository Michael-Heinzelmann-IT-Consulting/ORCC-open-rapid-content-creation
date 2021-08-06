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
	public static final String SOUNDVIS_PROPERTIES_FILE_NAME = "soundvis.properties";
	public static final String SOUNDVIS_CONFIG_DIR_NAME = "soundvis";
	
	// system settings
	private static String userHomeDir = System.getProperty("user.home");
	private static String sep = System.getProperty("file.separator");
	private static String  tempDir = System.getProperty("java.io.tmpdir");
	private static String TARGET_CONF_DIR = userHomeDir + sep + ".config";
	private static String SOUNDVIS_CONF_DIR = TARGET_CONF_DIR + sep + SOUNDVIS_CONFIG_DIR_NAME;
	
	private static String bootDir;
	private static String appDir;
	
	private static String exportDir;
	private static String imageDir;
	private static String logDir;

	/**
	 * 
	 */
	public static void init() {

		// current directory
		bootDir = new File("").getAbsolutePath();
		
		// TODO mkdir .config
		boolean ask = false;
		File confFile = new File(SOUNDVIS_CONF_DIR + sep + SOUNDVIS_PROPERTIES_FILE_NAME);
		final boolean usrConfigExists = userHomeDir != null && confFile.canWrite();
		if(usrConfigExists) {
			// load config
			Properties cp = new Properties();

				try(FileReader fr = new FileReader(confFile)){
					cp.load(fr);
					ask = "true".equals(cp.getProperty(SOUNDVIS_PROPERTY_ASK_APP_DIR_ON_STARTUP));
					if(!ask) {
						appDir = cp.getProperty(SOUNDVIS_PROPERTY_APP_DIR);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			
		}
		final boolean bootDirWriteable = bootDir != null && new File(bootDir).canWrite();
		if(appDir == null && !ask && bootDirWriteable) {
			// 'classic' conf
			appDir = bootDir;
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
				File config = new File(soundvisConfDir.getAbsolutePath()  + sep + SOUNDVIS_PROPERTIES_FILE_NAME);
				Properties cp = new Properties();
				if(config.exists()) {
					try(FileReader fr = new FileReader(config)){
						cp.load(fr);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					cp.put(SOUNDVIS_PROPERTY_ASK_APP_DIR_ON_STARTUP, "false");
				}
				
				try(FileWriter fw = new FileWriter(config)){
					cp.put(SOUNDVIS_PROPERTY_APP_DIR, appDir);
					cp.store(fw, "GNU General Public License"); // TODO license, version
				} catch (IOException e) {
					e.printStackTrace();
				}
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
	
	public static String getAppDir() {
		return appDir;
	}
	public static String getSep() {
		return sep;
	}

}
