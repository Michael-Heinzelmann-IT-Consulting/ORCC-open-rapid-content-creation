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
package org.mcuosmipcuter.orcc.soundvis.defaultcanvas.model;

import java.awt.Image;
import java.beans.Transient;
import java.util.UUID;

import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayObject;
import org.mcuosmipcuter.orcc.soundvis.ImageStore;
import org.mcuosmipcuter.orcc.soundvis.ImageStore.Key;

/**
 * @author Michael Heinzelmann
 *
 */
public class Slide implements DisplayObject {

	private String id = UUID.randomUUID().toString();
	private Image image;
	private String text;
	private int position;
	private Key key;
	

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Transient
	public Image getImage() {
		return image;
	}
	public void setImage(Key key, Image image) {
		this.key = key;
		this.image = image;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public void setKey(Key key) {
		if(image == null) {
			image = ImageStore.getOrLoadImage(key);
		}
		else {
			throw new IllegalStateException("image already set!");
		}
		this.key = key;
	}
	public Key getKey() {
		return key;
	}
	@Override
	public String getDisplayKey() {
		return "slide " + position;
	}
	@Override
	public DisplayDuration<?> getDisplayDuration(long frameFrom, long frameTo) {
		DisplayDuration<Slide> duration = new DisplayDuration<>();
		duration.setDisplayObject(this);
		duration.setFrom(frameFrom);
		duration.setTo(frameTo);
		duration.setOverlapBefore(0);
		duration.setOverlapAfter(0);
		return duration;
	}

	@Override
	public String toString() {
		return "Slide [id=" + id + "image=" + System.identityHashCode(image) + ", text=" + text + ", position=" + position + "]";
	}
	
}
