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

import java.io.Closeable;
import java.io.IOException;

/**
 * Input Output utilities
 * @author Michael Heinzelmann
 */
public class IOUtil {

	/**
	 * Boolean flag whether logging is enabled, switch on with -Dorg.mcuosmipcuter.orcc.util.IOUtil.log=true
	 */
	public final static boolean isLogOn = "true".equals(System.getProperty(IOUtil.class.getName() + ".log"));

	/**
	 * Performs the usual close routine in finally blocks: 
	 * checks for null and catches exception and prints stack trace
	 * @param closeable
	 */
	public static void safeClose(Closeable closeable) {
		try {
			if(closeable != null) {
				closeable.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace(); // it's ignored but at least with some trace
		}
	}
	
	/**
	 * Log a message to the configured simple logging - it is not synchronized
	 * @param msg message to log
	 */
	public static void log(String msg) {
		if(isLogOn) {
			System.err.println(Thread.currentThread().getName() + ": "+ msg);
		}
	}

}
