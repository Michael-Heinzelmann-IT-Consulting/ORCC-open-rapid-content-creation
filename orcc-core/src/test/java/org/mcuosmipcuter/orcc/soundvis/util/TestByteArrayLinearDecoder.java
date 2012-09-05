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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;

import junit.framework.TestCase;

import org.mcuosmipcuter.orcc.soundvis.DecodingCallback;

/**
 * @author Michael Heinzelmann
 *
 */
public class TestByteArrayLinearDecoder extends TestCase{

	int samples = 7;

	public  void test_matrix() throws IOException {
		
		Encoding[] encodings = new Encoding[] {Encoding.PCM_SIGNED, Encoding.PCM_UNSIGNED}; // what's about float
		float[] sampleRates = new float[] {22050, 44100, 48000};
		int[] sampleSizes = new int[] {8, 16, 24};
		int[] channels = new int[] {1, 2};

		boolean[] bigEndians = new boolean [] {false, true};

		for(Encoding encoding : encodings) {
			for(float sampleRate : sampleRates) {
				for(final int sampleSize : sampleSizes) {
					for(int channel : channels) {
						for(boolean bigEndian : bigEndians) {
						AudioFormat audioFormat = new AudioFormat(encoding, sampleRate, sampleSize, channel, sampleSize / 8 * channel, (int)sampleRate, bigEndian);
						InputStream bis = new ByteArrayInputStream(getSignedData(sampleSize, channel, bigEndian));
						AudioInputStream ais = new AudioInputStream(bis, audioFormat, samples);
				
						final String msg = encoding + " " + sampleRate + " " + sampleSize + " " + channel + " " + bigEndian;
						ByteArrayLinearDecoder.decodeLinear(ais, new DecodingCallback() {
							
							int sample;
							int wavePos;
							@Override
							public boolean nextSample(int[] amplitudes, byte[] rawData) {
								
								for(int i = 0; i < amplitudes.length; i++) {
									System.err.println(sample + " chan " + i + ": " + amplitudes[i] + " " + msg);
									int expected = wavePos == 0  ? getMin(sampleSize) : (wavePos == 1 ? getCenter(sampleSize) : getMax(sampleSize));
									assertEquals(msg, expected, amplitudes[i]);
								}
								if(wavePos < 2) {
									wavePos++;
								}
								else {
									wavePos = 0;
								}
								sample++;
								return true;
							}
						});
						}
					}
				}
			}
		}
	}
	
	protected int getMax(int bitSize) {
		return (int)Math.pow(2, bitSize) - 1;
	}
	protected int getCenter(int bitSize) {
		return  (int)Math.pow(2, bitSize) / 2;
	}
	protected int getMin(int bitSize) {
		return 0;
	}
	
	
	private byte[] centerArray8 = new byte[] {0};
	private byte[] centerArray16le = new byte[] {-128 , 0 };
	private byte[] centerArray24le = new byte[] {-128 , -128 , 0 };
	private byte[] centerArray16be = new byte[] {0, -128 };
	private byte[] centerArray24be = new byte[] {0, -128 , -128 };
	
	protected byte[] getSignedData(int bitSize, int channels, boolean isBigEndian) {
		
		int words = bitSize/8;
		byte[] bytes = new byte[samples * words * channels];
		int sampleSize = (int)Math.pow(2, bitSize);
		
		System.err.println(words + " words max sampleSize: " + sampleSize);

		int counter = 0;
		int wavePos = 0;
		for(int sample = 0; sample < samples; sample++) {
			
			for(int channel = 0; channel < channels; channel++) {
				for(int word = 0; word < words; word++) {
					if(wavePos == 0){
						bytes[counter++] = -128;
					}
					if(wavePos == 1){
						if(words == 1) {
							bytes[counter++] = centerArray8[word];
						}
						if(words == 2) {
							bytes[counter++] = isBigEndian ? centerArray16be[ word] : centerArray16le[ word];
						}
						if(words == 3) {
							bytes[counter++] = isBigEndian ? centerArray24be[word] : centerArray24le[word];
						}
					}
					if(wavePos == 2){	
						bytes[counter++] = 127;
					}
				}

			}
			if(wavePos < 2) {
				wavePos++;
			}
			else {
				wavePos = 0;
			}
		}


		return bytes;
	}
	
}
