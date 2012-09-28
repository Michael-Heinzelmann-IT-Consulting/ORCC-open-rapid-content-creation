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

/**
 * @author Michael Heinzelmann
 */
public class Pulsating implements SoundCanvas {
	
	@UserProperty(description="foreground color")
	private Color foreGround = Color.BLACK;
	@LimitedIntProperty(description="alpha is limited from 0 to 255", minimum=0, maximum=255)
	@UserProperty(description="alpha of the foreground color")
	int alpha = 255;
	@UserProperty(description="if reverse the low amplituses are large and high amplitudes small")
	boolean reverse = false;
	
	private int centerX;
	private int centerY;

	private float amplitudeDivisor;
	private float amplitudeMultiplicator;
	private AmplitudeHelper amplitude;
	
	int max;

	@Override
	public void nextSample(int[] amplitudes) {

		int mono = 2 * Math.abs(amplitude.getSignedMono(amplitudes));
		if(mono > max) {
			max = mono;
		}
		
	}

	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {
		
		int amp = amplitudeDivisor > 1 ? (int)(max / amplitudeDivisor) : (int)(max * amplitudeMultiplicator);
		if(reverse) {
			amp = centerY * 2 - amp;
		}
		graphics2D.setColor(new Color(foreGround.getRed(), foreGround.getGreen(), foreGround.getBlue(), alpha));		
		graphics2D.fillOval(centerX - amp / 2, centerY - amp / 2, amp, amp);
		
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		centerX = videoOutputInfo.getWidth() / 2;
		centerY = videoOutputInfo.getHeight() / 2;

		amplitude = new AmplitudeHelper(audioInputInfo);
		amplitudeDivisor = (amplitude.getAmplitudeRange() / videoOutputInfo.getHeight());
		if(amplitudeDivisor < 1){
			amplitudeMultiplicator = videoOutputInfo.getHeight() / amplitude.getAmplitudeRange();
		}
	}

	@Override
	public int getPreRunFrames() {
		// we need 1 frame for sampling data
		return 1;
	}

	@Override
	public void postFrame() {
		max = 0;
	}

	@Override
	public void drawCurrentIcon(int width, int height, Graphics2D graphics) {
		graphics.setColor(new Color(foreGround.getRed(), foreGround.getGreen(), foreGround.getBlue(), alpha));	
		int amp = Math.min(width, height);
		graphics.fillOval(width / 2 - amp / 2,  height / 2 - amp / 2, amp, amp);
	}


}
