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
package org.mcuosmipcuter.orcc.soundvis.model;

import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;

/**
 * Bean implementation of {@link VideoOutputInfo}
 * @author Michael Heinzelmann
 */
public class VideoOutputInfoImpl implements VideoOutputInfo {
	private int framesPerSecond;
	private int width;
	private int height;
	private String title;
	
	public VideoOutputInfoImpl() {
	}

	public VideoOutputInfoImpl(int framesPerSecond, int width, int height) {
		this.framesPerSecond = framesPerSecond;
		this.width = width;
		this.height = height;
	}

	@Override
	public int getFramesPerSecond() {
		return framesPerSecond;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	public void setFramesPerSecond(int framesPerSecond) {
		this.framesPerSecond = framesPerSecond;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + framesPerSecond;
		result = prime * result + height;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		VideoOutputInfoImpl other = (VideoOutputInfoImpl) obj;
		if (framesPerSecond != other.framesPerSecond)
			return false;
		if (height != other.height)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (width != other.width)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VideoOutputInfoImpl [fps=" + framesPerSecond + ", w=" + width + ", h=" + height
				+ ", title=" + title + "]";
	}

}
