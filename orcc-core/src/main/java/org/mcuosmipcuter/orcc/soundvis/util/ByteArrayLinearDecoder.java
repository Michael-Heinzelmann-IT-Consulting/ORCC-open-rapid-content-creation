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
package org.mcuosmipcuter.orcc.soundvis.util;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.mcuosmipcuter.orcc.soundvis.DecodingCallback;

/**
 * Byte array decoder for different input formats, the meaning of the bytes has to be liner PCM in all cases.
 * @author Michael Heinzelmann
 */
public class ByteArrayLinearDecoder {
	/**
	 * Decode the given byte array into an unsigned amplitude array
	 * @param source byte array to decode
	 * @param channels number of channels
	 * @param words number of words
	 * @param isBigEndian byte order used
	 * @return decoded unsigned amplitude array
	 */
	public static int[] decodeLinear(byte[] source, final int channels, final int words, final boolean isBigEndian) {
		int[] amplitudes = new int[channels];
		int chunkPos = 0;
		for(int channel = 0; channel < channels; channel++) {

			for(int word = 1; word <= words; word++) {
				if(isBigEndian) {
					int us = 128 + source[chunkPos++];
					amplitudes[channel] += us << ((words - word) * 8);
					
				}
				else {
					int us = 128 + source[chunkPos + words - word];
					amplitudes[channel]  += us << ((words - word) * 8);
				}
			}
			if(!isBigEndian) {
				chunkPos += words;
			}
		}
		return amplitudes;
	}
	/**
	 * Decode the given audio stream using the given callback
	 * @param ais audio input stream
	 * @param decodingCallback call back that will receive the decoded samples
	 * @throws IOException
	 */
	public static void decodeLinear(AudioInputStream ais, DecodingCallback decodingCallback) throws IOException {

		final AudioFormat format = ais.getFormat();
		final int chunkSize =  format.getFrameSize();
		final int  channels = format.getChannels();
		final int words = chunkSize / channels;
		final boolean isBigEndian = format.isBigEndian();

		byte[] barr = new byte[chunkSize];
		boolean keepReading = true;
		long sampleCount = 0;
		while(keepReading && ais.read(barr, 0, chunkSize) != -1) {
			int[] amplitudes = ByteArrayLinearDecoder.decodeLinear(barr, channels, words, isBigEndian);
			keepReading = decodingCallback.nextSample(amplitudes, barr, ++sampleCount);
		}
	}
	
}
