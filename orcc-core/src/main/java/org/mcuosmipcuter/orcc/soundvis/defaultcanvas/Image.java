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
public class Image implements SoundCanvas {
	
	@TimedChange
	@UserProperty(description="image to show")
	private BufferedImage image = null;
	
	@UserProperty(description="upper left x position")
	private int upperLeftCornerX = 0;
	@UserProperty(description="to center horizontally")
	private boolean centeredHorizontal = true;
	
	@UserProperty(description="upper left y position")
	private int upperLeftCornerY = 0;
	@UserProperty(description="to center vertically")
	private boolean centeredVertical = true;
	
	@TimedChange
	@LimitedIntProperty(minimum=0, description="width cannot be lower than 0")
	@UserProperty(description="width of image, 0 means use video width")
	private int scaledWidth = 0;
	
	@TimedChange
	@LimitedIntProperty(minimum=0, description="height cannot be lower than 0")
	@UserProperty(description="height of image, 0 means use video height")
	private int scaledHeight = 0;
	
	private DimensionHelper dimensionHelper;
	private BufferedImage scaledImage;
	private java.awt.Image iconImage;
	boolean imageUpdating;
	private int imageHashCode;
	private String scaled;
	private String outputHash;
	VideoOutputInfo videoOutputInfo;

	@Override
	public void nextSample(int[] amplitudes) {
		
	}

	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {
		if(image != null) {	
			BufferedImage i = (scaledWidth != 0 || scaledHeight != 0) ? scaledImage : image;
			final int x = centeredHorizontal ? (videoOutputInfo.getWidth() - i.getWidth()) / 2 : dimensionHelper.realX(upperLeftCornerX);
			final int y = centeredVertical ? (videoOutputInfo.getHeight() - i.getHeight()) / 2 : dimensionHelper.realX(upperLeftCornerY);
			if(imageUpdating) {
				graphics2D.setColor(Color.BLACK);
				graphics2D.drawRect(x, y, i.getWidth(), i.getHeight());
			}
			else {
				graphics2D.drawImage(i, x, y, null, null);
			}
		}
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		this.videoOutputInfo = videoOutputInfo;
		this.dimensionHelper = new DimensionHelper(videoOutputInfo);
		final String oh = videoOutputInfo.getWidth() + "=" + videoOutputInfo.getHeight();
		if(!oh.equals(outputHash)) {
			if(image != null) {
				resizeImage();
			}
			outputHash = oh;
		}
		
	}

	@Override
	public void postFrame() {
		// nothing to reset
	}

	@Override
	public void updateUI(int widthPx, int heightPx, Graphics2D graphics) {
		if(image != null) {

			final int hc = System.identityHashCode(image);
			final String sc = scaledWidth + "=" + scaledHeight;

			if(hc != imageHashCode || !sc.equals(scaled)) {
//				BufferedImage i = (scaledWidth != 0 || scaledHeight != 0) ? scaledImage : image;
//				final int x = centeredHorizontal ? (videoOutputInfo.getWidth() - i.getWidth()) / 2 : dimensionHelper.realX(upperLeftCornerX);
//				final int y = centeredVertical ? (videoOutputInfo.getHeight() - i.getHeight()) / 2 : dimensionHelper.realX(upperLeftCornerY);
//				graphics.drawString("IMAE UPDAT", x, y);
				imageUpdating = true;
				resizeImage();
				imageHashCode = hc;
				scaled = sc;
				iconImage = image.getScaledInstance(widthPx, heightPx, java.awt.Image.SCALE_SMOOTH);
				imageUpdating = false;
			}
			graphics.drawImage(iconImage, 0, 0, null, null);
		}
	}
	
	private void resizeImage() {
		final boolean scaleToWidth = scaledWidth != 0;
		final boolean scaleToHeight = scaledHeight != 0;
		final int width = scaledWidth == 0 ? videoOutputInfo.getWidth() : dimensionHelper.realX(scaledWidth);
		final int height = scaledHeight == 0 ? videoOutputInfo.getHeight() : dimensionHelper.realY(scaledHeight);
		float ratio = (float)image.getWidth() / (float)image.getHeight();
		int w = scaleToWidth ? width : scaleToHeight ? (int)((float)height * ratio) : image.getWidth();
		int h = scaleToHeight ? height : scaleToWidth ? (int)((float)width / ratio) : image.getHeight();
		scaledImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		int wDraw = scaleToWidth ? width : -1;
		int hDraw = scaleToHeight ? height : -1;
		scaledImage.getGraphics().drawImage(image.getScaledInstance(wDraw, hDraw, java.awt.Image.SCALE_SMOOTH), 0, 0, Color.BLACK, null);

	}

}
