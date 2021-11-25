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
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.AudioLayout;
import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.SoundReader;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Implementation of a classpath based audio input
 * @author Michael Heinzelmann
 */
public class AudioClasspathInputImpl implements AudioInput {
	
	private final AudioInputInfo audioInputInfo;
	private final String audioResourcePath;
	private byte[] data;
	
	/**
	 * Creates an audio input instance from the given resource name,
	 * and extracts header information {@link AudioFormat}.
	 * Invalid files caused by programming or build error
	 * are rejected with a {@link RuntimeException} wrapping the cause.
	 * @param audioFileName the full path to the audio file
	 */
	public AudioClasspathInputImpl(String audioResourcePath) {
		this.audioResourcePath = audioResourcePath;
		InputStream is = null;
		AudioInputStream ais = null;
		long frameLength;
		AudioInputInfo audioInputInfoTemp = null;
		try{
			Class.forName("org.mcuosmipcuter.orcc.ert.humble_video.JavaURLProtocolHandler")
			.getDeclaredConstructor().newInstance();
			SoundReader sr = (SoundReader) Class.forName("org.mcuosmipcuter.orcc.ert.humble_video.AudiImportHelper")
					.getDeclaredConstructor().newInstance();

			data = sr.readSound(audioResourcePath);
			
			frameLength = data.length / 4;
			AudioFormat audioFormat = new AudioFormat(22050, 16, 2, true, false); // from humble
			audioInputInfoTemp = new AudioInputInfoImpl(audioFormat, frameLength, AudioLayout.COMPRESSED);
		}
		catch(Exception ex) {
				// try java audio
		
		try {
			URL url = new URL(audioResourcePath);
			is = url.openStream();
			//is = AudioClasspathInputImpl.class.getResourceAsStream(audioResourcePath);
			BufferedInputStream buf = new BufferedInputStream(is);
			ais = AudioSystem.getAudioInputStream(buf);
			AudioFormat audioFormat = ais.getFormat();
			 frameLength = ais.getFrameLength();
			 audioInputInfoTemp = new AudioInputInfoImpl(audioFormat, frameLength, AudioLayout.LINEAR);
		}
		catch(Exception ex1) {
			ex1.printStackTrace();
			throw new RuntimeException(audioResourcePath + "\n" + ex1);
		}
		finally {

			IOUtil.safeClose(is);
			IOUtil.safeClose(ais);
		}
		}
		audioInputInfo = audioInputInfoTemp;
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
		if(data != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			AudioInputStream ais = new AudioInputStream(bis, audioInputInfo.getAudioFormat(), audioInputInfo.getFrameLength());
			return ais;
		}
		InputStream is;
		try {
			URL url = new URL(audioResourcePath);
			
			is = url.openStream();
			//is = AudioClasspathInputImpl.class.getResourceAsStream(audioResourcePath);
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

	public static URL getUrl(String path) {
		return AudioClasspathInputImpl.class.getResource(path);
	}
	
}
