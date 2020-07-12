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
import java.awt.Composite;
import java.awt.Graphics2D;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayUnit;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.soundvis.effects.Fader;
import org.mcuosmipcuter.orcc.soundvis.effects.Repeater;

/**
 * Displays a solid color
 * @author Michael Heinzelmann
 */
public class SolidColor implements SoundCanvas {
	@ChangesIcon
	@UserProperty(description="color of the area")
	private Color color = Color.WHITE;
	
	private int width;
	private int height;

	private long frameFrom;
	private long frameTo;
	
	@NestedProperty(description = "fading in and out")
	private Fader fader = new Fader();
	
	@NestedProperty(description = "repeating inside from and to")
	private Repeater repeater = new Repeater(fader);

	@Override
	public void nextSample(int[] amplitudes) {
	}

	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {	
		for(DisplayUnit displayUnit : repeater.repeat(frameFrom, frameTo, frameCount)) {
			graphics2D.setColor(color);
			Composite origComposite = fader.fade(graphics2D, displayUnit.currentPosition, displayUnit.duration);
			graphics2D.fillRect(0, 0, width, height);
			graphics2D.setComposite(origComposite);
		}
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		width = videoOutputInfo.getWidth();
		height = videoOutputInfo.getHeight();
	}

	@Override
	public void postFrame() {
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		graphics.setColor(color);
		graphics.fillRect(0, 0, width, height);
		if(color.getRed() > 245 && color.getGreen() > 245 && color.getBlue() > 245) {
			graphics.setColor(Color.BLACK);
			graphics.drawRect(0, 0, width -1, height - 1);
		}
	}
	public void setFrameRange(long frameFrom, long frameTo){
		this.frameFrom = frameFrom;
		this.frameTo = frameTo;
	}
	public DisplayDuration<?>[] getFrameFromTos() {
		return repeater.getFrameFromTos(frameFrom, frameTo);
	}

}
