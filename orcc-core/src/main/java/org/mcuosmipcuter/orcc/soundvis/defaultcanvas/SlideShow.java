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
import org.mcuosmipcuter.orcc.util.IOUtil;


/**
 * Displays an image
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

	@LimitedIntProperty(minimum=0, description="width cannot be lower than 0")
	@UserProperty(description="width of image, 0 means use video width")
	private int scaledWidth = 0;
	
	@LimitedIntProperty(minimum=0, description="height cannot be lower than 0")
	@UserProperty(description="height of image, 0 means use video height")
	private int scaledHeight = 0;
	
	private DimensionHelper dimensionHelper;

	private java.awt.Image iconImage;
	private List<DisplayDuration<Slide>> timeLine = new ArrayList<>();

	VideoOutputInfo videoOutputInfo;
	AudioInputInfo audioInputInfo;
	
	@UserProperty(description="cout out")
	private CLIP_SHAPE cutOut = CLIP_SHAPE.NONE;
	
	@UserProperty(description="in scale 0 means none")
	private int scaleIn;
	@UserProperty(description="direction rule to scale in")
	private SCALE_RULE scaleRuleIn = SCALE_RULE.BOTH;
	
	@UserProperty(description="in scale 0 means none")
	private int scaleOut;
	@UserProperty(description="direction rule to scale out")
	private SCALE_RULE scaleRuleOut = SCALE_RULE.BOTH;
	
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

				int posInSlideDuration = (int)(frameCount - duration.getFrom());
				float origH = image.getHeight();
				float origW = image.getWidth();

				final boolean scaleToWidth = scaledWidth != 0;
				final boolean scaleToHeight = scaledHeight != 0;
				float scaleX;
				float scaleY;
				if(scaleToWidth && scaleToHeight) {
					scaleX = (float)dimensionHelper.realX(scaledWidth) / origW;
					scaleY = (float)dimensionHelper.realY(scaledHeight) / origH;
				}
				else  {
					if(scaleToWidth) {
						scaleX = (float)dimensionHelper.realX(scaledWidth) / origW;
						scaleY = scaleX;
					}else if(scaleToHeight) {
						scaleY =  (float)dimensionHelper.realY(scaledHeight) / origH;
						scaleX = scaleY;
					}
					else {
						scaleX = 1.0f;
						scaleY = 1.0f;
					}
				}
				float currentScaleIn = 1;
				float currentScaleOut = 1;
				float currentScaleX = 1;
				float currentScaleY = 1;
				boolean isScaleIn = scaleIn != 0 && posInSlideDuration <= Math.abs(scaleIn);
				boolean isScaleOut = scaleOut != 0 && posInSlideDuration > (numberOfFramesSlideIsVisible - Math.abs(scaleOut));
				
				if(isScaleIn) {
					float scaleRateIn = 100f / (Math.abs(scaleIn) * 100f);
					currentScaleIn = posInSlideDuration * scaleRateIn;
				}
				if(isScaleOut) {
					float scaleRateOut = 100f / (Math.abs(scaleOut) * 100f);
					currentScaleOut = (numberOfFramesSlideIsVisible - posInSlideDuration - 1) * scaleRateOut;
				}
				if(isScaleIn||isScaleOut) {
					float currentScale;
					SCALE_RULE scaleRule;
					if(currentScaleIn < currentScaleOut) {
						scaleRule = scaleRuleIn;
						currentScale = currentScaleIn;
					}
					else {
						scaleRule = scaleRuleOut;
						currentScale = currentScaleOut;
					}					
					if(scaleRule == SCALE_RULE.HORIZONTAL || scaleRule == SCALE_RULE.BOTH) {
						scaleX *= currentScale;
						currentScaleX = currentScale;
					}
					if(scaleRule == SCALE_RULE.VERTICAL || scaleRule == SCALE_RULE.BOTH) {
						scaleY *= currentScale;
						currentScaleY = currentScale;
					}
				}

				
				float wShape = (float) (scaleToWidth ? dimensionHelper.realX(scaledWidth) * currentScaleX
				: scaleToHeight ? origW * scaleX : origW * currentScaleX);
				float hShape = (float) (scaleToHeight ? dimensionHelper.realY(scaledHeight) * currentScaleY
				: scaleToWidth ? origH * scaleY : origH * currentScaleY);
				Shape clip = null;
				if (cutOut != CLIP_SHAPE.NONE) {
					if (cutOut == CLIP_SHAPE.ELLIPSE) {
						clip = new Ellipse2D.Float(0, 0, wShape / scaleX , hShape / scaleY);
					}
					else if(cutOut == CLIP_SHAPE.CIRCLE) {
						if(wShape >= hShape) {
							clip = new Ellipse2D.Float(0+(wShape-hShape)/ scaleX/2, 0, hShape /scaleX , hShape / scaleY);
							//System.err.println("1 w > h");
						}
						else {
							//System.err.println("2 h > w");
							clip = new Ellipse2D.Float(0 , 0 + (hShape- wShape)/scaleY/2, wShape / scaleX , wShape / scaleY);
						}
					} else {
						float corner = Math.min(wShape / scaleX / 10, hShape/ scaleY / 10);
						clip = new RoundRectangle2D.Float(0, 0, wShape / scaleX , hShape / scaleY, corner, corner);
					}

				}

				AffineTransform transform = positioner.position(dimensionHelper, new Rectangle(0, 0, (int)wShape, (int)hShape));
				AffineTransform transformM = mover.move(posInSlideDuration, numberOfFramesSlideIsVisible);
				if(!transformM.isIdentity()) {
					transform.concatenate(transformM);
				}
				AffineTransform transformR = rotator.rotate(posInSlideDuration, numberOfFramesSlideIsVisible, (int)(wShape / 2), (int)(hShape / 2));
				if(!transformR.isIdentity()) {
					transform.concatenate(transformR);
				}
				transform.scale(scaleX, scaleY);
				final Composite saveComposite = fader.fade(graphics2D, posInSlideDuration, numberOfFramesSlideIsVisible);

				final AffineTransform saveAT = graphics2D.getTransform();
				try {		
					graphics2D.transform(transform);
					graphics2D.setClip(clip);
					graphics2D.drawImage(image, 0, 0, null, null);
					//TextHelper.writeText("slide show", graphics2D, 400, Color.BLACK, (int)origW, (int)origH / 3);
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
			long frameToConcrete = frameTo;// - frameFrom < slides.length ? frameFrom + slides.length : frameTo;
			
			tl:
			while(durationTo < frameToConcrete && numberOfFramesSlideIsVisible > 0) {
				
				for(Slide slide : slides) {

					DisplayDuration<Slide> duration = new DisplayDuration<>();
					duration.setDisplayObject(slide);
					duration.setFrom(startFrame);
					
					durationTo = startFrame  + numberOfFramesSlideIsVisible - 1;
					durationTo = durationTo < frameToConcrete ? durationTo : frameToConcrete;
					duration.setTo(durationTo);
					duration.setEffectDurationIn(overLapBefore);
					duration.setEffectDurationOut(overLapAfter);
					startFrame = startFrame + numberOfFramesSlideIsVisible + (overLapBefore < 0 ? overLapBefore : 0) - (overLapAfter > 0 ? overLapAfter : 0);
					IOUtil.log(String.valueOf(duration));
					timeLine.add(duration);
					if(startFrame + overLapBefore >= frameToConcrete) {
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
