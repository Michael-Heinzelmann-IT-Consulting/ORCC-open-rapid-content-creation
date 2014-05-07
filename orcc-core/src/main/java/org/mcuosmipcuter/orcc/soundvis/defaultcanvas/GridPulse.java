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
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;

/**
 * @author Michael Heinzelmann
 */
public class GridPulse implements SoundCanvas {
	
	@UserProperty(description="maximum size of the grid , this size is reached on maximum amplitude, lower amplitudes produce proportionally smaller grid")
	int maxGridSize = 100;
	@UserProperty(description="max thickness")
	int thickness = 10;
	@UserProperty(description="whether to change thickness proportionally")
	boolean proportionalThickness = true;
	@UserProperty(description="whether to fill if the sreen is full")
	boolean fillOnFull = true;
	@UserProperty(description="foreground color")
	private Color foreGround = Color.BLACK;
	@UserProperty(description="if reverse the low amplituses are large and high amplitudes small")
	boolean reverse = false;
	@UserProperty(description="x distance from center")
	int shiftX = 0;
	@UserProperty(description="y distance from center")
	int shiftY = 0;
	
	private int centerX;
	private int centerY;

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
		final double factor = (double)max / (double)amplitude.getAmplitudeRange();
		int amp = (int)(factor * maxGridSize);
		int t = proportionalThickness ? (int)(factor * thickness) : thickness;
		if(reverse) {
			amp = maxGridSize - amp;
			if(proportionalThickness) {
				t = thickness - t;
			}
		}
		
		if(amp == 0) {
			amp = 1;
		}
		if(t == 0) {
			t = 1;
		}
		else if(t % 2 != 0) {
			t += 1;
		}

		graphics2D.setColor(foreGround);
		final int actualCenterX = centerX + shiftX;
		final int actualCenterY = centerY + shiftY;
		
		if(amp - t <= 0) {
			if(fillOnFull) {
				graphics2D.fillRect(0, 0, centerX * 2, centerY * 2);
			}
		}
		else {
		for(int x = actualCenterX ; x < centerX * 2; x += amp) {
			if( t == 1) {
				graphics2D.drawLine(x, 0, x, centerY * 2);
			}
			else{
				graphics2D.fillRect(x - t / 2, 0, t, centerY * 2);
			}
		}
		for(int x = actualCenterX - amp ; x > 0; x -= amp) {
			if( t == 1) {
				graphics2D.drawLine(x, 0, x, centerY * 2);
			}
			else {
				graphics2D.fillRect(x - t / 2, 0, t, centerY * 2);
			}
		}
		for(int y = actualCenterY; y < centerY * 2; y += amp){
			if( t == 1) {
				graphics2D.drawLine(0, y, centerX * 2, y);
			}
			else {
				graphics2D.fillRect(0, y - t / 2, centerX * 2, t);
			}
		}
		for(int y = actualCenterY - amp; y > 0; y -= amp){
			if( t == 1) {
				graphics2D.drawLine(0, y, centerX * 2, y);
			} else {
				graphics2D.fillRect(0, y - t / 2, centerX * 2, t);
			}
		}
		}
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo) {
		centerX = videoOutputInfo.getWidth() / 2;
		centerY = videoOutputInfo.getHeight() / 2;

		amplitude = new AmplitudeHelper(audioInputInfo);
	}

	@Override
	public void postFrame() {
		max = 0;
	}

	@Override
	public void drawCurrentIcon(int width, int height, Graphics2D graphics) {
		graphics.setColor(new Color(foreGround.getRed(), foreGround.getGreen(), foreGround.getBlue()));	
		int amp = Math.min(width, height) / 5;
		for(int x = 0; x < width; x += amp) {
			graphics.drawLine(x, 0, x, height);
		}
		for(int y = 0; y < height; y += amp) {
			graphics.drawLine(0, y, width, y);
		}
	}

}
