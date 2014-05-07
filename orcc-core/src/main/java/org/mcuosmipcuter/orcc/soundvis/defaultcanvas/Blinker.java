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
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.util.ArrayDeque;
import java.util.Deque;

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
public class Blinker implements SoundCanvas {
	

	@UserProperty(description="foreground color")
	private Color foreGround = Color.WHITE;
	@UserProperty(description="whether to draw xor")
	private boolean xor = false;


	@UserProperty(description="x size of tile")
	int tileX = 240;
	@UserProperty(description="y size of tile")
	int tileY = 180;
	
	int size;
	
	private int width;
	private int height;

	private float amplitudeDivisor;

	private AmplitudeHelper amplitude;	
	long degrees;

	int max;
	int samplesPerFrame;
	long frameCount;
	
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
		
		if(xor) {
			graphics2D.setXORMode(foreGround);
		}
			graphics2D.setColor(new Color(c, c, c));
			graphics2D.fillRect(0, 0, tileX, tileY);
		if(xor) {
			graphics2D.setPaintMode();
		}
		this.frameCount = frameCount;
	}

	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#prepare(org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo, org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo, java.awt.Graphics2D, org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround)
	 */
	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		int frameRate = videoOutputInfo.getFramesPerSecond();
		int sampleRate = (int)audioInputInfo.getAudioFormat().getSampleRate(); // non integer sample rates are rare
		samplesPerFrame = sampleRate / frameRate; // e.g. 44100 / 25 = 1764
		width = videoOutputInfo.getWidth();
		height = videoOutputInfo.getHeight();

		amplitude = new AmplitudeHelper(audioInputInfo);
		amplitudeDivisor = (amplitude.getAmplitudeRange() / 255);

		max = 0;
		size = (width / tileX) * (height / tileY);
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
