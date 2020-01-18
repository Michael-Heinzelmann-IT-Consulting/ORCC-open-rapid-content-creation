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
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.TimedChange;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;

/**
 * Displays an image
 * @author Michael Heinzelmann
 */
public class SlideShow implements SoundCanvas {
	
	@TimedChange
	@UserProperty(description="images to show")
	private BufferedImage[] images = null;
	
	@UserProperty(description="center x position")
	private int centerX = 0;
	@UserProperty(description="to center horizontally")
	private boolean centeredHorizontal = true;
	
	@UserProperty(description="center y position")
	private int centerY = 0;
	@UserProperty(description="to center vertically")
	private boolean centeredVertical = true;
	
	@UserProperty(description="number of frames per image")
	@LimitedIntProperty(minimum=0, description="cannot be negative")
	private int numberOfFrames = 0;
	private int numberOfFramesToUse = 25;
	int imageIndex = 0;
	
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

	VideoOutputInfo videoOutputInfo;
	
	@UserProperty(description="moveX in px per frame")
	private int moveX = 0;
	private int deltaX;
	
	@UserProperty(description="moveY in px per frame")
	private int moveY = 0;
	private int deltaY;
	
	@UserProperty(description="rotate dgrees per frame")
	private int rotate = 0;
	double rotatePosition;
	
	@UserProperty(description="cout out")
	private CLIP_SHAPE cutOut = CLIP_SHAPE.NONE;
	
	@LimitedIntProperty(minimum=0, description="cannot be lower than 0")
	@UserProperty(description="in scale 0 means none")
	private int scaleIn;
	@UserProperty(description="direction rule to scale in")
	private SCALE_RULE scaleRuleIn = SCALE_RULE.BOTH;
	
	@LimitedIntProperty(minimum=0, description="cannot be lower than 0")
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
	
	@LimitedIntProperty(minimum=0, description="cannot be lower than 0")
	@UserProperty(description="number of frames to fade in")
	private int fadeIn = 0;
	AlphaComposite ac  = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
	
	@LimitedIntProperty(minimum=0, description="cannot be lower than 0")
	@UserProperty(description="number of frames to fade out")
	private int fadeOut = 0;
	
	@UserProperty(description="rule for composite")
	private RULE compositeRule = RULE.SRC_OVER;
	
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

	private long frameFrom;
	private long frameTo;
	private long frameToConcrete;

	@Override
	public void nextSample(int[] amplitudes) {
		
	}

	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {
		if(images != null && frameCount >= frameFrom && (frameCount <= frameTo || frameTo ==0)) {
			long posInRange = frameCount - frameFrom;
			if(loop) {
				posInRange = posInRange % (numberOfFramesToUse * images.length);
			}
			final int oldImageIndex = imageIndex; // local snaphot
			imageIndex = (int)posInRange / numberOfFramesToUse;

				if(imageIndex >= images.length ) {
					imageIndex = loop ? 0 : images.length -1;
				}
			if(imageIndex != oldImageIndex) {
				deltaX = 0;
				deltaY = 0;
				rotatePosition = 0;
			}
			else {
				deltaX = (int)(frameCount % numberOfFramesToUse) * moveX;
				deltaY = (int)(frameCount % numberOfFramesToUse) * moveY;
			}
			

			BufferedImage image = images[imageIndex];
			if(image != null) {	

				int posInSlideDuration = (int)frameCount % numberOfFramesToUse;
				
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
				float currentScale = 1;
				float currentScaleX = 1;
				float currentScaleY = 1;
				if(scaleIn != 0 && posInSlideDuration <= scaleIn) {
					float scaleRateIn = 100f / (scaleIn * 100f);
					currentScale = posInSlideDuration * scaleRateIn;
					if(scaleRuleIn == SCALE_RULE.HORIZONTAL || scaleRuleIn == SCALE_RULE.BOTH) {
						scaleX *= currentScale;
						currentScaleX = currentScale;
					}
					if(scaleRuleIn == SCALE_RULE.VERTICAL || scaleRuleIn == SCALE_RULE.BOTH) {
						scaleY *= currentScale;
						currentScaleY = currentScale;
					}
				}
				if(scaleOut != 0 && posInSlideDuration > (numberOfFramesToUse - scaleOut)) {
					float scaleRateOut = 100f / (scaleOut * 100f);
					currentScale = (numberOfFramesToUse - posInSlideDuration - 1) * scaleRateOut;
					if(scaleRuleOut == SCALE_RULE.HORIZONTAL || scaleRuleOut == SCALE_RULE.BOTH) {
						scaleX *= currentScale;
						currentScaleX = currentScale;
					}
					if(scaleRuleOut == SCALE_RULE.VERTICAL || scaleRuleOut == SCALE_RULE.BOTH) {
						scaleY *= currentScale;
						currentScaleY = currentScale;
					}
				}

				
				float translateX =  ((float)(videoOutputInfo.getWidth() + dimensionHelper.realX(centerX) - (image.getWidth() * scaleX))) / 2f ;
				float translateY =    ((float)(videoOutputInfo.getHeight() + dimensionHelper.realY(centerY) -(image.getHeight()* scaleY))) / 2f;

				
				translateX = translateX + deltaX;
				deltaX += moveX;
				translateY = translateY + deltaY;
				deltaY += moveY;
				float wShape = (float) (scaleToWidth ? dimensionHelper.realX(scaledWidth) * currentScaleX
				: scaleToHeight ? origW * scaleX : origW * currentScaleX);
				float hShape = (float) (scaleToHeight ? dimensionHelper.realY(scaledHeight) * currentScaleY
				: scaleToWidth ? origH * scaleY : origH * currentScaleY);
				Shape clip = null;
				if (cutOut != CLIP_SHAPE.NONE) {
//					float wShape = (float) (scaleToWidth ? dimensionHelper.realX(scaledWidth) * currentScaleX
//							: scaleToHeight ? origW * scaleX : origW * currentScaleX);
//					float hShape = (float) (scaleToHeight ? dimensionHelper.realY(scaledHeight) * currentScaleY
//							: scaleToWidth ? origH * scaleY : origH * currentScaleY);
					//Shape clip;
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
	
					//graphics2D.setClip(clip);

				}
//				graphics2D.setColor(Color.BLACK);
//				if(clip != null) {
//				graphics2D.draw(clip);
//				}
				//graphics2D.drawRect((int)translateX, (int)translateY, (int)wShape, (int)hShape);
				AffineTransform transform = new AffineTransform(1, 0, 0, 1, translateX,translateY);
				//AffineTransform transform = new AffineTransform(scaleX, 0, 0, scaleY, translateX,translateY);
				if(rotate != 0) {
					double theta = Math.PI * rotate / 180;
					rotatePosition = posInSlideDuration * theta;
					//transform.rotate(rotatePosition, wShape / 2 / scaleX , hShape / 2/ scaleY);
					transform.rotate(rotatePosition, wShape / 2 , hShape / 2);
					
					
				}
				transform.scale(scaleX, scaleY);
				//graphics2D.clip(clip);
				final Composite saveComposite = graphics2D.getComposite();
				float transparency = 1.0f;

				if(fadeIn != 0 && posInSlideDuration <= fadeIn) {
					float fadeRate = 100f / (fadeIn * 100f);
					transparency = posInSlideDuration * fadeRate;
					transparency = transparency < 0 ? 0 : (transparency > 1 ? 1f : transparency);
					//System.err.println("transparency in " +transparency);
					graphics2D.setComposite(AlphaComposite.getInstance(compositeRule.getNumber(), transparency));  
				}
				if(fadeOut != 0 && posInSlideDuration > (numberOfFramesToUse - fadeOut)) {
					float fadeRate = 100f / (fadeOut * 100f);
					transparency = (numberOfFramesToUse - posInSlideDuration - 1) * fadeRate;
					transparency = transparency < 0 ? 0 : (transparency > 1 ? 1f : transparency);
					//System.err.println("transparency out " +transparency);
					graphics2D.setComposite(AlphaComposite.getInstance(compositeRule.getNumber(), transparency));  
				}
				final AffineTransform saveAT = graphics2D.getTransform();
				try {			
					graphics2D.transform(transform);
					//graphics2D.clip(clip);
					graphics2D.setClip(clip);
					//graphics2D.scale(scaleX, scaleY);
					graphics2D.drawImage(image, 0, 0, null, null);
					//graphics2D.drawString("numberOfFramesToUse: " + numberOfFramesToUse, 50, 50);
//					graphics2D.setColor(Color.RED);
//					if(clip != null) {
//					graphics2D.draw(clip);
//					}
				}
				finally {
					graphics2D.setComposite(saveComposite);
					graphics2D.setTransform(saveAT);
					graphics2D.setClip(null);
				}
			}
		}
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		this.videoOutputInfo = videoOutputInfo;
		this.dimensionHelper = new DimensionHelper(videoOutputInfo);
		if(numberOfFrames == 0) {
			if(images != null) {
				
				long to;
				if(frameTo == 0) {
					double audioLength =(double)audioInputInfo.getFrameLength();
					double sampleRate = audioInputInfo.getAudioFormat().getSampleRate();	
					double numberOfSeconds = audioLength / sampleRate;
					
					double frameRate = videoOutputInfo.getFramesPerSecond();
					to = (long)Math.floor( numberOfSeconds * frameRate);
				}
				else {
					to = frameTo;
				}
				frameToConcrete = to;
				long frameRange = to - frameFrom;
				numberOfFramesToUse = (int)frameRange / images.length; // even distribution, remainder depends loop flag
			}
			else {
				numberOfFramesToUse = videoOutputInfo.getFramesPerSecond(); // default 1s
			}
		}
		else {
			numberOfFramesToUse = numberOfFrames;
		}
		deltaX = 0;
		deltaY = 0;
	}

	public void setFrameRange(long frameFrom, long frameTo){
		this.frameFrom = frameFrom;
		this.frameTo = frameTo;
	}

	@Override
	public void updateUI(int widthPx, int heightPx, Graphics2D graphics) {
		if(images != null && images.length > 0) {
			iconImage = images[0].getScaledInstance(widthPx, heightPx, java.awt.Image.SCALE_SMOOTH);
			graphics.drawImage(iconImage, 0, 0, null, null);
			graphics.drawString(images.length + " images", 4, heightPx - 2);
		
		}
	}

	@Override
	public long[][] getFrameFromTos() {
		if(images != null && numberOfFramesToUse > 0) {
			long frame = frameFrom;
			List<long[]> ftos = new ArrayList<long[]>();
			while(frame < frameToConcrete) {
				ftos.add(new long[] {frame, frame + numberOfFramesToUse});
				frame += numberOfFramesToUse;
			}
			return ftos.toArray(new long[][] {});
		}
		return SoundCanvas.super.getFrameFromTos();
	}
	
	
	

}
