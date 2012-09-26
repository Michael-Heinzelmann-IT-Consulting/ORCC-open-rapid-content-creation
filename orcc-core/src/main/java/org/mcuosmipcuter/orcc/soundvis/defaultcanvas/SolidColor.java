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
package org.mcuosmipcuter.orcc.soundvis.defaultcanvas;

import java.awt.Color;
import java.awt.Graphics2D;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;

/**
 * Displays a solid color
 * @author Michael Heinzelmann
 */
public class SolidColor implements SoundCanvas {
	
	@UserProperty(description="color of the area")
	private Color color = Color.WHITE;
	@LimitedIntProperty(description="alpha is limited from 0 to 255", minimum=0, maximum=255)
	@UserProperty(description="alpha of the color")
	int alpha = 255;
	
	private int width;
	private int height;
	Graphics2D graphics2D;

	@Override
	public void nextSample(int[] amplitudes) {
		
	}

	@Override
	public void newFrame(long frameCount) {

		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		graphics2D.setColor(new Color(r, g, b, alpha));		
		graphics2D.fillRect(0, 0, width, height);

	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo, Graphics2D graphics) {
		width = videoOutputInfo.getWidth();
		height = videoOutputInfo.getHeight();
		this.graphics2D = graphics;
	}

	@Override
	public void preView(int width, int height, Graphics2D graphics) {

	}

}
