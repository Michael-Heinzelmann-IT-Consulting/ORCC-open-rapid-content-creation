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
package org.mcuosmipcuter.orcc.soundvis.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mcuosmipcuter.orcc.soundvis.AudioInput;

/**
 * Bean conforming class
 * @author user
 *
 */
public class PersistentSession implements Serializable {
	
	/**
	 * version
	 */
	private static final long serialVersionUID = 1L;
	
	private List<PersistentSoundCanvasWrapper> soundCanvasList = new ArrayList<PersistentSoundCanvasWrapper>();
	private AudioInput.Type audioInputType;
	private String audioInputName;
	private int videoOutPutHeight;
	private int videoOutPutWidth;
	private int videoOutPutFrames;

	/**
	 * 
	 */
	public PersistentSession() {
		// bean
	}

	public List<PersistentSoundCanvasWrapper> getSoundCanvasList() {
		return soundCanvasList;
	}

	public void setSoundCanvasList(List<PersistentSoundCanvasWrapper> soundCanvasList) {
		this.soundCanvasList = soundCanvasList;
	}

	public AudioInput.Type getAudioInputType() {
		return audioInputType;
	}

	public void setAudioInputType(AudioInput.Type audioInputType) {
		this.audioInputType = audioInputType;
	}

	public String getAudioInputName() {
		return audioInputName;
	}

	public void setAudioInputName(String audioInputName) {
		this.audioInputName = audioInputName;
	}

	public int getVideoOutPutHeight() {
		return videoOutPutHeight;
	}

	public void setVideoOutPutHeight(int videoOutPutHeight) {
		this.videoOutPutHeight = videoOutPutHeight;
	}

	public int getVideoOutPutWidth() {
		return videoOutPutWidth;
	}

	public void setVideoOutPutWidth(int videoOutPutWidth) {
		this.videoOutPutWidth = videoOutPutWidth;
	}

	public int getVideoOutPutFrames() {
		return videoOutPutFrames;
	}

	public void setVideoOutPutFrames(int videoOutPutFrames) {
		this.videoOutPutFrames = videoOutPutFrames;
	}


}
