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
package org.mcuosmipcuter.orcc.ert.humble_video;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.sound.sampled.LineUnavailableException;

import org.mcuosmipcuter.orcc.soundvis.SoundReader;
import org.mcuosmipcuter.orcc.util.IOUtil;

import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.MediaAudio;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.javaxsound.MediaAudioConverter;
import io.humble.video.javaxsound.MediaAudioConverterFactory;

/**
 * @author Michael Heinzelmann
 * @see https://github.com/artclarke/humble-video/blob/master/humble-video-demos/src/main/java/io/humble/video/demos/DecodeAndPlayAudio.java
 */
public class AudiImportHelper implements SoundReader {

	/**
	 * Tries to read a coded (compressed) audio file and returns a byte array in
	 * linear format
	 * 
	 * @throws if file cannot be decoded a) codec not supported b) file is linear
	 *            format c) other reasons
	 */
	public byte[] readSound(String filename) throws InterruptedException, IOException, LineUnavailableException {
		
		JavaURLProtocolHandler.init();

		Demuxer demuxer = null;

		try (ByteArrayOutputStream resultStream = new ByteArrayOutputStream()) {
			demuxer = Demuxer.make();
			demuxer.open(filename, null, false, true, null, null);
			int numStreams = demuxer.getNumStreams();

			/*
			 * Iterate through the streams to find the first audio stream
			 */
			int audioStreamId = -1;
			Decoder audioDecoder = null;
			for (int i = 0; i < numStreams; i++) {
				final DemuxerStream stream = demuxer.getStream(i);
				final Decoder decoder = stream.getDecoder();
				if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_AUDIO) {
					audioStreamId = i;
					audioDecoder = decoder;
					// stop at the first one.
					break;
				}
			}
			if (audioStreamId == -1) {
				throw new RuntimeException("could not find audio stream in container: " + filename);
			}
			audioDecoder.open(null, null);

			/*
			 * We allocate a set of samples with the same number of channels as the coder
			 * tells us is in this buffer.
			 */
			final MediaAudio samples = MediaAudio.make(audioDecoder.getFrameSize(), audioDecoder.getSampleRate(),
					audioDecoder.getChannels(), audioDecoder.getChannelLayout(), audioDecoder.getSampleFormat());
			final MediaAudioConverter converter = MediaAudioConverterFactory
					.createConverter(MediaAudioConverterFactory.DEFAULT_JAVA_AUDIO, samples);

			ByteBuffer rawAudio = null;

			final MediaPacket packet = MediaPacket.make();
			while (demuxer.read(packet) >= 0) {
				/*
				 * Now we have a packet, let's see if it belongs to our audio stream
				 */
				if (packet.getStreamIndex() == audioStreamId) {
					int offset = 0;
					int bytesRead = 0;
					do {
						bytesRead += audioDecoder.decode(samples, packet, offset);
						if (samples.isComplete()) {
							rawAudio = converter.toJavaAudio(rawAudio, samples);
							resultStream.write(rawAudio.array());
						}
						offset += bytesRead;
					} while (offset < packet.getSize());
				}
			}

			// flush
			do {
				audioDecoder.decode(samples, null, 0);
				if (samples.isComplete()) {
					rawAudio = converter.toJavaAudio(rawAudio, samples);
					resultStream.write(rawAudio.array());
				}
			} while (samples.isComplete());

			return resultStream.toByteArray();
		} finally {
			if (demuxer != null) {
				try {
					demuxer.close();
				}
				catch (Exception e) {
					IOUtil.log("error closing demuxer: " + e.getMessage());
				}
			}
		}

	}

}
