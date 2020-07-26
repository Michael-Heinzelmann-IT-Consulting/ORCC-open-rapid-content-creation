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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

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
		
		public Key(File imageFile) {
			this(imageFile.lastModified(), imageFile.getAbsolutePath(), 0, false);
		}

		public Key(long lastModified, String absolutePath, int quadrantRotation, boolean mirrored) {
			this.lastModified = lastModified;
			this.absolutePath = absolutePath;
			this.quadrantRotation = quadrantRotation;
			this.mirrored = mirrored;
		}

		@Override
		public String toString() {
			return "Key [lastModified=" + lastModified + ", absolutePath=" + absolutePath + ", quadrantRotation="
					+ quadrantRotation + ", mirrored=" + mirrored + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((absolutePath == null) ? 0 : absolutePath.hashCode());
			result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
			result = prime * result + (mirrored ? 1231 : 1237);
			result = prime * result + quadrantRotation;
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
			if (lastModified != other.lastModified)
				return false;
			if (mirrored != other.mirrored)
				return false;
			if (quadrantRotation != other.quadrantRotation)
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


		
	}
	
	private static Map<Key, SoftReference<BufferedImage>> store = new HashMap<Key, SoftReference<BufferedImage>>();
	public static boolean contains(Key key) {
		return store.containsKey(key);
	}
	public static BufferedImage getImage(Key key) {

		//Key key = new Key(imageFile.lastModified(), imageFile.getAbsolutePath());
		SoftReference<BufferedImage> ref = store.get(key);
		if(ref != null) {
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
			try {
				File imageFile = new File(key.getAbsolutePath());
				BufferedImage image = ImageIO.read(imageFile);
				if(image != null) {
					if(key.quadrantRotation != 0) {
						image = ImageUtil.quadrantRotate(image, key.quadrantRotation);
					}
					if(key.mirrored) {
						image = ImageUtil.mirrorY(image);
					}
					addImage(key, image);
				}
				return image;
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	public static void addImage(Key key, BufferedImage image) {
		store.put(key, new SoftReference<BufferedImage>(image));
	}
	
	public static BufferedImage transformImage(Key oldKey, Key newKey) {
		BufferedImage newImage = getImage(newKey);
		if(newImage != null) {
			return newImage;
		}
		else {
			BufferedImage oldImage = getImage(oldKey);
			if(oldImage == null) {
				IOUtil.log("returnin null, old key not stored " + oldKey);
				return null;
			}
			if(newKey.equals(oldKey)) {
				return oldImage;
			}
			if(oldKey.quadrantRotation != newKey.quadrantRotation) {
				newImage = ImageUtil.quadrantRotate(oldImage, newKey.quadrantRotation);
			}
			if(oldKey.mirrored != newKey.mirrored) {
				newImage = ImageUtil.mirrorY(oldImage);
			}
			addImage(newKey, newImage);
			return newImage;
		}	
	}

}
