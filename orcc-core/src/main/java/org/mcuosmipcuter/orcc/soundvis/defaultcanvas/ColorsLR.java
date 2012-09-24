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
import org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
import org.mcuosmipcuter.orcc.api.util.TextHelper;

/**
 * Displays L and R amplitudes as colors, high values are lighter lower values darker
 * @author Michael Heinzelmann
 */
public class ColorsLR implements SoundCanvas {
	
	@LimitedIntProperty(minimum=-1, maximum=255, description="-1 means red is automatic")
	@UserProperty(description="RGB value 0-255 for red if -1 the amplitude min-max color is used")
	int fixedRed = -1;
	@LimitedIntProperty(minimum=-1, maximum=255, description="-1 means green is automatic")
	@UserProperty(description="RGB value 0-255 for green if -1 the amplitude min-max color is used")
	int fixedGreen = -1;
	@LimitedIntProperty(minimum=-1, maximum=255, description="-1 means blue is automatic")
	@UserProperty(description="RGB value 0-255 for blue if -1 the amplitude min-max color is used")
	int fixedBlue = -1;
	@LimitedIntProperty(description="alpha is limited from 0 to 255", minimum=0, maximum=255)
	@UserProperty(description="alpha of the foreground color")
	int alpha = 255;
	
	private int centerX;
	private int centerY;
	Graphics2D graphics2D;
	CanvasBackGround canvasBackGround;
	private int amplitudeDivisor;
	private AmplitudeHelper amplitude;
	
	int maxL;
	int maxR;
	int minL = Integer.MAX_VALUE;
	int minR = Integer.MAX_VALUE;

	@Override
	public void nextSample(int[] amplitudes) {
		if(amplitudes.length == 2) {
			if(amplitudes[0] < minL) {
				minL = amplitudes[0];
			}
			if(amplitudes[0] > maxL) {
				maxL = amplitudes[0];
			}
			if(amplitudes[0] < minR) {
				minR = amplitudes[1];
			}
			if(amplitudes[0] > maxR) {
				maxR = amplitudes[1];
			}
		}
		
	}

	@Override
	public void newFrame(long frameCount) {

		canvasBackGround.drawBackGround();
		
		int rgb = maxL / amplitudeDivisor;
		int r = fixedRed == -1 ? rgb : fixedRed;
		int g = fixedGreen == -1 ? rgb: fixedGreen;
		int b = fixedBlue == -1 ? rgb: fixedBlue;
		graphics2D.setColor(new Color(r, g, b, alpha));		
		graphics2D.fillRect(0, 0, centerX, centerY);
		
		rgb = minL / amplitudeDivisor;
		r = fixedRed == -1 ? rgb: fixedRed;
		g = fixedGreen == -1 ? rgb : fixedGreen;
		b = fixedBlue == -1 ? rgb: fixedBlue;
		graphics2D.setColor(new Color(r, g, b, alpha));		
		graphics2D.fillRect(0, centerY, centerX, centerY);
		
		rgb = maxR / amplitudeDivisor;
		r = fixedRed == -1 ? rgb : fixedRed;
		g = fixedGreen == -1 ? rgb: fixedGreen;
		b = fixedBlue == -1 ? rgb: fixedBlue;
		graphics2D.setColor(new Color(r, g, b, alpha));		
		graphics2D.fillRect(centerX, 0, centerX, centerY);
		
		rgb = minR/ amplitudeDivisor;
		r = fixedRed == -1 ? rgb : fixedRed;
		g = fixedGreen == -1 ? rgb: fixedGreen;
		b = fixedBlue == -1 ? rgb: fixedBlue;
		graphics2D.setColor(new Color(r, g, b, alpha));		
		graphics2D.fillRect(centerX, centerY, centerX, centerY);
		
		maxL = 0;
		maxR = 0;
		minL = Integer.MAX_VALUE;
		minR = Integer.MAX_VALUE;

	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo, Graphics2D graphics,
			CanvasBackGround canvasBackGround) {
		centerX = videoOutputInfo.getWidth() / 2;
		centerY = videoOutputInfo.getHeight() / 2;
		this.graphics2D = graphics;
		this.canvasBackGround = canvasBackGround;
		
		amplitude = new AmplitudeHelper(audioInputInfo);
		amplitudeDivisor = (int)amplitude.getAmplitudeRange() / 256;
	}

	@Override
	public void preView(int width, int height, Graphics2D graphics) {
		String text = "draws ...";
		graphics.setXORMode(Color.BLACK);
		TextHelper.writeText(text, graphics, 24f, Color.WHITE, width, height / 2);
		graphics.setPaintMode();
	}

}
