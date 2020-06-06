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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayUnit;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.TimedChange;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;
import org.mcuosmipcuter.orcc.soundvis.defaultcanvas.model.Slide;
import org.mcuosmipcuter.orcc.soundvis.effects.Fader;
import org.mcuosmipcuter.orcc.soundvis.effects.Mover;
import org.mcuosmipcuter.orcc.soundvis.effects.Positioner;
import org.mcuosmipcuter.orcc.soundvis.effects.Repeater;
import org.mcuosmipcuter.orcc.soundvis.effects.Rotator;
import org.mcuosmipcuter.orcc.soundvis.effects.Scaler;


/**
 * Displays a slide show
 * @author Michael Heinzelmann
 */
public class SlideShow implements SoundCanvas {
	@TimedChange
	@UserProperty(description="slides to show")
	private Slide[] slides;
	
	@UserProperty(description="number of frames per image")
	@LimitedIntProperty(minimum=0, description="cannot be negative")
	private int numberOfFrames = 0;
	@UserProperty(description="number of frames per image")
	@LimitedIntProperty(minimum=0, description="cannot be negative")
	private int repeat = 0;
	
	private DimensionHelper dimensionHelper;

	private java.awt.Image iconImage;

	VideoOutputInfo videoOutputInfo;
	AudioInputInfo audioInputInfo;
	
	@UserProperty(description="cout out")
	private CLIP_SHAPE cutOut = CLIP_SHAPE.NONE;
	
	
	public static enum SCALE_RULE {
		HORIZONTAL, VERTICAL, BOTH
	}
	
	public static enum CLIP_SHAPE {
		NONE, ELLIPSE, CIRCLE, ROUND_RECTANGLE,
	}
	@NestedProperty(description = "x and y position")
	Positioner positioner = new Positioner();
	@NestedProperty(description = "fading in and out")
	private Fader fader = new Fader();
	@NestedProperty(description = "moving in and out")
	private Mover mover = new Mover();
	@NestedProperty(description = "rotate in and out")
	private Rotator rotator = new Rotator();
	@NestedProperty(description = "scale in and out")
	Scaler scaler = new Scaler();
	
	private Repeater repeater = new Repeater(fader, mover, rotator, scaler);

	private long frameFrom;
	private long frameTo;

	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {
		


		if(slides != null && frameCount >= frameFrom && (frameCount <= frameTo || frameTo ==0)) {

			repeater.setRepeat(repeat==0 ? slides.length : repeat);
			repeater.setFrames(numberOfFrames);

			for(DisplayUnit displayUnit : repeater.repeat(frameFrom, frameTo, frameCount)) {

				if(! (slides[displayUnit.index % slides.length].getImage() instanceof BufferedImage)) {
					continue; // TODO non image slides
				}

				BufferedImage image = (BufferedImage) slides[displayUnit.index % slides.length].getImage();
				final AffineTransform saveAT = graphics2D.getTransform();

				Area imageArea = new Area(new Rectangle(image.getWidth(), image.getHeight()));

				AffineTransform transformS = scaler.scale(displayUnit.currentPosition, displayUnit.duration, imageArea.getBounds().width, imageArea.getBounds().height);
				imageArea.transform(transformS);

				AffineTransform transformP = positioner.position(dimensionHelper, imageArea.getBounds());
				imageArea.transform(transformP);

				int wShape = imageArea.getBounds().width;
				int hShape = imageArea.getBounds().height;
				int xShape = imageArea.getBounds().x;
				int yShape = imageArea.getBounds().y;
				
				Shape clip = null;
				if (cutOut != CLIP_SHAPE.NONE) {
					if (cutOut == CLIP_SHAPE.ELLIPSE) {
						clip = new Ellipse2D.Float(xShape, yShape, wShape, hShape);
					} else if (cutOut == CLIP_SHAPE.CIRCLE) {
						if (wShape >= hShape) {
							clip = new Ellipse2D.Float(xShape + (wShape - hShape) / 2, yShape, hShape, hShape);
						} else {
							clip = new Ellipse2D.Float(xShape, yShape + (hShape - wShape) / 2, wShape, wShape);
						}
					} else {
						float corner = Math.min(wShape / 10, hShape / 10);
						clip = new RoundRectangle2D.Float(xShape, yShape, wShape, hShape, corner, corner);
					}
				}
				
				AffineTransform transformR = rotator.rotate(displayUnit.currentPosition, displayUnit.duration, xShape + wShape / 2, yShape + hShape / 2);
				if(!transformR.isIdentity()) {
					imageArea.transform(transformR);
				}
				AffineTransform transformM = mover.move(displayUnit.currentPosition, displayUnit.duration);
				if(!transformM.isIdentity()) {
					imageArea.transform(transformM);
				}

				final Composite saveComposite = fader.fade(graphics2D, displayUnit.currentPosition, displayUnit.duration);


				try {							
					graphics2D.transform(transformM);
					graphics2D.transform(transformR);
					graphics2D.setClip(clip);
					graphics2D.transform(transformP);
					graphics2D.transform(transformS);
					graphics2D.drawImage(image, 0, 0, null, null);
				}
				finally {
					graphics2D.setComposite(saveComposite);
					graphics2D.setTransform(saveAT);
					graphics2D.setClip(null);
				}
			}
		}
	}

	private void updateSlides() {
		if(audioInputInfo != null && slides != null) {
			if (numberOfFrames == 0) {
				repeater.setRepeat(repeat==0 ? slides.length : repeat);
			} else {
				repeater.setFrames(numberOfFrames);
			}
		}
	}
	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		this.videoOutputInfo = videoOutputInfo;
		this.dimensionHelper = new DimensionHelper(videoOutputInfo);
		this.audioInputInfo = audioInputInfo;
		updateSlides();
	}

	public void setFrameRange(long frameFrom, long frameTo){
		this.frameFrom = frameFrom;
		this.frameTo = frameTo;
		if(slides != null && frameTo - frameFrom < slides.length) {
			this.frameTo = frameFrom + slides.length;
		}
	}

	@Override
	public void updateUI(int widthPx, int heightPx, Graphics2D graphics) {
		if(slides != null && slides.length > 0) {
			if(slides[0].getImage() instanceof BufferedImage) {
				iconImage = ((BufferedImage)slides[0].getImage()).getScaledInstance(widthPx, heightPx, java.awt.Image.SCALE_SMOOTH);
				graphics.drawImage(iconImage, 0, 0, null, null);
			}
			graphics.drawString(slides.length + " images", 4, heightPx - 2);
			updateSlides();
		}
		else {
			graphics.setColor(Color.DARK_GRAY);
			graphics.drawString("-- no slides --", 4, heightPx - 2);
		}
	}

	@Override
	public DisplayDuration<?>[] getFrameFromTos() {
		return repeater.getFrameFromTos(frameFrom, frameTo);
	}

}
