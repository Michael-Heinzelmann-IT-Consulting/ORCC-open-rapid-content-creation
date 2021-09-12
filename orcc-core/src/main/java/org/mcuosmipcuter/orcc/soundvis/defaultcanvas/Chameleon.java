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
import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyGroup;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
import org.mcuosmipcuter.orcc.api.util.ColorHelper;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;
import org.mcuosmipcuter.orcc.soundvis.effects.MovingAverage;

/**
 * @author Michael Heinzelmann
 *
 */
public class Chameleon implements SoundCanvas {
	public static enum DRAW_MODE {
		CIRCLE, SQARE, RECTANGLE, ELLIPSE
	}
	
	@ChangesIcon
	@UserProperty(description="mode of fill")
	DRAW_MODE drawMode = DRAW_MODE.CIRCLE;

	@ChangesIcon
	@UserProperty(description="base color to add brightness")
	private Color baseColorForAdding = Color.BLACK;
	
	///// colors
	@SuppressWarnings("unused") // used by reflection
	private PropertyGroup colors = new PropertyGroup("addRed", "addGreen", "addBlue", "subtractRed", "subtractGreen", "subtractBlue");
	@ChangesIcon
	@UserProperty(description="add brightness to red")
	private boolean addRed = true;
	@ChangesIcon
	@UserProperty(description="add brightness to green")
	private boolean addGreen = true;
	@ChangesIcon
	@UserProperty(description="add brightness to blue")
	private boolean addBlue = true;
	
	@ChangesIcon
	@UserProperty(description="subtract brightness from red")
	private boolean subtractRed = true;
	@ChangesIcon
	@UserProperty(description="subtract brightness from green")
	private boolean subtractGreen = true;
	@ChangesIcon
	@UserProperty(description="subtract brightness from blue")
	private boolean subtractBlue = true;
	/////

	@LimitedIntProperty(description="must be inbetween min and max", minimum=0, maximum=100)
	@UserProperty(description="width of shape in % of video width", unit = Unit.PERCENT_VIDEO)
	int sizeWidh = 100;
	
	@LimitedIntProperty(description="must be inbetween min and max", minimum=0, maximum=100)
	@UserProperty(description="height of shape in % of video height", unit = Unit.PERCENT_VIDEO)
	int sizeHeight = 100;
	
	@NestedProperty(description = "smoothening using moving average")
	MovingAverage movingAverage = new MovingAverage(1200);
	
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
		mono = movingAverage.average(mono);
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
		setColors(graphics2D, ma,  mi);
		
		int w = dimensionHelper.realX(sizeWidh);
		int h = dimensionHelper.realY(sizeHeight);
		
		int x = 0;
		int y = 0;
		int radius = 0;
		
		switch(drawMode) {
		case CIRCLE:
		case SQARE:
			radius = Math.min(w, h);
			x = (dimensionHelper.getVideoWidth() - radius) / 2;
			y = (dimensionHelper.getVideoHeight() - radius) / 2;
			break;
		case ELLIPSE:
		case RECTANGLE:
			x = (dimensionHelper.getVideoWidth() - w) / 2;
			y = (dimensionHelper.getVideoHeight() - h) / 2;
			break;
		default:
			
		}
		
		switch(drawMode) {
		case CIRCLE:
			graphics2D.fillOval(x, y, radius, radius);
			break;
		case SQARE:
			radius = Math.min(w, h);
			graphics2D.fillRect(x, y, radius, radius);
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

	private void setColors(Graphics2D graphics2D, int ma, int mi) {
		int r = addRed ? ma : 0;
		int g = addGreen ?  ma : 0;
		int b = addBlue ?  ma : 0;
		
		r = subtractRed ? r + mi : r;
		g = subtractGreen ? g + mi : g;
		b = subtractBlue ? b + mi : b;
		
		graphics2D.setColor(baseColorForAdding);
		ColorHelper.setColorFromPercentNoClipping(r, g, b, graphics2D);
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
	public void updateUI(int w, int h, Graphics2D graphics2D) {
		setColors(graphics2D, 50, 50);
		int radius = Math.min(w, h);
		int x = (w - radius) / 2;
		int y = (h - radius) / 2;
		switch(drawMode) {
		case CIRCLE:
			graphics2D.fillOval(x, y, radius, radius);
			break;
		case SQARE:
			radius = Math.min(w, h);
			graphics2D.fillRect(x, y, radius, radius);
			break;
		case ELLIPSE:
			graphics2D.fillOval(0, 0, w, h);
			break;
		case RECTANGLE:
			graphics2D.fillRect(0, 0, w, h);
			break;
		default:
		}
		
	}

}
