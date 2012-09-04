/**
* Copyright 2012 Michael Heinzelmann IT-Consulting
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
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
