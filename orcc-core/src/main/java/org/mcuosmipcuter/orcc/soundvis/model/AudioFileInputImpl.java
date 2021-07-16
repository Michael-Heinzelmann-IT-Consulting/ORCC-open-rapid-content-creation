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
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.AudioLayout;
import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.SoundReader;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Implementation of a file based audio input
 * @author Michael Heinzelmann
 */
public class AudioFileInputImpl implements AudioInput {
	
	private byte[] data; // optional expansion area for compressed input
	private final AudioInputInfo audioInputInfo;
	private final String audioFileName;
	
	/**
	 * Creates an audio input instance from the given file name,
	 * and extracts header information {@link AudioFormat}.
	 * Invalid files (non existing, not readable, not supported audio etc.)
	 * are rejected with a {@link RuntimeException} wrapping the cause.
	 * @param audioFileName the full path to the audio file
	 */
	public AudioFileInputImpl(String audioFileName) {
		this.audioFileName = audioFileName;
		long frameLength;
		AudioInputInfo audioInputInfoTemp = null;
		try {
			SoundReader sr = (SoundReader) Class.forName("org.mcuosmipcuter.orcc.ert.humble_video.AudiImportHelper")
					.getDeclaredConstructor().newInstance();

			byte[] data = sr.readSound(audioFileName);
			this.data = data;

			frameLength = data.length / 4;
			AudioFormat audioFormat = new AudioFormat(22050, 16, 2, true, false); // from humble
			audioInputInfoTemp = new AudioInputInfoImpl(audioFormat, frameLength, AudioLayout.COMPRESSED);
		} catch (Exception e) {
			IOUtil.log("could not open as audio input: " + e.getMessage());

			FileInputStream fis = null;
			AudioInputStream ais = null;
			try {
				fis = new FileInputStream(audioFileName);
				BufferedInputStream buf = new BufferedInputStream(fis);
				ais = AudioSystem.getAudioInputStream(buf);
				AudioFormat audioFormat = ais.getFormat();
				frameLength = ais.getFrameLength();
				audioInputInfoTemp = new AudioInputInfoImpl(audioFormat, frameLength, AudioLayout.LINEAR);
				try {
					data = ais.readAllBytes();
				}
				catch(OutOfMemoryError oom) {
					data = null;
					IOUtil.log("not enogh buffer for " + audioFileName + " " + oom.getMessage());
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			} finally {
				IOUtil.safeClose(fis);
				IOUtil.safeClose(ais);
			}

		}
		this.audioInputInfo = audioInputInfoTemp;
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
	public AudioInputStream getAudioStream() {
		if(data != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			AudioInputStream ais = new AudioInputStream(bis, audioInputInfo.getAudioFormat(), audioInputInfo.getFrameLength());
			return ais;
		}
		FileInputStream fis;
		try {
			fis = new FileInputStream(audioFileName);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		try {
			BufferedInputStream buf = new BufferedInputStream(fis);
			return AudioSystem.getAudioInputStream(buf);
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
