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
public class Tiles implements SoundCanvas {
	

	@LimitedIntProperty(minimum=-1, maximum=255, description="-1 means red is automatic")
	@UserProperty(description="RGB value 0-255 for red if -1 the amplitude min-max color is used")
	int fixedRed = -1;
	@LimitedIntProperty(minimum=-1, maximum=255, description="-1 means green is automatic")
	@UserProperty(description="RGB value 0-255 for green if -1 the amplitude min-max color is used")
	int fixedGreen = -1;
	@LimitedIntProperty(minimum=-1, maximum=255, description="-1 means blue is automatic")
	@UserProperty(description="RGB value 0-255 for blue if -1 the amplitude min-max color is used")
	int fixedBlue = -1;


	@UserProperty(description="x size of tile")
	int tileX = 240;
	@UserProperty(description="y size of tile")
	int tileY = 180;
	
	int size;
	
	private int width;
	private int height;

	private float amplitudeDivisor;

	private AmplitudeHelper amplitude;	
	Deque<Color> deque = new ArrayDeque<Color>();
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
		if(this.frameCount != frameCount) {
			if(deque.size() == size) {
				deque.removeFirst();
			}
			int c = (int)(max / amplitudeDivisor);
			int r = fixedRed == -1 ? c : fixedRed;
			int g = fixedGreen == -1 ? c: fixedGreen;
			int b = fixedBlue == -1 ? c: fixedBlue;
			deque.addLast(new Color(r, g, b));
		}

		int x = 0;
		int y = 0;
		for(Color color : deque) {
			graphics2D.setColor(color);
			graphics2D.fillRect(x, y, tileX, tileY);
			x += tileX;
			if(x >= width) {
				x = 0;
				y += tileY;
			}
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
		deque.clear();
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
