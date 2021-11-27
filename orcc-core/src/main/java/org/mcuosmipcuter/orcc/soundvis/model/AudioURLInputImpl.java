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
 * Implementation of a URL based audio input
 * @author Michael Heinzelmann
 */
public class AudioURLInputImpl implements AudioInput {
	
	private final AudioInputInfo audioInputInfo;
	private final URL url;
	private byte[] data;
	
	/**
	 * Creates an audio input instance from the given URL
	 * and extracts header information {@link AudioFormat}.
	 * Invalid files caused by programming or build error
	 * are rejected with a {@link RuntimeException} wrapping the cause.
	 * @param url the url of the audio file
	 */
	public AudioURLInputImpl(URL url) {
		this.url = url;
		long frameLength;
		AudioInputInfo audioInputInfoTemp = null;
		try{
			SoundReader sr = (SoundReader) Class.forName("org.mcuosmipcuter.orcc.ert.humble_video.AudiImportHelper")
					.getDeclaredConstructor().newInstance();

			data = sr.readSound(url.toString());
			
			frameLength = data.length / 4;
			AudioFormat audioFormat = new AudioFormat(22050, 16, 2, true, false); // from humble
			audioInputInfoTemp = new AudioInputInfoImpl(audioFormat, frameLength, AudioLayout.COMPRESSED);
			IOUtil.log("loaded with humble: " + audioInputInfoTemp);
		}
		catch(Exception ex) {
			IOUtil.log("humble could not read audio: " + ex);
		}
		if(data == null) {
			// try java audio
			InputStream is = null;
			AudioInputStream ais = null;
			try {
				is = url.openStream();
				BufferedInputStream buf = new BufferedInputStream(is);
				ais = AudioSystem.getAudioInputStream(buf);
				AudioFormat audioFormat = ais.getFormat();
				frameLength = ais.getFrameLength();
				audioInputInfoTemp = new AudioInputInfoImpl(audioFormat, frameLength, AudioLayout.LINEAR);
				data = ais.readAllBytes();
				IOUtil.log("loaded with java audio: " + audioInputInfoTemp);
			}
			catch(Exception ex) {
				ex.printStackTrace();
				throw new RuntimeException(url + "\n" + ex);
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
		return url.toString();
	}

	@Override
	public Type getType() {
		return Type.STREAM;
	}

	@Override
	public AudioInputStream getAudioStream() {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		AudioInputStream ais = new AudioInputStream(bis, audioInputInfo.getAudioFormat(), audioInputInfo.getFrameLength());
		return ais;
	}

	@Override
	public AudioInputInfo getAudioInputInfo() {
		return audioInputInfo;
	}

	public static URL getUrl(String path) {
		return AudioURLInputImpl.class.getResource(path);
	}
	
}
