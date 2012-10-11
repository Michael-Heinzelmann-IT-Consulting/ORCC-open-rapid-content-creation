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
package org.mcuosmipcuter.orcc.soundvis.model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;

/**
 * Implementation of a sound canvas wrapper
 * @author Michael Heinzelmann
 */
public class SoundCanvasWrapperImpl implements SoundCanvasWrapper {
	
	final SoundCanvas soundCanvas;
	boolean enabled = true;
	long frameFrom = 0;
	long frameTo = 0;
	private Graphics2D devNullGraphics;
	private boolean selected;
	
	public SoundCanvasWrapperImpl(SoundCanvas soundCanvas) {
		this.soundCanvas = soundCanvas;
	}
	@Override
	public void nextSample(int[] amplitudes) {
		soundCanvas.nextSample(amplitudes);
	}

	@Override
	public void newFrame(long frameCount, Graphics2D graphics2d) {
		if(enabled && frameCount >= frameFrom && (frameCount <= frameTo || frameTo <= 0)) {
			// draws to the real graphics
			soundCanvas.newFrame(frameCount, graphics2d);
		}
		else {
			// it's not reasonable to proxy graphics or make a wrapper with 
			//limited number of methods, use a dummy graphics object 
			soundCanvas.newFrame(frameCount, devNullGraphics);
		}
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		soundCanvas.prepare(audioInputInfo, videoOutputInfo);
		//since this image is for nothing it can be small
		BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		devNullGraphics = bi.createGraphics();
	}

	@Override
	public String getDisplayName() {
		return soundCanvas.getClass().getSimpleName();
	}

	@Override
	public boolean isVisible() {
		return enabled;
	}
	@Override
	public void setVisible(boolean enabled) {
		this.enabled = enabled;
	}
	@Override
	public SoundCanvas getSoundCanvas() {
		return soundCanvas;
	}
	@Override
	public String toString() {
		return getDisplayName();
	}
	@Override
	public int getPreRunFrames() {
		return soundCanvas.getPreRunFrames();
	}
	@Override
	public void postFrame() {
		soundCanvas.postFrame();
	}
	@Override
	public void drawCurrentIcon(int width, int height, Graphics2D graphics) {
		soundCanvas.drawCurrentIcon(width, height, graphics);
	}
	@Override
	public long getFrameFrom() {
		return frameFrom;
	}
	@Override
	public long getFrameTo() {
		return frameTo;
	}
	@Override
	public void setFrameFrom(long frameFrom) {
		this.frameFrom = frameFrom;
	}
	@Override
	public void setFrameTo(long frameTo) {
		this.frameTo = frameTo;
	}
	@Override
	public boolean isSelected() {
		return selected;
	}
	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
