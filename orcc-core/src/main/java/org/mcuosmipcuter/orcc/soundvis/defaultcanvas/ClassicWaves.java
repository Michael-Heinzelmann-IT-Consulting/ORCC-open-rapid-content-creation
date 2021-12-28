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
import java.util.Map;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.InputEnabling;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyGroup;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
import org.mcuosmipcuter.orcc.api.util.ColorHelper;
import org.mcuosmipcuter.orcc.soundvis.InputController;
import org.mcuosmipcuter.orcc.soundvis.effects.MovingAverage;


/**
 * Classic audio wave representation
 * @author Michael Heinzelmann
 */
public class ClassicWaves extends InputController implements SoundCanvas {
	
	public static enum FILL {
		NONE, TOP, BOTTOM
	}
	public static enum BEAM_TYPE{
		FLAT, ETCHED, RAISED
	}
	
	@ChangesIcon
	@UserProperty(description="color of the waves")
	private Color foreGroundColor = Color.BLUE;
	
	@NestedProperty(description = "smoothening using moving average")
	MovingAverage movingAverage = new MovingAverage(1000);
	
	@UserProperty(description="whether to draw without margin")
	private boolean drawMargin = true;
	
	///// beams
	@SuppressWarnings("unused") // used by reflection
	private PropertyGroup beams = new PropertyGroup("beamWidth", "beamType");
	
	@UserProperty(description="beam width of analyzer", unit = Unit.PIXEL)
	@NumberMeaning(numbers = 0, meanings = "no beam")
	@LimitedIntProperty(minimum = 0, description = "not negative")
	private int beamWidth = 0;
	
	@UserProperty(description="beam type analyzer")
	private BEAM_TYPE beamType = BEAM_TYPE.FLAT;
	
	@ChangesIcon
	@UserProperty(description="whether to draw filled bottom")
	private FILL fill = FILL.NONE;
	
	@SuppressWarnings("unused") // used by reflection
	private PropertyGroup multiLine = new PropertyGroup("repeat", "distanceX", "distanceY", "symmetric");
	@UserProperty(description="repeat line in y axis")
	@NumberMeaning(numbers = 0, meanings = "off")
	@LimitedIntProperty(minimum = 0, description = "not negative")
	private int repeat = 0;
	@UserProperty(description="line distance in x axis")
	private int distanceX = 0;
	@UserProperty(description="line distance in y axis")
	private int distanceY = 0;
	@UserProperty(description="draw symmetric to original line")
	private boolean symmetric = false;

	
	// parameters automatically set
	private float amplitudeDivisor;
	private float amplitudeMultiplicator;
	private int leftMargin;
	private int height;
	private int wideMargin;
	
	private AmplitudeHelper amplitude;
	private int factor;
	private long samplecount;
	int max;

	// state
	private int counterInsideFrame;
	private int[] amplitudeBuffer;
	private int[] marginBuffer;
	private int prevAmplitude;
	
	@Override
	public void nextSample(int[] amplitudes) {

		int mono = amplitude.getSignedMono(amplitudes);
		int amp = amplitudeDivisor > 1 ? (int)(mono / amplitudeDivisor) : (int)(mono * amplitudeMultiplicator);
		
		amp = movingAverage.average(amp);
		
		if(factor == 1 || Math.abs(amp) > Math.abs(max)) {
			max = amp;
		}
		
		if(samplecount % factor == 0) {
			this.amplitudeBuffer[counterInsideFrame] = max;
			counterInsideFrame++;
			max = 0;
		}
		samplecount++;

	}

	@Override
	public void newFrame(long frameCount, Graphics2D graphics) {	
		counterInsideFrame = 0;
		graphics.setColor(foreGroundColor);
		int x = 0;
		final int lm = drawMargin ? wideMargin : leftMargin;
		int aMaxamp = 0;
		int aMinAmp = 0;

		int maxArrayPos = drawMargin ? marginBuffer.length + amplitudeBuffer.length : amplitudeBuffer.length;
		for(int i = 0; i < maxArrayPos; i++) {
			int amp;
			if(drawMargin) {
				amp = i < marginBuffer.length ? marginBuffer[i] : amplitudeBuffer[i - marginBuffer.length];
			}
			else {
				amp = amplitudeBuffer[i < amplitudeBuffer.length ? i : amplitudeBuffer.length - 1];
			}
			if(beamWidth > 0) {
				if(amp > aMaxamp) {
					aMaxamp = amp;
				}
				if(amp < aMinAmp) {
					aMinAmp = amp;
				}
				if((x + 1) % beamWidth == 0) {
					int ct = aMaxamp > Math.abs(aMinAmp) ? aMaxamp : aMinAmp;
					int cb = Math.abs(aMinAmp) > aMaxamp ? aMinAmp : aMaxamp;
					final int rectHeight = getY(aMaxamp - aMinAmp, height / 2 - ct, height / 2 + cb);
					if(beamType == BEAM_TYPE.FLAT) {
						graphics.fillRect(x  + lm - beamWidth + 1, getY(height / 2 - aMaxamp, 0 , height / 2 - cb), beamWidth, rectHeight);
					}
					else {
						graphics.fill3DRect(x  + lm - beamWidth + 1, getY(height / 2 - aMaxamp, 0, height / 2 - cb), beamWidth, rectHeight, beamType == BEAM_TYPE.RAISED);
					}
					aMaxamp = 0;
					aMinAmp = 0;
				}
			}
			else {
				int y2 = getY(height / 2 - prevAmplitude, 0,height);
				int mx = 0;
				int my = 0;
				for(int cy = 0; cy <= repeat; cy++) {
					if(my != 0 || mx != 0) {
						graphics.drawLine(lm + x + mx, height / 2 - amp + my, lm + x + mx, y2 + my);
						if(symmetric) {
							graphics.drawLine(lm + x + -mx, height / 2 - amp + -my, lm + x + -mx, y2 + -my);
						}
					}
					mx += distanceX;
					my += distanceY;
				}
				graphics.drawLine(lm + x, height / 2 - amp, lm + x, y2);
				prevAmplitude = amp;
			}
			x++;
		}
		for(int i = 0; i < marginBuffer.length; i++) {
			marginBuffer[i] = amplitudeBuffer[amplitudeBuffer.length - marginBuffer.length + i];		
		}
		if(drawMargin  &&  amplitudeBuffer.length - marginBuffer.length > 0) {
			prevAmplitude = amplitudeBuffer[amplitudeBuffer.length - marginBuffer.length - 1];	
		}
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo)  {
		int width = videoOutputInfo.getWidth();
		int frameRate = videoOutputInfo.getFramesPerSecond();
		float sampleRate = audioInputInfo.getAudioFormat().getSampleRate(); 
		int pixelLengthOfaFrame = (int)Math.ceil(sampleRate / (float)frameRate); // e.g. 44100 / 25 = 1764
		wideMargin = 0;
		if((float)width / (float) pixelLengthOfaFrame >= 2) { // wide video no 'zoom'
			width = pixelLengthOfaFrame * 2  - 1;
			wideMargin = (videoOutputInfo.getWidth() - width) / 2;
		}
		factor = (int)(pixelLengthOfaFrame / width) + 1;
		int pixelsUsed = (int)Math.ceil((float)pixelLengthOfaFrame / (float)factor);
		amplitudeBuffer = new int[pixelsUsed];
		marginBuffer = new int[width - pixelsUsed];
		leftMargin =  (videoOutputInfo.getWidth() - pixelsUsed) / 2;
		this.height = videoOutputInfo.getHeight();
		counterInsideFrame = 0;
		amplitude = new AmplitudeHelper(audioInputInfo);
		amplitudeDivisor = (amplitude.getAmplitudeRange() / height);
		if(amplitudeDivisor < 1){
			amplitudeMultiplicator = height / amplitude.getAmplitudeRange();
		}
	}

	@Override
	public void postFrame() {
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		if(ColorHelper.averageRGB(foreGroundColor) > 230) {
			graphics.setColor(Color.LIGHT_GRAY);
			graphics.fillRect(0, 0, width, height);
		}
		graphics.setColor(foreGroundColor);
		int x = 1;
		int prevAmp = 0;
		for(int i =0 ; i < width; i++) {
			int amp = (int)((0.5 - Math.random()) * height ) ;
			int y2 = getY(height / 2 - prevAmp, 0,height);
			graphics.drawLine( x, height / 2 - amp ,  x, y2);
			prevAmp = amp;
			x++;
			prevAmp = amp;
		}
		
	}
	
	private int getY(int none, int top, int bottom) {
		switch(fill) {
		case NONE:
			default:
			return none;
		case TOP:
			return top;
		case BOTTOM:
			return bottom;
		}
	}

	@Override
	protected void doFieldEnablings(Map<String, InputEnabling> fieldEnablings) {
		fieldEnablings.get("beamType").enableInput(beamWidth != 0);
		fieldEnablings.get("multiLine").enableInput(beamWidth == 0 && fill == FILL.NONE);
	}


}
