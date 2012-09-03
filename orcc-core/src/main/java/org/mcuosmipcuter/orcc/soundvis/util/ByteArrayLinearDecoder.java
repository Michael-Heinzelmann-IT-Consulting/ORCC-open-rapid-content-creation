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

		while(keepReading && ais.read(barr, 0, chunkSize) != -1) {
			int[] amplitudes = ByteArrayLinearDecoder.decodeLinear(barr, channels, words, isBigEndian);
			keepReading = decodingCallback.nextSample(amplitudes, barr);
		}
	}
	
}
