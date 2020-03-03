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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;
import org.mcuosmipcuter.orcc.soundvis.effects.Fader;
import org.mcuosmipcuter.orcc.soundvis.effects.Positioner;
import org.mcuosmipcuter.orcc.soundvis.effects.Repeater;
import org.mcuosmipcuter.orcc.soundvis.effects.Rotator;
import org.mcuosmipcuter.orcc.soundvis.effects.Scaler;

/**
 * Displays a solid color
 * 
 * @author Michael Heinzelmann
 */
public class Shutter implements SoundCanvas {

	public static enum CLIP_SHAPE {
		RECTANGLE, ELLIPSE, DIAMOND,
	}

	@UserProperty(description = "color of the area")
	private Color color = Color.BLACK;
	@UserProperty(description = "color of the area")
	private CLIP_SHAPE clipShape = CLIP_SHAPE.RECTANGLE;
	@LimitedIntProperty(description = "limits", minimum = 1, maximum = 36)
	@UserProperty(description = "rotate multiply the area")
	private int multiPlyRotated = 1;
	@UserProperty(description = "add shapes xor")
	private boolean multiplyXor;

	private int width;
	private int height;
	Shape screen = new Rectangle(width, height);

	private DimensionHelper dimensionHelper;
	private long frameFrom;
	private long frameTo;

	Polygon diamond;

	@NestedProperty(description = "x and y position")
	Positioner positioner = new Positioner();

	@NestedProperty(description = "rotate in and out")
	private Rotator rotator = new Rotator();

	@NestedProperty(description = "scale in and out")
	Scaler scaler = new Scaler();

	@NestedProperty(description = "fading in and out")
	private Fader fader = new Fader();
	
	@NestedProperty(description = "repeating inside from and to")
	private Repeater repeater = new Repeater();

	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {
		
		int posInSlideDuration = repeater.repeat(frameFrom, frameTo, frameCount);
		int repeatDurationFrames = repeater.getRepeatDurationFrames(frameFrom, frameTo);

		Shape clip;

		if (clipShape == CLIP_SHAPE.ELLIPSE) {
			clip = new Ellipse2D.Float(0, 0, width, height);
		} else if (clipShape == CLIP_SHAPE.DIAMOND) {
			clip = diamond;
		} else {
			clip = new Rectangle(width, height);
		}

		Area clipAreaInside = new Area(clip);

		AffineTransform atsc = scaler.scale(posInSlideDuration, repeatDurationFrames);
		clipAreaInside.transform(atsc);

		AffineTransform atp = positioner.position(dimensionHelper, clipAreaInside.getBounds());
		clipAreaInside.transform(atp);

		int centerX = clipAreaInside.getBounds().x + clipAreaInside.getBounds().width / 2;
		int centerY = clipAreaInside.getBounds().y + clipAreaInside.getBounds().height / 2;

		// System.err.println(centerX + "-" + centerY);
		AffineTransform atr = rotator.rotate(posInSlideDuration, repeatDurationFrames, centerX, centerY);
		clipAreaInside.transform(atr);

		double theta = Math.PI / (double) multiPlyRotated;

		Area fillArea = new Area(screen);
		fillArea.subtract(clipAreaInside);

		for (int i = 2; i <= multiPlyRotated; i++) {
			AffineTransform tr = new AffineTransform();
			tr.rotate(theta, centerX, centerY);
			clipAreaInside.transform(tr);
			if (multiplyXor) {
				fillArea.exclusiveOr(clipAreaInside);
			} else {
				fillArea.subtract(clipAreaInside);
			}
		}

		graphics2D.setColor(color);

		graphics2D.setClip(fillArea);
		final Composite saveComposite = fader.fade(graphics2D, posInSlideDuration, repeatDurationFrames);

		// graphics2D.setPaint(new GrayFilter(false, 50).);

		graphics2D.fillRect(0, 0, width, height);
		// graphics2D.fill(fillArea);

		graphics2D.setClip(null);
		graphics2D.setComposite(saveComposite);

	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo) {
		width = videoOutputInfo.getWidth();
		height = videoOutputInfo.getHeight();
		dimensionHelper = new DimensionHelper(videoOutputInfo);
		screen = new Rectangle(width, height);
		diamond = new Polygon(new int[] { 0, width / 2, width, width / 2 },
				new int[] { height / 2, height, height / 2, 0, }, 4);
	}

	@Override
	public void postFrame() {
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		graphics.setColor(color);
		graphics.fillRect(0, 0, width, height);
		if (color.getRed() > 245 && color.getGreen() > 245 && color.getBlue() > 245) {
			graphics.setColor(Color.BLACK);
			graphics.drawRect(0, 0, width - 1, height - 1);
		}
	}

	@Override
	public void setFrameRange(long frameFrom, long frameTo) {
		this.frameFrom = frameFrom;
		this.frameTo = frameTo;
	}

	@Override
	public DisplayDuration<?>[] getFrameFromTos() {
		return repeater.getFrameFromTos(frameFrom, frameTo, scaler, fader, rotator);
				
	}

}
