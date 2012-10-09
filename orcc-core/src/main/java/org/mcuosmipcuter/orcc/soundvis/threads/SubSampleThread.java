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
package org.mcuosmipcuter.orcc.soundvis.threads;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;

import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.DecodingCallback;
import org.mcuosmipcuter.orcc.soundvis.util.ByteArrayLinearDecoder;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Takes an audio stream and produces sub samples from it with less resolution
 * @author Michael Heinzelmann
 */
public class SubSampleThread extends Thread {
	
	public interface CallBack {
		public void finishedSampling(SuperSampleData superSampleData);
	}
	
	public static class SuperSample {
		
		public SuperSample(int min, int max, int noOfSamples) {
			this.min = min;
			this.max = max;
			this.noOfSamples = noOfSamples;
		}
		private final int min;
		private final int max;
		private final int noOfSamples;
		
		public int getMin() {
			return min;
		}

		public int getMax() {
			return max;
		}

		public int getNoOfSamples() {
			return noOfSamples;
		}

		@Override
		public String toString() {
			return "SuperSample [min=" + min + ", max=" + max
					+ ", noOfSamples=" + noOfSamples + "]";
		}
		
	}
	public static class SuperSampleData {
		private SuperSample[] list;
		private int overallMin;
		private int overallMax;
		public SuperSample[] getList() {
			return list;
		}
		public int getOverallMin() {
			return overallMin;
		}
		public int getOverallMax() {
			return overallMax;
		}
		
	}
	
	final AudioInput ai;
	final int noOfSamples;
	final CallBack callBack;
		
	public SubSampleThread(final AudioInput ai, final int noOfSamples, final CallBack callBack) {
		this.ai = ai;
		this.noOfSamples =  noOfSamples;
		this.callBack = callBack;
	}

	@Override
	public void run() {	

		final SuperSampleData superSampleData = new SuperSampleData();
		final List<SuperSample> list = new ArrayList<SubSampleThread.SuperSample>();
		
		AudioInputStream ais = ai.getAudioStream();
		try {
			ByteArrayLinearDecoder.decodeLinear(ais, new DecodingCallback() {
				final long total = ai.getAudioInputInfo().getFrameLength();
				int sampleCenter =  (int)Math.pow(2,ai.getAudioInputInfo().getAudioFormat().getSampleSizeInBits()) / 2;
				long totalCounter;
				int counter;
				int max = 0;
				int min = Integer.MAX_VALUE;
				@Override
				public boolean nextSample(int[] amplitudes, byte[] rawData, long sampleCount) {
					counter++;
					totalCounter++;
					for(int i = 0; i < amplitudes.length; i++) {
						if(amplitudes[i] > max) {
							max = amplitudes[i];
						}
						if(amplitudes[i] < min) {
							min = amplitudes[i];
						}
					}
					if(counter == noOfSamples || totalCounter == total) {
						int signedMin = min - sampleCenter;
						int signedMax = max - sampleCenter;
						SuperSample superSample = new SuperSample(signedMin, signedMax, counter);
						list.add(superSample);
						if(signedMin < superSampleData.overallMin) {
							superSampleData.overallMin = signedMin;
						}
						if(signedMax > superSampleData.overallMax) {
							superSampleData.overallMax = signedMax;
						}
						counter = 0;
						min = Integer.MAX_VALUE;
						max = 0;
					}
					return true;
				}

			});
			superSampleData.list = list.toArray(new SuperSample[0]);
			callBack.finishedSampling(superSampleData);
			
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		finally {
			IOUtil.safeClose(ais);
		}
	}

}
