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

import javax.sound.sampled.AudioFormat;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;

/**
 * Bean implementation of {@link AudioInputInfo}
 * @author Michael Heinzelmann
 */
public class AudioInputInfoImpl implements AudioInputInfo {
	
	private final AudioFormat audioFormat;
	private final long frameLength;
	
	public AudioInputInfoImpl(AudioFormat audioFormat, long frameLength) {
		this.audioFormat = audioFormat;
		this.frameLength = frameLength;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	@Override
	public long getFrameLength() {
		return frameLength;
	}

}
