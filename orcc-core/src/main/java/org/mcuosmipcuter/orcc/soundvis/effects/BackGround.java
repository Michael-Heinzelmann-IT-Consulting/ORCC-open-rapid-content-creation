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
import java.awt.Graphics2D;
import java.util.Map;
import java.util.Map.Entry;

import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.InputEnabling;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.soundvis.InputController;

/**
 * @author Michael Heinzelmann
 *
 */
public class BackGround extends InputController{
	
	public static enum CLIP_SHAPE {
		RECTANGLE, ELLIPSE
	}
	@ChangesIcon
	@UserProperty(description = "background enabled")
	private boolean enabled;

	@UserProperty(description = "zoom x", unit = Unit.PERCENT_VIDEO)
	@LimitedIntProperty(minimum = 0, description = "only positive integers")
	private int zoomX = 100;

	@UserProperty(description = "zoom y", unit = Unit.PERCENT_VIDEO)
	@LimitedIntProperty(minimum = 0, description = "only positive integers")
	private int zoomY = 100;
	
	@ChangesIcon
	@UserProperty(description = "complement foreground")
	private boolean complement;
	
	@ChangesIcon
	@UserProperty(description = "color background")
	private Color backGround = Color.GRAY;
	
	@UserProperty(description = "shape")
	private CLIP_SHAPE shape = CLIP_SHAPE.RECTANGLE;
	
	@UserProperty(description = "corner rounding", unit = Unit.PERCENT_OBJECT)
	@LimitedIntProperty(minimum = 0, maximum = 100, description = "only positive integers")
	int cornerRounding = 0;

	public void draw(Graphics2D graphics2d, int videoWidth, int videoHeight, Color foreGround) {
		if(!enabled) {
			return;
		}
		Color saveColor = graphics2d.getColor();
		int widthToUse = (int) (videoWidth * (zoomX / 100f));
		int heightToUse = (int) (videoHeight * (zoomY / 100f));
		int x = (videoWidth - widthToUse) / 2;
		int y = (videoHeight - heightToUse) / 2;
		Color colorToUse = getColorToUse(foreGround);

		graphics2d.setColor(colorToUse);
		if(shape == CLIP_SHAPE.RECTANGLE) {
			if(cornerRounding == 0) {
				graphics2d.fillRect(x, y, widthToUse, heightToUse);
			}
			else {
				int arc = (int) (Math.min(heightToUse, widthToUse) * (cornerRounding / 100f));
				graphics2d.fillRoundRect(x, y, widthToUse, heightToUse, arc, arc);
			}
		}
		else {
			graphics2d.fillOval(x, y, widthToUse, heightToUse);
		}

		graphics2d.setColor(saveColor);
	}
	
	public Color getColorToUse(Color foreGround) {
		Color colorToUse = complement && foreGround != null
				? new Color(255 - foreGround.getRed(), 255 - foreGround.getGreen(), 255 - foreGround.getBlue())
				: backGround;
		return colorToUse;
	}

	public boolean isDrawingEnabled() {
		return enabled;
	}

	@Override
	protected void doFieldEnablings(Map<String, InputEnabling> fieldEnablings) {
		for (Entry<String, InputEnabling> entry : fieldEnablings.entrySet()) {
			if (!"enabled".equals(entry.getKey())) {
				if ("cornerRounding".equals(entry.getKey())) {
					entry.getValue().enableInput(enabled && shape == CLIP_SHAPE.RECTANGLE);
				} else if ("backGround".equals(entry.getKey())) {
					entry.getValue().enableInput(enabled && !complement);
				} else {
					entry.getValue().enableInput(enabled);
				}
			}
		}
	}
	
}
