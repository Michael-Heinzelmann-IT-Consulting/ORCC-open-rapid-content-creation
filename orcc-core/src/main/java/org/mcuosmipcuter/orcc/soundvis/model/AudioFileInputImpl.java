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
import java.io.IOException;

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
public class AudioFileInputImpl implements AudioInput {
	
	private final AudioInputInfo audioInputInfo;
	private final String audioFileName;
	
	private FileInputStream fis;
	private AudioInputStream ais;
	
	/**
	 * Creates an audio input instance from the given file name,
	 * and extracts header information {@link AudioFormat}.
	 * Invalid files (non existing, not readable, not supported audio etc.)
	 * are rejected with a {@link RuntimeException} wrapping the cause.
	 * @param audioFileName the full path to the audio file
	 */
	public AudioFileInputImpl(String audioFileName) {
		FileInputStream fis = null;
		AudioInputStream ais = null;
		try{
			fis = new FileInputStream(audioFileName);
			BufferedInputStream buf = new BufferedInputStream(fis);
			ais = AudioSystem.getAudioInputStream(buf);
			AudioFormat audioFormat = ais.getFormat();
			long frameLength = ais.getFrameLength();
			audioInputInfo = new AudioInputInfoImpl(audioFormat, frameLength);
			this.audioFileName = audioFileName;
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		finally {
			IOUtil.safeClose(fis);
			IOUtil.safeClose(ais);
		}
	}

	@Override
	public void close() throws IOException {
		IOUtil.safeClose(fis);
		IOUtil.safeClose(ais);
	}
	/**
	 * Returns the file name as given in the constructor
	 */
	@Override
	public String getName() {
		return audioFileName;
	}

	@Override
	public Type getType() {
		return Type.FILE;
	}
	/**
	 * Although the constructor checked the wrapped file already to ba a valid audio file, 
	 * runtime exceptions can still occur e.g. if the file has been deleted in the meantime.
	 */
	@Override
	public AudioInputStream open() {
		try {
			fis = new FileInputStream(audioFileName);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		try {
			BufferedInputStream buf = new BufferedInputStream(fis);
			ais = AudioSystem.getAudioInputStream(buf);
			return ais;
		}
		catch(Exception ex) {
			// file existed but was no (supported) audio file, 
			// this is a common error situation, close file stream
			IOUtil.safeClose(fis); 
			throw new RuntimeException(ex);
		}
	}

	@Override
	public AudioInputInfo getAudioInputInfo() {
		return audioInputInfo;
	}

	
}
