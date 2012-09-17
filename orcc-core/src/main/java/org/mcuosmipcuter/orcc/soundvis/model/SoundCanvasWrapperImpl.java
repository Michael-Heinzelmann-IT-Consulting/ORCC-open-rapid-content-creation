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

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround;
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
	
	public SoundCanvasWrapperImpl(SoundCanvas soundCanvas) {
		this.soundCanvas = soundCanvas;
	}
	@Override
	public void nextSample(int[] amplitudes) {
		if(enabled) {// TODO this causes wrong state
			soundCanvas.nextSample(amplitudes);
		}
	}

	@Override
	public void newFrame(long frameCount) {
		if(enabled) {// TODO refactor to use proxy graphics
			soundCanvas.newFrame(frameCount);
		}
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo, Graphics2D graphics,
			CanvasBackGround canvasBackGround) {
		soundCanvas.prepare(audioInputInfo, videoOutputInfo, graphics, canvasBackGround);
	}

	@Override
	public void preView(int width, int height, Graphics2D graphics) {
		soundCanvas.preView(width, height, graphics);
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

}
