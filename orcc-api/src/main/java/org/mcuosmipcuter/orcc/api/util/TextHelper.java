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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Utility for text drawing
 * @author Michael Heinzelmann
 */
public class TextHelper {
	/**
	 * 'Writes' text from top to bottom with clipping on all 4 ends and no wrapping.
	 * @param text the text
	 * @param graphics2d graphics to write on
	 * @param fontSize size of font
	 * @param textColor color of font / text
	 * @param width 'paper' width, only used to determine margin, if width is not sufficient clipping will occur.
	 * @param topPos top position from where to start writing
	 */
	public static void writeText(final String text, final Graphics2D graphics2d, 
			final float fontSize, Color textColor, final int width, final int topPos) {
		
		if(text == null || text.length() == 0) {
			return;
		}
		final Font oldFont = graphics2d.getFont();
		try {
			Font f = graphics2d.getFont().deriveFont((float)fontSize);
			graphics2d.setFont(f);
			graphics2d.setColor(textColor);


			String[] lines = text.split("\n");
			int maxTextWidth = getTextDimesion(lines, graphics2d).width;
			int top = topPos;
			final int strHeight = graphics2d.getFontMetrics().getHeight();
			int leftMargin = (width - maxTextWidth) / 2 ;
			for(String line : lines) {
				graphics2d.drawString(line, leftMargin, top);
				top += strHeight;
			}
		}
		finally {
			graphics2d.setFont(oldFont);
		}
	}
	/**
	 * Get the dimensions of the given text array of lines
	 * @param lines lines of text
	 * @param graphics2d the graphics where the text will appear
	 * @return the dimensions
	 */
	public static Dimension getTextDimesion(String[] lines, Graphics graphics2d) {
		int maxWidth = 0;
		int height = 0;
		final int strHeight = graphics2d.getFontMetrics().getHeight();
		for(String line : lines) {
			final int strWidth = graphics2d.getFontMetrics().stringWidth(line);
			if(strWidth > maxWidth) {
				maxWidth = strWidth;
			}
			height += strHeight;
		}
		return new Dimension(maxWidth, height);
	}
}

