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
 * @author Michael Heinzelmann
 */
public class Pulsating implements SoundCanvas {
	
	@UserProperty(description="foreground color")
	private Color foreGround = Color.BLACK;
	@UserProperty(description="alpha of the foreground color")
	int alpha = 255;
	
	private int centerX;
	private int centerY;
	Graphics2D graphics2D;
	CanvasBackGround canvasBackGround;
	private float amplitudeDivisor;
	private float amplitudeMultiplicator;
	private AmplitudeHelper amplitude;
	
	int max;

	@Override
	public void nextSample(int[] amplitudes) {

		int mono = amplitude.getUnSignedMono(amplitudes);
		if(mono > max) {
			max = mono;
		}
		
	}

	@Override
	public void newFrame(long frameCount) {

		canvasBackGround.drawBackGround();
		
		int amp = amplitudeDivisor > 1 ? (int)(max / amplitudeDivisor) : (int)(max * amplitudeMultiplicator);
		graphics2D.setColor(new Color(foreGround.getRed(), foreGround.getGreen(), foreGround.getBlue(), alpha));		
		graphics2D.fillOval(centerX - amp / 2, centerY - amp / 2, amp, amp);

		max = 0;
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo, Graphics2D graphics,
			CanvasBackGround canvasBackGround) {
		centerX = videoOutputInfo.getWidth() / 2;
		centerY = videoOutputInfo.getHeight() / 2;
		this.graphics2D = graphics;
		this.canvasBackGround = canvasBackGround;
		
		amplitude = new AmplitudeHelper(audioInputInfo);
		amplitudeDivisor = (amplitude.getAmplitudeRange() / videoOutputInfo.getHeight());
		if(amplitudeDivisor < 1){
			amplitudeMultiplicator = videoOutputInfo.getHeight() / amplitude.getAmplitudeRange();
		}
	}

	@Override
	public void preView(int width, int height, Graphics2D graphics) {
		String text = "draws a pulsating ball onto the background";
		graphics.setXORMode(Color.BLACK);
		TextHelper.writeText(text, graphics, 24f, Color.WHITE, width, height / 2);
		graphics.setPaintMode();
	}

}
