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
import org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
import org.mcuosmipcuter.orcc.api.util.TextHelper;


/**
 * Classic audio wave representation
 * @author Michael Heinzelmann
 */
public class ClassicWaves implements SoundCanvas {
	
	@UserProperty(description="color of the waves")
	private Color foreGroundColor = Color.BLUE;
	
	// parameters automatically set
	private float amplitudeDivisor;
	private float amplitudeMultiplicator;
	private int leftMargin;
	private int height;
	
	private AmplitudeHelper amplitude;
	private int factor;
	private long samplecount;
	int max;
	
	// state
	private Graphics2D graphics;
	private int counterInsideFrame;
	private int prevAmplitude;
	private CanvasBackGround canvasBackGround;

	@Override
	public void nextSample(int[] amplitudes) {
		if(counterInsideFrame == leftMargin) {
			canvasBackGround.drawBackGround();
		}
		int mono = amplitude.getSignedMono(amplitudes);
		int amp = amplitudeDivisor > 1 ? (int)(mono / amplitudeDivisor) : (int)(mono * amplitudeMultiplicator);
		if(factor == 1 || Math.abs(amp) > Math.abs(max)) {
			max = amp;
		}
		graphics.setColor(foreGroundColor);
		if(samplecount % factor == 0) {
			graphics.drawLine(counterInsideFrame, height / 2 - max , counterInsideFrame, height / 2 - prevAmplitude);
			counterInsideFrame++;
			prevAmplitude = max;
			max = 0;
		}
		samplecount++;

	}

	@Override
	public void newFrame(long frameCount) {	
		counterInsideFrame = leftMargin;
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo,  
			Graphics2D graphics, CanvasBackGround canvasBackGround)  {
		int frameRate = videoOutputInfo.getFramesPerSecond();
		int sampleRate = (int)audioInputInfo.getAudioFormat().getSampleRate(); // non integer sample rates are rare
		int pixelLengthOfaFrame = sampleRate / frameRate; // e.g. 44100 / 25 = 1764
		factor = (int)(pixelLengthOfaFrame / videoOutputInfo.getWidth()) + 1;
		int pixelsUsed = pixelLengthOfaFrame / factor;
		System.err.println(pixelsUsed + " used factor " + factor);
		leftMargin =  (videoOutputInfo.getWidth() - pixelsUsed) / 2;
		this.height = videoOutputInfo.getHeight();
		counterInsideFrame = leftMargin;
		amplitude = new AmplitudeHelper(audioInputInfo);
		amplitudeDivisor = (amplitude.getAmplitudeRange() / height);
		if(amplitudeDivisor < 1){
			amplitudeMultiplicator = height / amplitude.getAmplitudeRange();
		}
		this.graphics = graphics;
		this.canvasBackGround = canvasBackGround;
	}

	@Override
	public void preView(int width, int height, Graphics2D graphics) {
		String text = "draws the classic analyzer wave forms";
		graphics.setXORMode(Color.BLACK);
		TextHelper.writeText(text, graphics, 24f, Color.WHITE, width, height / 2);
		graphics.setPaintMode();
	}


}
