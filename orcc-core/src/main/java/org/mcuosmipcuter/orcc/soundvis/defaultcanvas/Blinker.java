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
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.Point2D;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;

/**
 * @author Michael Heinzelmann
 *
 */
public class Blinker implements SoundCanvas {

	@UserProperty(description="x size of tile in % of video width")
	int tileX = 100;
	@UserProperty(description="y size of tile in % of video height")
	int tileY = 100;
	@UserProperty(description="x position of center in % of video width")
	int centerX = 50;
	@UserProperty(description="y position of center in % of video height")
	int centerY = 50;
	@UserProperty(description="x position of focus  in % of video width")
	int focusX = 50;
	@UserProperty(description="y position of focus  in % of video height")
	int focusY = 50;
	@UserProperty(description="color of background")
	private Color backColor = Color.WHITE;
	@UserProperty(description="color of middle")
	private Color midColor = Color.YELLOW;
	@UserProperty(description="color of center")
	private Color centerColor = Color.RED;
	@UserProperty(description="cycle method")
	private CycleMethod cycleMethod = CycleMethod.NO_CYCLE;
	@UserProperty(description="size of radius in % of video height")
	private int radius = 100;
	@LimitedIntProperty(description="must be inbetween min and max", minimum=1, maximum=99)
	@UserProperty(description="distance in %")
	private int distance = 50;
	
	private float amplitudeDivisor;

	private AmplitudeHelper amplitude;
	private DimensionHelper dimensionHelper;
	long degrees;

	int max;
	
	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#nextSample(int[])
	 */
	@Override
	public void nextSample(int[] amplitudes) {

		int mono = amplitude.getUnSignedMono(amplitudes);
		if(mono > max) {
			max = mono;
		}
		
	}

	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#newFrame(long)
	 */
	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {

		int c = (int)(max / amplitudeDivisor);
		if(c < 1) {
			c = 1;
		}
		
		int w = dimensionHelper.realX(tileX);
		int h = dimensionHelper.realY(tileY);
		int x = dimensionHelper.realX(centerX);
		int y = dimensionHelper.realY(centerY);
		int fx = dimensionHelper.realX(focusX);
		int fy = dimensionHelper.realY(focusY);
		
	     Point2D center = new Point2D.Float(x, y);
	     Point2D focus = new Point2D.Float(fx, fy);

	     float radiusPx = h /(radius/100f) * (c/255f);
	     float dist = distance / 100f;
	     float[] distances = {0.0f,  dist, 1.0f};
	     Color[] colors = {centerColor, midColor , backColor};
	     RadialGradientPaint p =
	         new RadialGradientPaint(center, radiusPx, focus, distances, colors, cycleMethod);
	     graphics2D.setPaint(p);
	     graphics2D.fillRect(0, 0, w, h);

	}

	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#prepare(org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo, org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo, java.awt.Graphics2D, org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround)
	 */
	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		amplitude = new AmplitudeHelper(audioInputInfo);
		amplitudeDivisor = (amplitude.getAmplitudeRange() / 255);
		max = 0;
		dimensionHelper = new DimensionHelper(videoOutputInfo);
	}

	@Override
	public void postFrame() {
		max = 0;
	}

	@Override
	public void drawCurrentIcon(int width, int height, Graphics2D graphics) {
		// TODO Auto-generated method stub
		
	}

}
