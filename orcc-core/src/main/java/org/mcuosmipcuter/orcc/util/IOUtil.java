/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2012 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

/**
 * Input Output utilities
 * @author Michael Heinzelmann
 */
public class IOUtil {

	/**
	 * Boolean flag whether logging is enabled, switch on with -Dorg.mcuosmipcuter.orcc.util.IOUtil.log=true
	 */
	public final static boolean isLogOn = "true".equals(System.getProperty(IOUtil.class.getName() + ".log"));
	
	public static Function<String, Void> listener;

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
			System.out.println(Thread.currentThread().getName() + ": "+ msg);
		}
		if(listener != null) {
			listener.apply(Thread.currentThread().getName() + ": "+ msg);
		}
	}
	
	public static void logWithStack(Throwable ex) {
		if(ex == null) {
			log("programming error logWithStack Exception argument is null");
		}
		else {
			StringBuilder str = new StringBuilder(ex.getClass().getName());
			str.append("\nmessage: " +  ex.getMessage());
			for(StackTraceElement ste : ex.getStackTrace()) {
				str.append("\n" + ste.toString());
			}
			log(str.toString());
		}
	}

	public static void setListener(Function<String, Void> listener) {
		IOUtil.listener = listener;
	}

	public static Function<String, Void> getListener() {
		return listener;
	}

}
