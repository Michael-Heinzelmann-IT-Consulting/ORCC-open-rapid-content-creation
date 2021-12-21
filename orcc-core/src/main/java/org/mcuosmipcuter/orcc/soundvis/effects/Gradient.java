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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.Map;

import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.InputEnabling;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.soundvis.InputController;

/**
 * @author Michael Heinzelmann
 *
 */
public class Gradient extends InputController{
	

	@ChangesIcon
	@UserProperty(description = "limit x", unit = Unit.PERCENT_VIDEO)
	private int limitX = 0;

	@ChangesIcon
	@UserProperty(description = "limit y", unit = Unit.PERCENT_VIDEO)
	private int limitY = 0;
	
	
	@ChangesIcon
	@UserProperty(description = "RGB complement color 1")
	private boolean complement;
	
	@ChangesIcon
	@UserProperty(description = "color gradient")
	private Color color2 = Color.GRAY;
	
	@ChangesIcon
	@UserProperty(description = "cycle gradient")
	private boolean cycle;

	public Paint draw(Graphics2D graphics2D, int x, int y, int width, int height, Color color1) {

		Paint original = graphics2D.getPaint();
		if(limitX != 0 || limitY != 0) {
			int lx = (int) (width * (limitX / 100f));
			int ly = (int) (height * (limitY / 100f));
			// correct rounding to zero
			final int minPix = cycle ? 2 : 1;
			if(limitX != 0 && Math.abs(lx) < minPix) {
				lx = limitX > 0 ? minPix : -minPix;
			}
			if(limitY != 0 && Math.abs(ly) < minPix) {
				ly = limitY > 0 ? minPix : -minPix;
			}
			Color color2ToUse = getColorToUse(color1);
			graphics2D.setPaint(new GradientPaint(x, y, color1, x + lx, y + ly, color2ToUse, cycle));
		}
		return original;
	}
	
	public Color getColorToUse(Color color) {
		Color colorToUse = complement && color != null
				? new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue())
				: color2;
		return colorToUse;
	}


	@Override
	protected void doFieldEnablings(Map<String, InputEnabling> fieldEnablings) {
		fieldEnablings.get("color2").enableInput(!complement);
	}
	
}
