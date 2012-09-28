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
import org.mcuosmipcuter.orcc.api.util.TextHelper;


/**
 * @author Michael Heinzelmann
 *
 */
public class XOR implements SoundCanvas {

	private int width;
	private int height;
	
	@LimitedIntProperty(minimum=-1, maximum=255, description="-1 means red is automatic")
	@UserProperty(description="RGB value 0-255 for red if -1 the amplitude min-max color is used")
	int fixedRed = -1;
	@LimitedIntProperty(minimum=-1, maximum=255, description="-1 means green is automatic")
	@UserProperty(description="RGB value 0-255 for green if -1 the amplitude min-max color is used")
	int fixedGreen = -1;
	@LimitedIntProperty(minimum=-1, maximum=255, description="-1 means blue is automatic")
	@UserProperty(description="RGB value 0-255 for blue if -1 the amplitude min-max color is used")
	int fixedBlue = -1;
	
	@LimitedIntProperty(minimum=0, description="percentage cannot be less than 0 and more than 100")
	@UserProperty(description="threshold when exceeded by max - min a an XOR paint is triggered")
	int thresholdPercent = 25;
	@LimitedIntProperty(minimum=1, description="cannot be less than 1 sample")
	@UserProperty(description="number of frames to take samples in order to determine min and max")
	int samplingFrames = 25;
	
	// state
	private int min = Integer.MAX_VALUE;
	private int max;
	
	Graphics2D graphics;
	int amplitudeDivisor;
	int sampleCount;
	int sampelsPerFrame;
	int onePercentOfSampleSize;
	
	/* (non-Javadoc)
	 * @see com.mcuosmipcuter.wave.DecodingCallback#nextSample(int[], byte[])
	 */
	@Override
	public void nextSample(int[] amplitudes) {
		for(int channel = 0; channel < amplitudes.length; channel++) {
			if(amplitudes[channel] < min) {
				min = amplitudes[channel];
			}
			if(amplitudes[channel] > max) {
				max = amplitudes[channel];
			}
		}
		sampleCount++;
	}

	/* (non-Javadoc)
	 * @see com.mcuosmipcuter.soundvis.gui.api.SoundCanvas#newFrame(long, java.awt.Graphics2D)
	 */
	@Override
	public void newFrame(long frameCount, Graphics2D graphics) {
		
		int threshold = thresholdPercent * onePercentOfSampleSize;
		if(max - min > threshold) {		
			int r = fixedRed == -1 ? min / amplitudeDivisor : fixedRed;
			int g = fixedGreen == -1 ? min / amplitudeDivisor : fixedGreen;
			int b = fixedBlue == -1 ? min / amplitudeDivisor : fixedBlue;
			graphics.setXORMode(graphics.getColor());
			graphics.setColor(new Color(r, g,  b));
			graphics.fillRect(0, 0, width, height);
			graphics.setPaintMode();
		}

	}

	
	@Override
	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo) {
		int sampleSizeBits = audioInputInfo.getAudioFormat().getSampleSizeInBits();
		long sampleSize = (long)Math.pow(2, sampleSizeBits);
		amplitudeDivisor = (int)(sampleSize / 256);
		this.sampelsPerFrame = (int)(sampleSize / videoOutputInfo.getFramesPerSecond());
		this.onePercentOfSampleSize = (int)(sampleSize / 100);
		this.width = videoOutputInfo.getWidth();
		this.height = videoOutputInfo.getHeight();
	}

	@Override
	public int getPreRunFrames() {
		// this number is needed to collect data
		return samplingFrames;
	}

	@Override
	public void postFrame() {
		int sampleSize = sampelsPerFrame * samplingFrames;
		if(sampleCount >= sampleSize) {
			min = Integer.MAX_VALUE;
			max = 0;
			sampleCount = 0;
		}
	}

	@Override
	public void drawCurrentIcon(int width, int height, Graphics2D graphics) {
		int r = fixedRed == -1 ? 255 : fixedRed;
		int g = fixedGreen == -1 ? 255: fixedGreen;
		int b = fixedBlue == -1 ? 255: fixedBlue;
		Color c = new Color(r, g, b);
		graphics.setColor(c);		
		graphics.fillRect(0, 0, width, height);
		TextHelper.writeText("XOR", graphics, 16f, Color.DARK_GRAY, width, height / 2);
	}


}
