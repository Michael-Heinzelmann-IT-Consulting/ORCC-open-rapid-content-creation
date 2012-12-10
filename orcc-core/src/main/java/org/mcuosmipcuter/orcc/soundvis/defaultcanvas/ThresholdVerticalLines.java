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
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;

/**
 * @author Michael Heinzelmann
 *
 */
public class ThresholdVerticalLines extends FrameMonoAmplitudes {
	@UserProperty(description="color of the waves")
	private Color foreGroundColor = Color.BLUE;
	@LimitedIntProperty(description="threshold must be between 0 and 100", minimum=0, maximum = 100)
	@UserProperty(description="consider amplitudes above this threshold for drawing, value in percent of maximum amplitude")
	private int threshold;
	@Override
	public void newFrame(long frameCount, Graphics2D graphics) {	
		
		graphics.setColor(foreGroundColor);
		int x = 1;
		for(int amp : amplitudes) {
			int percent = amplitudeHelper.getSignedPercent(Math.abs(amp));	
			if(percent > threshold) {
				graphics.drawLine(leftMargin + x, 0 , leftMargin + x, height);
			}
			x++;
		}
	}
	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#drawCurrentIcon(int, int, java.awt.Graphics2D)
	 */
	@Override
	public void drawCurrentIcon(int width, int height, Graphics2D graphics) {
		// TODO Auto-generated method stub

	}
	@Override
	public void subClassPrepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		amplitudeHelper.getAmplitudeRange();
		
	}

}
