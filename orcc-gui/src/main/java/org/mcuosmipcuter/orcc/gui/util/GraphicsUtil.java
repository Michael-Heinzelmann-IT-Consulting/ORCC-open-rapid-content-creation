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
package org.mcuosmipcuter.orcc.gui.util;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * @author user
 */
public class GraphicsUtil {
	
	/**
	 * @param parent component
	 * @return
	 */
	public static Component getPointForChildWindow(Component parent, Component child) {
		Point point = parent.getLocationOnScreen();
		Component par= parent.getParent();
		Component frame = null;
		while((par = par.getParent()) != null){
			frame = par;
		}
		Rectangle screen = frame.getBounds(); //GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		Rectangle childBounds = child.getBounds();
		if(screen.contains(childBounds)){
			//return point;
		}
		else {
			Rectangle intersect = screen.intersection(childBounds);
			int dx = childBounds.x - intersect.x;
			int dy = childBounds.y - intersect.y;
			point.x -= dx;
			point.y -= dy;
			System.err.println("dx: " + dx + " dy:" + dy);
		}
		return frame;
	}
	public static Image getScaledInstanceKeepRatio(Image image, int size, int hints){
		float ratio = image.getWidth(null) == 0 ? 1f : (float)image.getHeight(null) / (float)image.getWidth(null);
		int w;
		int h;
		if(ratio < 1) {
			w = size;
			h = Math.round( size * ratio);
		}
		else {
			w = Math.round( size / ratio);
			h = size;
		}
		return image.getScaledInstance(w, h, hints);
		
	}
	
	public static BufferedImage rotateClockwise(BufferedImage img) {

		if (img == null) {
			IOUtil.log("WARN: image null, returning null");
			return null;
		}
		int width = img.getWidth(null);
		int height = img.getHeight(null);
		IOUtil.log("image w: " + width + " h: " + height);

		BufferedImage newImage = new BufferedImage(height, width, img.getType());

		for (int w = 0; w < width; w++) {
			for (int h = 0; h < height; h++) {
				newImage.setRGB(height - 1 - h, w, img.getRGB(w, h));
			}
		}

		return newImage;
	}
	
	public static BufferedImage mirrorX(BufferedImage img) {

		if (img == null) {
			IOUtil.log("WARN: image null, returning null");
			return null;
		}
		int width = img.getWidth(null);
		int height = img.getHeight(null);
		IOUtil.log("image w: " + width + " h: " + height);

		BufferedImage newImage = new BufferedImage(width, height, img.getType());
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				newImage.setRGB(w, h, img.getRGB(width - 1 - w, h));
			}
		}

		return newImage;

	}
}
