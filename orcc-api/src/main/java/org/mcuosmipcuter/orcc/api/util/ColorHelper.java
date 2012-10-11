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
package org.mcuosmipcuter.orcc.api.util;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Helps with color related task, stateful
 * @author Michael Heinzelmann
 */
public class ColorHelper {
	
	/**
	 * New helper with color initialized to the given alpha
	 * @param initialAlpha
	 */
	public ColorHelper(int initialAlpha) {
		alphaShadow = initialAlpha;
	}
	// field to detect changes of alpha
	private int alphaShadow;
	private Color colorWithAlpha;

	/**
	 * Set a new color and / or alpha
	 * @param alpha 0-255 value of alpha
	 * @param color the color to set
	 * @param graphics2D the graphics where the color is set
	 */
	public void setColorWithAlpha(int alpha, Color color, Graphics2D graphics2D) {
		if(alpha < 255) {
			if(alpha != alphaShadow) {
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				colorWithAlpha = new Color(r, g, b, alpha);
			}
			graphics2D.setColor(colorWithAlpha);		
		}
		else {
			graphics2D.setColor(color);
		}
		alphaShadow = alpha;
	}
	
}
