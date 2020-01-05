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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

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
	
	@UserProperty(description="upper left x position")
	private int upperLeftCornerX = 0;
	@UserProperty(description="to center horizontally")
	private boolean centeredHorizontal = true;
	
	@UserProperty(description="upper left y position")
	private int upperLeftCornerY = 0;
	@UserProperty(description="to center vertically")
	private boolean centeredVertical = true;
	
	@UserProperty(description="number of frames per image")
	@LimitedIntProperty(minimum=0, description="cannot be negative")
	private int numberOfFrames = 0;
	private int numberOfFramesToUse = 25;
	int imageIndex = 0;
	
	@UserProperty(description="loop image sequence")
	private boolean loop = true;
	
	//@TimedChange
	@LimitedIntProperty(minimum=0, description="width cannot be lower than 0")
	@UserProperty(description="width of image, 0 means use video width")
	private int scaledWidth = 0;
	
	//@TimedChange
	@LimitedIntProperty(minimum=0, description="height cannot be lower than 0")
	@UserProperty(description="height of image, 0 means use video height")
	private int scaledHeight = 0;
	
	private DimensionHelper dimensionHelper;
	//private BufferedImage[] scaledImages;
	private java.awt.Image iconImage;
	//boolean imageUpdating;
	//private String scaled;
	//private String outputHash;
	VideoOutputInfo videoOutputInfo;
	
	@UserProperty(description="moveX in px per frame")
	private int moveX = 0;
	private int deltaX;
	
	@UserProperty(description="moveY in px per frame")
	private int moveY = 0;
	private int deltaY;

	private long frameFrom;
	private long frameTo;

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
			}
			else {
				deltaX = (int)(frameCount % numberOfFramesToUse) * moveX;
				deltaY = (int)(frameCount % numberOfFramesToUse) * moveY;
			}
			

			BufferedImage image = images[imageIndex];
			if(image != null) {	
//				BufferedImage i = (scaledWidth != 0 || scaledHeight != 0) ? scaledImages[imageIndex] : image;
				
				float origH = image.getHeight();
				float origW = image.getWidth();
				float aRatio = origH / origW; 
				final boolean scaleToWidth = scaledWidth != 0;
				final boolean scaleToHeight = scaledHeight != 0;
				float scaleX;// = scaleToWidth ?  (float)videoOutputInfo.getWidth() / origW : scaleToHeight ? 1 : aRatio;
				float scaleY;// = scaleToHeight ?  (float)videoOutputInfo.getHeight() / origH  : scaleToWidth ? 1 : aRatio;
				if(scaleToWidth && scaleToHeight) {
					scaleX = (float)dimensionHelper.realX(scaledWidth) / origW;// (float)videoOutputInfo.getWidth() / origW;
					scaleY = (float)dimensionHelper.realY(scaledHeight) / origH; // (float)videoOutputInfo.getHeight() / origH;
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
				float translateX = centeredHorizontal ? ((float)(videoOutputInfo.getWidth() - (image.getWidth() * scaleX))) / 2f : dimensionHelper.realX(upperLeftCornerX);
				float translateY =   centeredVertical ? ((float)(videoOutputInfo.getHeight() -(image.getHeight()* scaleY))) / 2f : dimensionHelper.realY(upperLeftCornerY);

				translateX = translateX + deltaX;
				deltaX += moveX;
				translateY = translateY + deltaY;
				deltaY += moveY;

				
				AffineTransform transform = new AffineTransform(scaleX, 0, 0, scaleY, translateX,translateY);
				//System.err.println(transform);
//				if(imageUpdating) {
//					graphics2D.setColor(Color.BLACK);
//					graphics2D.drawRect(x, y, i.getWidth(), i.getHeight());
//				}
//				else {
				final AffineTransform saveAT = graphics2D.getTransform();
				try {			
					graphics2D.transform(transform);
					//graphics2D.scale(scaleX, scaleY);
					graphics2D.drawImage(image, 0, 0, null, null);
				}
				finally {
					graphics2D.setTransform(saveAT);
				}
			}
		}
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		this.videoOutputInfo = videoOutputInfo;
		this.dimensionHelper = new DimensionHelper(videoOutputInfo);
//		final String oh = videoOutputInfo.getWidth() + "=" + videoOutputInfo.getHeight();
//		if(!oh.equals(outputHash)) {
//			if(images != null) {
//				resizeImages();
//			}
//			outputHash = oh;
//			imageIndex = 0;
//		}
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
//			final String sc = scaledWidth + "=" + scaledHeight;
//			boolean allNew = scaledImages == null || scaledImages.length != images.length || !sc.equals(scaled);
//			if(allNew) {
//				scaledImages = new BufferedImage[images.length];
//				imageUpdating = true;
//				scaled = sc;
//				resizeImages();
//				iconImage = images[0].getScaledInstance(widthPx, heightPx, java.awt.Image.SCALE_SMOOTH);
//				imageUpdating = false;
//			}
			iconImage = images[0].getScaledInstance(widthPx, heightPx, java.awt.Image.SCALE_SMOOTH);
			graphics.drawImage(iconImage, 0, 0, null, null);
			graphics.drawString(images.length + " images", 4, heightPx - 2);
		
		}
	}
	
	private void resizeImages() {
//		for(int i = 0; i < images.length; i++) {
//			BufferedImage image = images[i];
//			final boolean scaleToWidth = scaledWidth != 0;
//			final boolean scaleToHeight = scaledHeight != 0;
//			final int width = scaledWidth == 0 ? videoOutputInfo.getWidth() : dimensionHelper.realX(scaledWidth);
//			final int height = scaledHeight == 0 ? videoOutputInfo.getHeight() : dimensionHelper.realY(scaledHeight);
//			float ratio = (float)image.getWidth() / (float)image.getHeight();
//			int w = scaleToWidth ? width : scaleToHeight ? (int)((float)height * ratio) : image.getWidth();
//			int h = scaleToHeight ? height : scaleToWidth ? (int)((float)width / ratio) : image.getHeight();
//			BufferedImage scaledImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
//			int wDraw = scaleToWidth ? width : -1;
//			int hDraw = scaleToHeight ? height : -1;
//			scaledImage.getGraphics().drawImage(image.getScaledInstance(wDraw, hDraw, java.awt.Image.SCALE_SMOOTH), 0, 0, Color.BLACK, null);
//			scaledImages[i] = scaledImage;
//		}
	}

}
