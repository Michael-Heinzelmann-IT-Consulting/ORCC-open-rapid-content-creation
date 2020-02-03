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
package org.mcuosmipcuter.orcc.soundvis;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.mcuosmipcuter.orcc.api.soundvis.MappedValue;

/**
 * @author Michael Heinzelmann
 */
public class FontStore {
	
	private static Font[] fonts;
	private static TreeMap<MappedValue<String>, Font> logicalFontNamesAlphabetically = new TreeMap<MappedValue<String>, Font>();
	
	public static Set<MappedValue<String>> getAll()  {
		return logicalFontNamesAlphabetically.keySet();
	}

	public static void init() {
		fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		Supplier<Set<MappedValue<String>>> all = new Supplier<Set<MappedValue<String>>>() {
			@Override
			public Set<MappedValue<String>> get() {
				return logicalFontNamesAlphabetically.keySet();
			}
		};
		for(Font font : fonts) {
			logicalFontNamesAlphabetically.put(new MappedValue<String>(font.getName(), font.getFontName(), all), font);
		}
		System.err.println(logicalFontNamesAlphabetically);
	}
	public static Font getFontByMappedValue(MappedValue<String> mappedValue) {
		return logicalFontNamesAlphabetically.get(mappedValue);
	}
	public static MappedValue<String> getDefaultFont(){
		for(MappedValue<String> mv : logicalFontNamesAlphabetically.keySet()) {
			if("Helvetica".equals(mv.getValue())) {
				System.err.println(mv);
				return mv;
			}
		}
		return null;
		//return logicalFontNamesAlphabetically.keySet().stream().findFirst().filter(v -> "Helvetica".equals(v.getValue())).orElse(null);
	}
}
