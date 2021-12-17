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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.ExtendedFrameHistory;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
import org.mcuosmipcuter.orcc.soundvis.effects.MovingAverage;
import org.mcuosmipcuter.orcc.util.RingMemory;

/**
 * @author Michael Heinzelmann
 */
public class Pulsating implements SoundCanvas, ExtendedFrameHistory {
	
	public static enum DRAW_MODE {
		CIRCLE, SQARE, RECTANGLE, ELLIPSE
	}
	
	@ChangesIcon
	@UserProperty(description="foreground color")
	private Color foreGround = Color.BLACK;	@LimitedIntProperty(description="alpha is limited from 0 to 255", minimum=0, maximum=255)
	@UserProperty(description="if reverse the low amplituses are large and high amplitudes small")
	boolean reverse = false;
	@ChangesIcon
	@UserProperty(description="mode of fill")
	DRAW_MODE drawMode = DRAW_MODE.CIRCLE;
	
	@NestedProperty(description = "smoothening using moving average")
	MovingAverage movingAverage = new MovingAverage(1000);
	
	@ChangesIcon
	@UserProperty(description="frame history", unit = Unit.FRAMES)
	@LimitedIntProperty(minimum = 1, description = "minimum 1")
	private int history = 1;
	
	@ChangesIcon
	@UserProperty(description="size of line, 0 = draw filled")
	@NumberMeaning(numbers = 0, meanings = "filled")
	@LimitedIntProperty(minimum = 0, description = "minimum 0")
	private int lineSize = 0;
	private int prevLineSize = 0; // reduce object creation
	
	RingMemory<int[]> ringMemory = new RingMemory<>();
	BasicStroke stroke = new BasicStroke(1);
	
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
		mono = movingAverage.average(mono);
		if(mono > max) {
			max = mono;
		}
		
	}

	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {

		int ampH = amplitudeDivisorH > 1 ? (int) (max / amplitudeDivisorH) : (int) (max * amplitudeMultiplicatorH);
		int ampW = amplitudeDivisorW > 1 ? (int) (max / amplitudeDivisorW) : (int) (max * amplitudeMultiplicatorW);
		if (reverse) {
			ampH = centerY * 2 - ampH;
			ampW = centerX * 2 - ampW;
		}
		graphics2D.setColor(foreGround);
		if (prevLineSize != lineSize) {
			
		}
		prevLineSize = lineSize;
		if (lineSize > 0) {
			stroke = new BasicStroke(lineSize);
			graphics2D.setStroke(stroke);
		}

		ringMemory.setSize(frameCount < history ? (int)frameCount : history );
		for (int[] wh : ringMemory.update(new int[] { ampW, ampH })) {
			int w = wh[0];
			int h = wh[1];
			switch (drawMode) {
			case CIRCLE:
				if (lineSize > 0) {
					graphics2D.drawOval(centerX - h / 2, centerY - h / 2, h, h);
				} else {
					graphics2D.fillOval(centerX - h / 2, centerY - h / 2, h, h);
				}
				break;
			case SQARE:
				if (lineSize > 0) {
					graphics2D.drawRect(centerX - h / 2, centerY - h / 2, h, h);
				} else {
					graphics2D.fillRect(centerX - h / 2, centerY - h / 2, h, h);
				}
				break;
			case ELLIPSE:
				if (lineSize > 0) {
					graphics2D.drawOval(centerX - w / 2, centerY - h / 2, w, h);
				} else {
					graphics2D.fillOval(centerX - w / 2, centerY - h / 2, w, h);
				}
				break;
			case RECTANGLE:
				if (lineSize > 0) {
					graphics2D.drawRect(centerX - w / 2, centerY - h / 2, w, h);
				} else {
					graphics2D.fillRect(centerX - w / 2, centerY - h / 2, w, h);
				}
				break;
			default:

			}
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
	public void updateUI(int width, int height, Graphics2D graphics) {
		graphics.setColor(new Color(foreGround.getRed(), foreGround.getGreen(), foreGround.getBlue()));
		int amplitude = Math.min(width, height);
		if (lineSize > 0) {
			stroke = new BasicStroke(Math.max(1, lineSize / 5));
			graphics.setStroke(stroke);
		}
		int hist = Math.min(3, history);
		for (int i = hist; i >= 1; i--) {
			int amp = amplitude / i;
			switch (drawMode) {
			case CIRCLE:
				if (lineSize > 0) {
					graphics.drawOval(width / 2 - amp / 2, height / 2 - amp / 2, amp, amp);
				} else {
					graphics.fillOval(width / 2 - amp / 2, height / 2 - amp / 2, amp, amp);
				}
				break;
			case SQARE:
				if (lineSize > 0) {
					graphics.drawRect(width / 2 - amp / 2, height / 2 - amp / 2, amp, amp);
				} else {
					graphics.fillRect(width / 2 - amp / 2, height / 2 - amp / 2, amp, amp);
				}
				break;
			case ELLIPSE:
				if (lineSize > 0) {
					graphics.drawOval(width / 2 - width / i / 2, height / 2 - amp / 2, width / i, amp);
				} else {
					graphics.fillOval(0, 0, width, height);
				}
				break;
			case RECTANGLE:
				if (lineSize > 0) {
					int w = (int) (width * 0.8);
					int a = (int) (amp * 0.8);
					graphics.drawRect(width / 2 - w / i / 2, height / 2 - a / 2, w / i, a);
				} else {
					graphics.fillRect((int) (width * 0.1), (int) (height * 0.1), (int) (width * 0.8),
							(int) (height * 0.8));
				}
				break;
			default:

			}
		}
	}

	@Override
	public int getCurrentHistoryFrameSize() {
		return history;
	}
	

}
