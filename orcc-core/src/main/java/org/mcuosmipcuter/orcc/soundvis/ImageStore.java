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
package org.mcuosmipcuter.orcc.soundvis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.mcuosmipcuter.orcc.api.util.TextHelper;
import org.mcuosmipcuter.orcc.soundvis.util.ImageUtil;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Shared In Memory store for loaded Images allowing multiple references to an image
 * avoiding duplicate loading and storage. 
 * @author Michael Heinzelmann
 *
 */
public class ImageStore {
	
	public static class Key{
		private final long lastModified; //  to detect changes
		private final String absolutePath;
		private final int quadrantRotation;
		private final boolean mirrored;
		private final int width;
		private final int height;
		
		public Key(File imageFile) {
			this(imageFile.lastModified(), imageFile.getAbsolutePath(), 0, false, 0, 0);
		}

		public Key(long lastModified, String absolutePath, int quadrantRotation, boolean mirrored, int width, int height) {
			this.lastModified = lastModified;
			this.absolutePath = absolutePath;
			this.quadrantRotation = quadrantRotation;
			this.mirrored = mirrored;
			this.width = width;
			this.height = height;
		}
		public Key toOriginal() {
			return new Key(this.lastModified, this.absolutePath, 0, false, 0, 0);
		}
		public Key toUntransformed() {
			return new Key(this.lastModified, this.absolutePath, 0, false, this.width, this.height);
		}
		public Key mirrorY() {
			boolean newMirror = ! this.isMirrored();
			return new Key(this.getLastModified(), this.getAbsolutePath(), this.getQuadrantRotation(), newMirror, this.getWidth(), this.getHeight());
		}
		public Key rotateClockWise() {
			int oldRotation = this.getQuadrantRotation();
			int newRotation = this.isMirrored() ? ((oldRotation == 0) ? 3 : oldRotation - 1 ) : (this.getQuadrantRotation() + 1) % 4;
			return new Key(this.getLastModified(), this.getAbsolutePath(), newRotation, this.isMirrored(), this.getWidth(), this.getHeight());
	
		}
		@Override
		public String toString() {
			return "Key [lastModified=" + lastModified + ", absolutePath=" + absolutePath + ", quadrantRotation="
					+ quadrantRotation + ", mirrored=" + mirrored + ", width=" + width + ", height=" + height + "]";
		}
		

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((absolutePath == null) ? 0 : absolutePath.hashCode());
			result = prime * result + height;
			result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
			result = prime * result + (mirrored ? 1231 : 1237);
			result = prime * result + quadrantRotation;
			result = prime * result + width;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (absolutePath == null) {
				if (other.absolutePath != null)
					return false;
			} else if (!absolutePath.equals(other.absolutePath))
				return false;
			if (height != other.height)
				return false;
			if (lastModified != other.lastModified)
				return false;
			if (mirrored != other.mirrored)
				return false;
			if (quadrantRotation != other.quadrantRotation)
				return false;
			if (width != other.width)
				return false;
			return true;
		}

		public long getLastModified() {
			return lastModified;
		}

		public String getAbsolutePath() {
			return absolutePath;
		}

		public int getQuadrantRotation() {
			return quadrantRotation;
		}

		public boolean isMirrored() {
			return mirrored;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
		
	}
	
	private static Map<Key, SoftReference<BufferedImage>> store = new HashMap<Key, SoftReference<BufferedImage>>();
	private static Map<Key, SoftReference<Image>> storeScaled = new HashMap<Key, SoftReference<Image>>();
	public static boolean contains(Key key) {
		return store.containsKey(key);
	}
	public static BufferedImage getImage(Key key) {

		SoftReference<BufferedImage> ref = store.get(key);
		if(ref != null && ref.get() != null) {
			IOUtil.log("*hit for " + key + " image " + ref.get());
		}
		else {
			IOUtil.log("miss for " + key);
		}
		return ref != null ? ref.get() : null;
	}
	public static BufferedImage getOrLoadImage(Key key) {
		BufferedImage fromStore = getImage(key);
		if (fromStore != null) {
			return fromStore;
		} else {
			BufferedImage image = null;
			File imageFile = new File(key.getAbsolutePath());
			if (imageFile.exists()) {
				try {
					image = ImageIO.read(imageFile);
				} catch (Exception ex) {
					IOUtil.log("problem loading image for " + key.getAbsolutePath() + ": " + ex.getMessage());
				}
			} else {
				IOUtil.log(key.getAbsolutePath() + " does not exist.");
			}
			if (image == null) {
				image = createPlaceHolderImage(imageFile);
			}
			Key originalKey = key.toOriginal();
			if(getImage(originalKey) == null) {
				addImage(originalKey, image);
			}
			if (key.quadrantRotation != 0) {
				image = ImageUtil.quadrantRotate(image, key.quadrantRotation);
			}
			if (key.mirrored) {
				image = ImageUtil.mirrorY(image);
			}	
			
			addImage(key, image);
			return image;

		}
	}
	public static Image getOrLoadScaledImage(Key originalKey, int newWidth, int newHeight) {
		Key newKey = new Key(originalKey.lastModified, originalKey.absolutePath, originalKey.quadrantRotation, originalKey.mirrored, newWidth, newHeight);
		SoftReference<Image> ref = storeScaled.get(newKey);
		IOUtil.log((ref != null && ref.get() != null ? "*hit" : "miss") + " for scaled " + newKey);
		if(ref != null && ref.get() != null) {
			return ref.get();
		}
		BufferedImage  original = getOrLoadImage(originalKey);
		Image  scaled = null;
		if(original != null) {;
			scaled = original.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
			storeScaled.putIfAbsent(newKey, new SoftReference<Image>(scaled));
		}
		return scaled;
	}
	
	public static void addImage(Key key, BufferedImage image) {
		store.put(key, new SoftReference<BufferedImage>(image));
	}
	
	public static BufferedImage transformImage(Key newKey) {
		BufferedImage newImage = getImage(newKey);
		if(newImage != null) {
			return newImage;
		}
		else {
			Key untransformedKey = newKey.toUntransformed();
			
			BufferedImage untransformedImage = getImage(untransformedKey);
			if(untransformedImage == null) {
				IOUtil.log("returning null, untransformed key not stored " + untransformedKey);
				return null;
			}
			if(newKey.equals(untransformedKey)) {
				return untransformedImage;
			}

			newImage = ImageUtil.quadrantRotate(untransformedImage, newKey.quadrantRotation);
			
			if(newKey.mirrored) {
				newImage = ImageUtil.mirrorY(newImage);
			}

			addImage(newKey, newImage);
			return newImage;
		}	
	}
	public static BufferedImage createPlaceHolderImage(File file) {
		int width = 600;
		int height = 400;
		BufferedImage placeholderImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = placeholderImage.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);

		TextHelper.writeText(file.getName(), graphics, height / 10, Color.BLUE, width, height / 2);
		
		return placeholderImage;
	}

}
