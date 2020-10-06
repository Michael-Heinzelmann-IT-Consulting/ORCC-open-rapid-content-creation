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
package org.mcuosmipcuter.orcc.soundvis.effects;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.MappedValue;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;
import org.mcuosmipcuter.orcc.api.util.TextHelper;
import org.mcuosmipcuter.orcc.soundvis.FontStore;

/**
 * @author Michael Heinzelmann
 */
public class SimpleText {
	
	public static enum Mode {
		OFF, TEXT, TEXT_AND_BACKGROUND
	}
	@UserProperty(description="draw mode")
	private Mode mode = Mode.OFF;
	@UserProperty(description="draw xor")
	private boolean xor;
	@UserProperty(description="background of text field")
	private Color backGroundColor = Color.WHITE;
	@UserProperty(description="color of text")
	private Color textColor = Color.BLACK;
	@UserProperty(description = "the font to use")
	private MappedValue<String> fontName = FontStore.getDefaultFont();
	@LimitedIntProperty(minimum=0, description="at least 0")
	@UserProperty(description="size of font")
	private int fontSize = 30;
	@LimitedIntProperty(minimum=0, maximum = 100, description="between 0 and 100")
	@UserProperty(description="top of text field")
	private int topField = 0;
	@LimitedIntProperty(minimum=0, maximum = 100, description="between 0 and 100")
	@UserProperty(description="width of text field")
	private int width = 100;
	@LimitedIntProperty(minimum=0, maximum = 100, description="between 0 and 100")
	@UserProperty(description="height of text field")
	private int height = 100;
	@LimitedIntProperty(minimum=0, maximum = 100, description="between 0 and 100")
	@UserProperty(description="top of text field")
	private int topText = 50;
	
	public void writeText(Graphics2D graphics2D, DimensionHelper dimensionHelper, String text) {
		if(text == null || mode == Mode.OFF || fontSize == 0) {
			return;
		}
		int w = dimensionHelper.realX(width);
		int h = dimensionHelper.realX(height);
		int tf = dimensionHelper.realY(topField);
		int tt = dimensionHelper.realY(topText);
		if(xor) {
			graphics2D.setXORMode(graphics2D.getColor());
		}
		if(mode == Mode.TEXT_AND_BACKGROUND) {
			graphics2D.setColor(backGroundColor);
			graphics2D.fillRect(0, tf, w, h);
		}
		graphics2D.setColor(textColor);
		Font backUp = graphics2D.getFont();		
		Font font = FontStore.getFontByMappedValue(fontName);
		graphics2D.setFont(font);
		TextHelper.writeText(text, graphics2D, fontSize, textColor, w, tt);
		graphics2D.setFont(backUp);
		
		if(xor) {
			graphics2D.setPaintMode();
		}
	}

}
