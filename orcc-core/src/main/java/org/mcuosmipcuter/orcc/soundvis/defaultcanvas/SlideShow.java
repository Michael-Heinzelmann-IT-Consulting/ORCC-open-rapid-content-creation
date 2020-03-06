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
import java.util.ArrayList;
import java.util.List;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
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
import org.mcuosmipcuter.orcc.soundvis.effects.Rotator;
import org.mcuosmipcuter.orcc.soundvis.effects.Scaler;
import org.mcuosmipcuter.orcc.util.IOUtil;


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
	private int numberOfFramesSlideIsVisible = 25;

	@UserProperty(description="loop image sequence")
	private boolean loop = true;
	
	private DimensionHelper dimensionHelper;

	private java.awt.Image iconImage;
	private List<DisplayDuration<Slide>> timeLine = new ArrayList<>();

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

	private long frameFrom;
	private long frameTo;

	private List<DisplayDuration<Slide>> currentShowing = new ArrayList<>(3);

	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {
		if(slides != null && frameCount >= frameFrom && (frameCount <= frameTo || frameTo ==0)) {

			currentShowing.clear();
			for(DisplayDuration<Slide> displayDuration: timeLine) {
				if(displayDuration.contains(frameCount)) {
					currentShowing.add(displayDuration);
				}
			}
			
			for(DisplayDuration<Slide> duration : currentShowing) {	

				if(! (duration.getDisplayObject().getImage() instanceof BufferedImage)) {
					continue; // TODO non image slides
				}

				BufferedImage image = (BufferedImage) duration.getDisplayObject().getImage();
				final AffineTransform saveAT = graphics2D.getTransform();
				int posInSlideDuration = (int)(frameCount - duration.getFrom());
				Area imageArea = new Area(new Rectangle(image.getWidth(), image.getHeight()));

				AffineTransform transformS = scaler.scale(posInSlideDuration, numberOfFramesSlideIsVisible);
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
				
				AffineTransform transformR = rotator.rotate(posInSlideDuration, numberOfFramesSlideIsVisible, xShape + wShape / 2, yShape + hShape / 2);
				if(!transformR.isIdentity()) {
					imageArea.transform(transformR);
				}
				AffineTransform transformM = mover.move(posInSlideDuration, numberOfFramesSlideIsVisible);
				if(!transformM.isIdentity()) {
					imageArea.transform(transformM);
				}

				final Composite saveComposite = fader.fade(graphics2D, posInSlideDuration, numberOfFramesSlideIsVisible);


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
				long frameRange = frameTo - frameFrom;
				numberOfFramesSlideIsVisible = (int) frameRange / slides.length; // even distribution, remainder depends
																					// loop flag
			} else {
				numberOfFramesSlideIsVisible = numberOfFrames;
			}
			timeLine.clear();
			long startFrame = frameFrom;
			int overLapBefore = 0;
			if(fader.getFadeIn() != 0) {
				overLapBefore = fader.getFadeIn();	
			}
			int scaleIn = (int)scaler.getDisplayDuration(overLapBefore, startFrame).getOverlapBefore();
			if(scaleIn != 0) {
				overLapBefore = overLapBefore < 0 ? Math.min(overLapBefore, scaleIn): scaleIn;	
			}
			if(mover.getMoveInXFrames() != 0) {
				overLapBefore = overLapBefore < 0 ? Math.min(overLapBefore, mover.getMoveInXFrames()): mover.getMoveInXFrames();	
			}
			if(mover.getMoveInYFrames() != 0) {
				overLapBefore = overLapBefore < 0 ? Math.min(overLapBefore, mover.getMoveInYFrames()): mover.getMoveInYFrames();	
			}
			if(rotator.getRotateInFrames() != 0) {
				overLapBefore = overLapBefore < 0 ? Math.min(overLapBefore, rotator.getRotateInFrames()): rotator.getRotateInFrames();
			}
			if(overLapBefore < 0) {
				startFrame += overLapBefore;
				numberOfFramesSlideIsVisible -= overLapBefore;
			}
			
			int overLapAfter = 0;
			if(fader.getFadeOut() != 0 ) {
				overLapAfter = fader.getFadeOut();
			}
			int scaleOut = (int)scaler.getDisplayDuration(overLapAfter, startFrame).getOverlapAfter();
			if(scaleOut != 0) {
				overLapAfter = overLapAfter > 0 ? Math.max(overLapAfter, scaleOut) : scaleOut;
			}
			if(mover.getMoveOutXFrames() != 0) {
				overLapAfter = overLapAfter > 0 ? Math.max(overLapAfter, mover.getMoveOutXFrames()) : mover.getMoveOutXFrames();
			}
			if(mover.getMoveOutYFrames() != 0) {
				overLapAfter = overLapAfter > 0 ? Math.max(overLapAfter, mover.getMoveOutYFrames()) : mover.getMoveOutYFrames();
			}
			if(rotator.getRotateOutFrames() != 0) {
				overLapAfter = overLapAfter > 0 ? Math.max(overLapAfter, rotator.getRotateOutFrames()) : rotator.getRotateOutFrames();
			}

			if(overLapAfter > 0) {
				numberOfFramesSlideIsVisible += overLapAfter;
			}
			
			long durationTo = 0;
			
			tl:
			while(durationTo < frameTo && numberOfFramesSlideIsVisible > 0) {
				
				for(Slide slide : slides) {

					DisplayDuration<Slide> duration = new DisplayDuration<>();
					duration.setDisplayObject(slide);
					duration.setFrom(startFrame);
					
					durationTo = startFrame  + numberOfFramesSlideIsVisible - 1;
					durationTo = durationTo < frameTo ? durationTo : frameTo;
					duration.setTo(durationTo);
					duration.setOverlapBefore(overLapBefore);
					duration.setOverlapAfter(overLapAfter);
					startFrame = startFrame + numberOfFramesSlideIsVisible + (overLapBefore < 0 ? overLapBefore : 0) - (overLapAfter > 0 ? overLapAfter : 0);
					IOUtil.log(String.valueOf(duration));
					timeLine.add(duration);
					if(startFrame + overLapBefore >= frameTo) {
						break tl;
					}
				}
				if(!loop) {
					break;
				}
				
			}
			IOUtil.log(String.valueOf(timeLine));
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
		if(timeLine != null && slides != null && numberOfFramesSlideIsVisible > 0) {
			return timeLine.toArray(new DisplayDuration<?>[] {});
		}
		return SoundCanvas.super.getFrameFromTos();
	}

}
