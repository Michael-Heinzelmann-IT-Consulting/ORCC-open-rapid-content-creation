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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
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
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.TimedChange;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;
import org.mcuosmipcuter.orcc.api.util.TextHelper;
import org.mcuosmipcuter.orcc.soundvis.defaultcanvas.model.Slide;
import org.mcuosmipcuter.orcc.util.IOUtil;


/**
 * Displays an image
 * @author Michael Heinzelmann
 */
public class SlideShow implements SoundCanvas {
	@TimedChange
	@UserProperty(description="slides to show")
	private Slide[] slides;
	
	@UserProperty(description="center x position")
	private int centerX = 0;
	
	@UserProperty(description="center y position")
	private int centerY = 0;
	
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
	
	@UserProperty(description="rotate dgrees per frame")
	private int rotate = 0;
	double rotatePosition;
	
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
	
	@UserProperty(description="number of frames to fade in")
	private int fadeIn = 0;
	AlphaComposite ac  = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
	
	@UserProperty(description="number of frames to fade out")
	private int fadeOut = 0;
	
	@UserProperty(description="rule for composite in")
	private RULE compositeInRule = RULE.SRC_OVER;
	
	@UserProperty(description="rule for composite out")
	private RULE compositeOutRule = RULE.SRC_OVER;
	
	public static enum RULE {
		CLEAR(1), SRC(2), DST(9), SRC_OVER(3), DST_OVER(4), SRC_IN(5), DST_IN(6), SRC_OUT(7), DST_OUT(8), SRC_ATOP(10),
		DST_ATOP(11), XOR(12);

		private int number;

		private RULE(int number) {
			this.number = number;
		}
		public int getNumber() {
			return this.number;
		}
	}
	@UserProperty(description="in move 0 means none")
	private int moveInXFrames;
	@UserProperty(description="move in x speed")
	private int moveInXSpeed;
	
	@UserProperty(description="in move 0 means none")
	private int moveOutXFrames;
	@UserProperty(description="move out x speed")
	private int moveOutXSpeed;
	
	@UserProperty(description="in move 0 means none")
	private int moveInYFrames;
	@UserProperty(description="move in y speed")
	private int moveInYSpeed;
	
	@UserProperty(description="in move 0 means none")
	private int moveOutYFrames;
	@UserProperty(description="move out y speed")
	private int moveOutYSpeed;

	private long frameFrom;
	private long frameTo;
	private long frameToConcrete;
	private List<DisplayDuration<Slide>> currentShowing = new ArrayList<>(3);

	@Override
	public void nextSample(int[] amplitudes) {
		
	}

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
				// adTo
//				int currCenterX = centerX + duration.getDisplayObject().getPosition() * 15;
//				int currCenterY = centerY + duration.getDisplayObject().getPosition() * 5;
				// TODO
				
				float translateX =  ((float)(videoOutputInfo.getWidth() + dimensionHelper.realX(centerX) - (origW * scaleX))) / 2f ;
				float translateY =    ((float)(videoOutputInfo.getHeight() + dimensionHelper.realY(centerY) -(origH * scaleY))) / 2f;
				
				if(moveInXFrames != 0 && posInSlideDuration <= Math.abs(moveInXFrames)) {
					int framesToGo = Math.abs(moveInXFrames) - posInSlideDuration;
					framesToGo = framesToGo > 0 ? framesToGo : 0;
					translateX = translateX -  framesToGo * moveInXSpeed;
				}

				if(moveOutXFrames != 0 && posInSlideDuration > (numberOfFramesSlideIsVisible - Math.abs(moveOutXFrames))) {
					int movedFrames = posInSlideDuration - (numberOfFramesSlideIsVisible - Math.abs(moveOutXFrames));
					translateX = translateX +  movedFrames * moveOutXSpeed;
				}
				if(moveInYFrames != 0 && posInSlideDuration <= Math.abs(moveInYFrames)) {
					int framesToGo = Math.abs(moveInYFrames) - posInSlideDuration;
					framesToGo = framesToGo > 0 ? framesToGo : 0;
					translateY = translateY -  framesToGo * moveInYSpeed;
				}

				if(moveOutYFrames != 0 && posInSlideDuration > (numberOfFramesSlideIsVisible - Math.abs(moveOutYFrames))) {
					int movedFrames = posInSlideDuration - (numberOfFramesSlideIsVisible - Math.abs(moveOutYFrames));
					translateY = translateY +  movedFrames * moveOutYSpeed;
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
				AffineTransform transform = new AffineTransform(1, 0, 0, 1, translateX,translateY);
				if(rotate != 0) {
					double theta = Math.PI * rotate / 180;
					rotatePosition = posInSlideDuration * theta;
					transform.rotate(rotatePosition, wShape / 2 , hShape / 2);	
				}
				transform.scale(scaleX, scaleY);

				final Composite saveComposite = graphics2D.getComposite();
				float transparency = 1.0f;

				if(fadeIn != 0 && posInSlideDuration <= Math.abs(fadeIn)) {
					float fadeRate = 100f / (Math.abs(fadeIn) * 100f);
					transparency = posInSlideDuration * fadeRate;
					transparency = transparency < 0 ? 0 : (transparency > 1 ? 1f : transparency);
					graphics2D.setComposite(AlphaComposite.getInstance(compositeInRule.getNumber(), transparency));  
				}
				if(fadeOut != 0 && posInSlideDuration > (numberOfFramesSlideIsVisible - Math.abs(fadeOut))) {
					float fadeRate = 100f / (fadeOut * 100f);
					transparency = 1-( (numberOfFramesSlideIsVisible - Math.abs(fadeOut)) - posInSlideDuration - 1) * fadeRate;
					transparency = transparency < 0 ? 0 : (transparency > 1 ? 1f : transparency);
					graphics2D.setComposite(AlphaComposite.getInstance(compositeOutRule.getNumber(), transparency));  
				}
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
			long to;
			if (frameTo == 0) {
				double audioLength = (double) audioInputInfo.getFrameLength();
				double sampleRate = audioInputInfo.getAudioFormat().getSampleRate();
				double numberOfSeconds = audioLength / sampleRate;

				double frameRate = videoOutputInfo.getFramesPerSecond();
				to = (long) Math.floor(numberOfSeconds * frameRate);
				frameToConcrete = to;
			} else {
				to = frameTo;
				frameToConcrete = frameTo;
			}
			if (numberOfFrames == 0) {
				long frameRange = to - frameFrom;
				numberOfFramesSlideIsVisible = (int) frameRange / slides.length; // even distribution, remainder depends
																					// loop flag
			} else {
				numberOfFramesSlideIsVisible = numberOfFrames;
			}
			timeLine.clear();
			long startFrame = frameFrom;
			int overLapBefore = 0;
			if(fadeIn != 0) {
				overLapBefore = fadeIn;	
			}
			if(scaleIn != 0) {
				overLapBefore = overLapBefore < 0 ? Math.min(overLapBefore, scaleIn): scaleIn;	
			}
			if(moveInXFrames != 0) {
				overLapBefore = overLapBefore < 0 ? Math.min(overLapBefore, moveInXFrames): moveInXFrames;	
			}
			if(moveInYFrames != 0) {
				overLapBefore = overLapBefore < 0 ? Math.min(overLapBefore, moveInYFrames): moveInYFrames;	
			}
			if(overLapBefore < 0) {
				startFrame += overLapBefore;
				numberOfFramesSlideIsVisible -= overLapBefore;
			}
			
			int overLapAfter = 0;
			if(fadeOut != 0 ) {
				overLapAfter = fadeOut;
			}
			if(scaleOut != 0) {
				overLapAfter = overLapAfter > 0 ? Math.max(overLapAfter, scaleOut) : scaleOut;
			}
			if(moveOutXFrames != 0) {
				overLapAfter = overLapAfter > 0 ? Math.max(overLapAfter, moveOutXFrames) : moveOutXFrames;
			}
			if(moveOutYFrames != 0) {
				overLapAfter = overLapAfter > 0 ? Math.max(overLapAfter, moveOutYFrames) : moveOutYFrames;
			}
			if(overLapAfter > 0) {
				numberOfFramesSlideIsVisible += overLapAfter;
			}
			long durationTo = 0;
			
			tl:
			while(durationTo < frameToConcrete) {
				
				for(Slide slide : slides) {

					DisplayDuration<Slide> duration = new DisplayDuration<>();
					duration.setDisplayObject(slide);
					duration.setFrom(startFrame);
					
					durationTo = startFrame  + numberOfFramesSlideIsVisible - 1;
					durationTo = durationTo < frameToConcrete ? durationTo : frameToConcrete;
					duration.setTo(durationTo);
					duration.setOverlapBefore(overLapBefore);
					duration.setOverlapAfter(overLapAfter);
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
