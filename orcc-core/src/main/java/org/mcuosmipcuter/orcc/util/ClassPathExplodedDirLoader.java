/**
* Copyright 2012 Michael Heinzelmann IT-Consulting
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package org.mcuosmipcuter.orcc.util;

import java.io.File;
import java.util.Set;

/**
 * Finds class names matching the given target class inside exploded directories on the class path.
 * @author Michael Heinzelmann
 */
public class ClassPathExplodedDirLoader  {


	/**
	 * Loads all class names that match the given target class into the set given.
	 * @param resultSet set of strings to store the result
	 * @param targetClass the class to match (instanceof)
	 */
	public static final void loadClassNamesInto(Set<String> resultSet, Class<?> targetClass) {
		
		String classPath = System.getProperty("java.class.path");
		String separator = System.getProperty("path.separator");
		String fileSeparator = System.getProperty("file.separator");
		
		String[] paths = classPath.split(separator);

		for(String path : paths) {
			File f = new File(path);

			if(f.isDirectory()) {
				for(String top : f.list()) {
					String pathB = new String(path +  fileSeparator + top + fileSeparator);
					String packAge = new String(top + ".");
					File t = new File(f.getAbsolutePath() + fileSeparator + top);
					recurse(t.list(), pathB, fileSeparator, packAge, resultSet, targetClass);
				}
			}
		}
	}
	
	// private recursive calls
	private static void recurse(String[] paths, String path, String fileSeparator, String packAge, Set<String> result, Class<?> targetClass) {
		
		for(String p : paths) {
			File f = new File(path + p);

			if(f.isDirectory()) {
				recurse(f.list(), path + p + fileSeparator, fileSeparator, packAge + p + ".", result, targetClass);
			}
			else {
				if(p.endsWith(".class")) {
					String className = packAge + p.substring(0, p.length() - 6);
					Class<?> claZZ;
					try {
						claZZ = Class.forName(className);
						if(targetClass.isAssignableFrom( claZZ)) {
							result.add(className);
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
	
				}

			}
			
		}
	}

}