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
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
import org.mcuosmipcuter.orcc.api.util.ColorHelper;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;

/**
 * @author Michael Heinzelmann
 *
 */
public class Chameleon implements SoundCanvas {
	public static enum DRAW_MODE {
		CIRCLE, SQARE, RECTANGLE, ELLIPSE
	}
	
	@UserProperty(description="mode of fill")
	DRAW_MODE drawMode = DRAW_MODE.CIRCLE;

	@UserProperty(description="base color to add brightness")
	private Color baseColorForAdding = Color.BLACK;
	
	@UserProperty(description="add brightness to red")
	private boolean addRed = true;
	@UserProperty(description="add brightness to green")
	private boolean addGreen = true;
	@UserProperty(description="add brightness to blue")
	private boolean addBlue = true;
	
	@UserProperty(description="subtract brightness from red")
	private boolean subtractRed = true;
	@UserProperty(description="subtract brightness from green")
	private boolean subtractGreen = true;
	@UserProperty(description="subtract brightness from blue")
	private boolean subtractBlue = true;
	
	@LimitedIntProperty(description="must be inbetween min and max", minimum=0, maximum=100)
	@UserProperty(description="x start of shape in % of video width")
	int startX = 0;
	
	@LimitedIntProperty(description="must be inbetween min and max", minimum=0, maximum=100)
	@UserProperty(description="width of shape in % of video width")
	int sizeWidh = 100;
	
	@LimitedIntProperty(description="must be inbetween min and max", minimum=0, maximum=100)
	@UserProperty(description="y start of shape in % of video height")
	int startY = 0;
	
	@LimitedIntProperty(description="must be inbetween min and max", minimum=0, maximum=100)
	@UserProperty(description="height of shape in % of video height")
	int sizeHeight = 100;
	
	private AmplitudeHelper amplitude;
	private DimensionHelper dimensionHelper;

	long degrees;

	int max;
	int min;
	
	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#nextSample(int[])
	 */
	@Override
	public void nextSample(int[] amplitudes) {

		int mono = amplitude.getSignedMono(amplitudes);
		if(mono > max) {
			max = mono;
		}
		if(mono < min) {
			min = mono;
		}
		
	}

	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#newFrame(long)
	 */
	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {
		
		int ma = amplitude.getSignedPercent(max);
		int mi = amplitude.getSignedPercent(min);
		int r = addRed ? ma : 0;
		int g = addGreen ?  ma : 0;
		int b = addBlue ?  ma : 0;
		
		r = subtractRed ? r + mi : r;
		g = subtractGreen ? g + mi : g;
		b = subtractBlue ? b + mi : b;
		
		graphics2D.setColor(baseColorForAdding);
		ColorHelper.setColorFromPercentNoClipping(r, g, b, graphics2D);
		int x = dimensionHelper.realX(startX);
		int y = dimensionHelper.realX(startY);
		int w = dimensionHelper.realX(sizeWidh);
		int h = dimensionHelper.realY(sizeHeight);
		
		//graphics2D.fillRect(dimensionHelper.realX(startX), dimensionHelper.realX(startY), dimensionHelper.realX(sizeWidh), dimensionHelper.realY(sizeHeight));
		switch(drawMode) {
		case CIRCLE:
			graphics2D.fillOval(x, y, Math.min(w, h), Math.min(w, h));
			break;
		case SQARE:
			graphics2D.fillRect(x, y, Math.min(w, h), Math.min(w, h));
			break;
		case ELLIPSE:
			graphics2D.fillOval(x, y, w, h);
			break;
		case RECTANGLE:
			graphics2D.fillRect(x, y, w, h);
			break;
		default:
		}
		
//		TextHelper.writeText("max " + amplitude.getSignedPercent(max), graphics2D, 120, Color.BLUE, 600, 300);
//		TextHelper.writeText("min " + amplitude.getSignedPercent(min), graphics2D, 120, Color.BLUE, 600, 400);
//		TextHelper.writeText(r + " " + g + " " + b, graphics2D, 120, Color.BLUE, 600, 500);
	}

	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#prepare(org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo, org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo, java.awt.Graphics2D, org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround)
	 */
	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		amplitude = new AmplitudeHelper(audioInputInfo);
		max = 0;
		dimensionHelper = new DimensionHelper(videoOutputInfo);
	}

	@Override
	public void postFrame() {
		min = max;
		max = 0;
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		// TODO Auto-generated method stub
		
	}

}
