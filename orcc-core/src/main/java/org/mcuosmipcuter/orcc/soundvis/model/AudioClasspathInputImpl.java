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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Implementation of a file based audio input
 * @author Michael Heinzelmann
 */
public class AudioClasspathInputImpl implements AudioInput {
	
	private final AudioInputInfo audioInputInfo;
	private final String audioResourcePath;
	
	/**
	 * Creates an audio input instance from the given resource name,
	 * and extracts header information {@link AudioFormat}.
	 * Invalid files caused by programming or build error
	 * are rejected with a {@link RuntimeException} wrapping the cause.
	 * @param audioFileName the full path to the audio file
	 */
	public AudioClasspathInputImpl(String audioResourcePath) {
		InputStream is = null;
		AudioInputStream ais = null;
		try{
			AudioClasspathInputImpl.class.getResource(audioResourcePath);
			
			is = AudioClasspathInputImpl.class.getResourceAsStream(audioResourcePath);
			BufferedInputStream buf = new BufferedInputStream(is);
			ais = AudioSystem.getAudioInputStream(buf);
			AudioFormat audioFormat = ais.getFormat();
			long frameLength = ais.getFrameLength();
			audioInputInfo = new AudioInputInfoImpl(audioFormat, frameLength);
			this.audioResourcePath = audioResourcePath;
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		finally {
			IOUtil.safeClose(is);
			IOUtil.safeClose(ais);
		}
	}

	/**
	 * Returns the file name as given in the constructor
	 */
	@Override
	public String getName() {
		return audioResourcePath;
	}

	@Override
	public Type getType() {
		return Type.STREAM;
	}

	@Override
	public AudioInputStream getAudioStream() {
		InputStream is;
		try {
			is = AudioClasspathInputImpl.class.getResourceAsStream(audioResourcePath);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		try {
			BufferedInputStream buf = new BufferedInputStream(is);
			return AudioSystem.getAudioInputStream(buf);
		}
		catch(Exception ex) {
			// resource existed but was no (supported) audio file - programming or build error
			IOUtil.safeClose(is); 
			throw new RuntimeException(ex);
		}
	}

	@Override
	public AudioInputInfo getAudioInputInfo() {
		return audioInputInfo;
	}

	
}
