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
 *
 */
public class ThresholdVerticalLines implements SoundCanvas {
	@UserProperty(description="color of the waves")
	private Color foreGroundColor = Color.BLUE;
	@LimitedIntProperty(description="threshold must be between 0 and 100", minimum=0, maximum = 100)
	@UserProperty(description="consider amplitudes above this threshold for drawing, value in percent of maximum amplitude")
	private int threshold;
	protected int leftMargin;
	protected int height;
	protected int width;
	protected int[] amplitudes;	
	protected AmplitudeHelper amplitudeHelper;
	
	private int factor;
	private long samplecount;
	private int max;
	private int counterInsideFrame;
	
	
	@Override
	public final void nextSample(int[] amplitudes) {

		int mono = amplitudeHelper.getSignedMono(amplitudes);
		if(factor == 1 || Math.abs(mono) > Math.abs(max)) {
			max = mono;
		}
		
		if(samplecount % factor == 0) {
			this.amplitudes[counterInsideFrame] = max;
			counterInsideFrame++;
			max = 0;
		}
		samplecount++;

	}

	@Override
	public final void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo)  {
		int frameRate = videoOutputInfo.getFramesPerSecond();
		float sampleRate = audioInputInfo.getAudioFormat().getSampleRate(); 
		int pixelLengthOfaFrame = (int)Math.ceil(sampleRate / (float)frameRate); // e.g. 44100 / 25 = 1764
		factor = (int)(pixelLengthOfaFrame / videoOutputInfo.getWidth()) + 1;
		int pixelsUsed = (int)Math.ceil((float)pixelLengthOfaFrame / (float)factor);
		amplitudes = new int[pixelsUsed];
		leftMargin =  (videoOutputInfo.getWidth() - pixelsUsed) / 2;
		this.height = videoOutputInfo.getHeight();
		this.width = videoOutputInfo.getWidth();
		counterInsideFrame = 0;
		amplitudeHelper = new AmplitudeHelper(audioInputInfo);
	}


	@Override
	public final void postFrame() {
		counterInsideFrame = 0;
	}

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
	public void updateUI(int width, int height, Graphics2D graphics) {
		float t = threshold / 100f;
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		graphics.setColor(foreGroundColor);

		for(int x = 0; x < width; x++) {
			if(Math.random() > t) {
				graphics.drawLine(x, 0, x, height);
			}
		}

	}
	

}
