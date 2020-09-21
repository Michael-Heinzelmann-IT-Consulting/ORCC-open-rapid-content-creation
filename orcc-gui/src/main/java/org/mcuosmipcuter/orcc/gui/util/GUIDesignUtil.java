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
package org.mcuosmipcuter.orcc.gui.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.mcuosmipcuter.orcc.soundvis.effects.Fader;
import org.mcuosmipcuter.orcc.soundvis.effects.Mover;
import org.mcuosmipcuter.orcc.soundvis.effects.Rotator;
import org.mcuosmipcuter.orcc.soundvis.effects.Scaler;
import org.mcuosmipcuter.orcc.soundvis.effects.Shearer;

/**
 * @author Michael Heinzelmann
 *
 */
public class GUIDesignUtil {
	
	private final static Map<Class<?>, Color[]> effectColors = new HashMap<>();
	
	static {
		effectColors.put(Mover.class, new Color[] {Color.ORANGE, Color.BLACK});
		effectColors.put(Fader.class, new Color[] {Color.CYAN, Color.BLACK});
		effectColors.put(Rotator.class, new Color[] {Color.RED, Color.BLACK});
		effectColors.put(Scaler.class, new Color[] {Color.BLUE, Color.WHITE});
		effectColors.put(Shearer.class, new Color[] {Color.GREEN, Color.BLACK});
	}

	public static Color getEffectBgColor(Class<?> effectClass, Color defaultColor) {
		Color[] carr = effectColors.get(effectClass);
		return carr != null ? carr[0] : defaultColor;
	}
	public static Color getEffectFgColor(Class<?> effectClass, Color defaultColor) {
		Color[] carr = effectColors.get(effectClass);
		return carr != null && carr.length > 1 ? carr[1] : defaultColor;
	}
}
