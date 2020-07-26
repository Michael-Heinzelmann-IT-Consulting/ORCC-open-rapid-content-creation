/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2020 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.soundvis.util;

import java.awt.image.BufferedImage;

import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * @author Michael Heinzelmann
 */
public class ImageUtil {

	/**
	 * Quadrant rotates an image returning a new Image with the rotation applied.
	 * @param origImage image to rotate
	 * @param quadrant any integer, number will be normalized to its modulus 4, positive numbers rotate clockwise
	 * @return a new rotated image or if rotation evaluates to 0 the original
	 */
	public static BufferedImage quadrantRotate(BufferedImage origImage, int quadrant) {

		if (origImage == null) {
			IOUtil.log("WARN: image null, returning null");
			return null;
		}
		
		int origWidth = origImage.getWidth(null);
		int origHeight = origImage.getHeight(null);
		IOUtil.log("image w: " + origWidth + " h: " + origHeight);
		
		int q = quadrant % 4;
		
		q = q < 0 ? 4 - q : q;
		
		if(q == 0) {
			return origImage;
		}
		
		int newWidth = q == 2 ? origWidth : origHeight;
		int newHeight = q == 2 ? origHeight : origWidth;


		BufferedImage newImage = new BufferedImage(newWidth, newHeight, origImage.getType());

		for (int origX = 0; origX < origWidth; origX++) {
			for (int origY = 0; origY < origHeight; origY++) {
				int rgb = origImage.getRGB(origX, origY);
				int newX;
				int newY;
				switch(q) {
				case 1:
					newX = origHeight - 1 - origY;
					newY = origX;
					break;
				case 2:
					newX = origWidth - 1 - origX;
					newY = origHeight - 1 - origY;
					break;
				case 3:
					newX = origY;
					newY = origWidth - 1 - origX;
					break;
					default:
						throw new IllegalStateException();
				}
				newImage.setRGB(newX, newY, rgb);
			}
		}

		return newImage;
	}
	/**
	 * Mirrors an image around the Y axis
	 * @param origImage the image to mirror
	 * @return a new image mirrored Y axis
	 */
	public static BufferedImage mirrorY(BufferedImage origImage) {

		if (origImage == null) {
			IOUtil.log("WARN: image null, returning null");
			return null;
		}
		int width = origImage.getWidth(null);
		int height = origImage.getHeight(null);
		IOUtil.log("image w: " + width + " h: " + height);

		BufferedImage newImage = new BufferedImage(width, height, origImage.getType());
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				newImage.setRGB(w, h, origImage.getRGB(width - 1 - w, h));
			}
		}

		return newImage;

	}
}
