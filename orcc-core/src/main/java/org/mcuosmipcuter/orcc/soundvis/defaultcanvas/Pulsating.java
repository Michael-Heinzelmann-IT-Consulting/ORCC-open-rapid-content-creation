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
	
	public static enum DRAW_MODE {
		CIRCLE, SQARE, RECTANGLE, ELLIPSE
	}
	
	@UserProperty(description="foreground color")
	private Color foreGround = Color.BLACK;	@LimitedIntProperty(description="alpha is limited from 0 to 255", minimum=0, maximum=255)
	@UserProperty(description="if reverse the low amplituses are large and high amplitudes small")
	boolean reverse = false;
	@UserProperty(description="mode of fill")
	DRAW_MODE drawMode = DRAW_MODE.CIRCLE;
	
	private int centerX;
	private int centerY;

	private float amplitudeDivisorH;
	private float amplitudeMultiplicatorH;
	private float amplitudeDivisorW;
	private float amplitudeMultiplicatorW;
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
		
		int ampH = amplitudeDivisorH > 1 ? (int)(max / amplitudeDivisorH) : (int)(max * amplitudeMultiplicatorH);
		int ampW = amplitudeDivisorW > 1 ? (int)(max / amplitudeDivisorW) : (int)(max * amplitudeMultiplicatorW);
		if(reverse) {
			ampH = centerY * 2 - ampH;
			ampW = centerX * 2 - ampW;
		}
		graphics2D.setColor(foreGround);	
		switch(drawMode) {
			case CIRCLE:
				graphics2D.fillOval(centerX - ampH / 2, centerY - ampH / 2, ampH, ampH);
				break;
			case SQARE:
				graphics2D.fillRect(centerX - ampH / 2, centerY - ampH / 2, ampH, ampH);
				break;
			case ELLIPSE:
				graphics2D.fillOval(centerX - ampW / 2, centerY - ampH / 2, ampW, ampH);
				break;
			case RECTANGLE:
				graphics2D.fillRect(centerX - ampW / 2, centerY - ampH / 2, ampW, ampH);
				break;
			default:

		}
		
	}
	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		centerX = videoOutputInfo.getWidth() / 2;
		centerY = videoOutputInfo.getHeight() / 2;

		amplitude = new AmplitudeHelper(audioInputInfo);
		amplitudeDivisorH = (amplitude.getAmplitudeRange() / videoOutputInfo.getHeight());
		if(amplitudeDivisorH < 1){
			amplitudeMultiplicatorH = videoOutputInfo.getHeight() / amplitude.getAmplitudeRange();
		}
		amplitudeDivisorW = (amplitude.getAmplitudeRange() / videoOutputInfo.getWidth());
		if(amplitudeDivisorW < 1){
			amplitudeMultiplicatorW = videoOutputInfo.getWidth() / amplitude.getAmplitudeRange();
		}
	}

	@Override
	public void postFrame() {
		max = 0;
	}

	@Override
	public void drawCurrentIcon(int width, int height, Graphics2D graphics) {
		graphics.setColor(new Color(foreGround.getRed(), foreGround.getGreen(), foreGround.getBlue()));	
		int amp = Math.min(width, height);
		
		switch(drawMode) {
		case CIRCLE:
			graphics.fillOval(width / 2 - amp / 2,  height / 2 - amp / 2, amp, amp);
			break;
		case SQARE:
			graphics.fillRect(width / 2 - amp / 2,  height / 2 - amp / 2, amp, amp);
			break;
		case ELLIPSE:
			graphics.fillOval(0,  0, width, height);
			break;
		case RECTANGLE:
			graphics.fillRect(0,  0, width, height);
			break;
		default:

	}
	}

}
