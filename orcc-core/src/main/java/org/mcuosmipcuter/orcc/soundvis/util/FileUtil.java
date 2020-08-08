/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2020 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.soundvis.util;

import java.io.File;
import java.io.FileInputStream;

import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * @author Michael Heinzelmann
 *
 */
public class FileUtil {

	public static boolean binaryCompare(File file1, File file2) throws Exception {
		if (file1.length() != file2.length()) {
			IOUtil.log(file1.length() + " != " + file2.length());
			return false;
		}
		try (FileInputStream fin1 = new FileInputStream(file1); FileInputStream fin2 = new FileInputStream(file2);) {
			int b1 = -1;
			int b2 = -1;
			boolean equal = false;
			int c = 0;
			do {
				b1 = fin1.read();
				b2 = fin2.read();
				c++;
				equal = b1 == b2;
			} while (equal && b1 != -1 && b2 != -1);
			IOUtil.log(equal ? "equal." : "first difference at " + c);
			return equal;

		} catch (Exception ex) {
			throw ex;
		}
	}

}
