/**
 * 
 */
package org.mcuosmipcuter.orcc.soundvis;

import java.awt.Image;
import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Shared In Memory store for loaded Images allowing multiple references to an image
 * avoiding duplicate loading and storage. 
 * @author Michael Heinzelmann
 *
 */
public class ImageStore {
	
	public static class Key{
		private long lastModified; //  to detect changes
		private String absolutePath; 
		
		private Key(long lastModified, String absolutePath) {
			this.lastModified = lastModified;
			this.absolutePath = absolutePath;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((absolutePath == null) ? 0 : absolutePath.hashCode());
			result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
			return result;
		}

		@Override
		public String toString() {
			return "Key [lastModified=" + lastModified + ", absolutePath=" + absolutePath + "]";
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
			return true;
		}

		
	}
	
	private static Map<Key, SoftReference<Image>> store = new HashMap<Key, SoftReference<Image>>();
	public static boolean contains(Key key) {
		return store.containsKey(key);
	}
	public static Image getImage(File imageFile) {
		System.err.println(store);
		Key key = new Key(imageFile.lastModified(), imageFile.getAbsolutePath());
		SoftReference<Image> ref = store.get(key);
		if(ref != null) {
			System.err.println(key + " ######## " + ref.get());
		}
		return ref != null ? ref.get() : null;
	}

	public static Key addImage(File imageFile, Image image) {
		Key key = new Key(imageFile.lastModified(), imageFile.getAbsolutePath());
		store.put(key, new SoftReference<Image>(image));
		return key;
	}
}
