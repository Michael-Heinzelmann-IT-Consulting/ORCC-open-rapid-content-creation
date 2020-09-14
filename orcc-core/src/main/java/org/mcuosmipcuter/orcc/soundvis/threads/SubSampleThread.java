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
import org.mcuosmipcuter.orcc.soundvis.model.SuperSample;
import org.mcuosmipcuter.orcc.soundvis.util.ByteArrayLinearDecoder;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Takes an audio stream and produces sub samples from it with less resolution
 * @author Michael Heinzelmann
 */
public class SubSampleThread extends Thread {
	
	/**
	 * Callback for asynchronous work
	 * @author Michael Heinzelman
	 */
	public interface CallBack {
		/**
		 * Called when this thread has finished its work
		 * @param superSampleData the sampled data
		 */
		public void finishedSampling(SuperSampleData superSampleData);
	}
	
	private final AudioInput ai;
	private final int noOfSamples;
	private final CallBack callBack;
		
	/**
	 * Constructs a new sample thread
	 * @param ai the audio input to use
	 * @param noOfSamples the desired number of samples
	 * @param callBack callback that will be notified when this thread has finished sampling
	 */
	public SubSampleThread(final AudioInput ai, final int noOfSamples, final CallBack callBack) {
		this.ai = ai;
		this.noOfSamples =  noOfSamples;
		this.callBack = callBack;
	}

	@Override
	public void run() {	

		final List<SuperSample> list = new ArrayList<SuperSample>();
		int[] overalls = new int[2];
		
		AudioInputStream ais = ai.getAudioStream();
		try {
			ByteArrayLinearDecoder.decodeLinear(ais, new DecodingCallback() {
				final long total = ai.getAudioInputInfo().getFrameLength();
				int sampleCenter =  (int)Math.pow(2,ai.getAudioInputInfo().getAudioFormat().getSampleSizeInBits()) / 2;
				long totalCounter;
				int counter;
				int upCounter;
				int doCounter;
				int max = 0;
				int min = Integer.MAX_VALUE;
				long sumUp = 0;
				long sumDown = 0;

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
						if(amplitudes[i] >= sampleCenter) {
							sumUp += amplitudes[i];
							upCounter++;
						}
						else {
							sumDown += amplitudes[i];
							doCounter++;
						}
					}
					if(counter == noOfSamples || totalCounter == total) {
						//System.err.println("''#" + counter);
						int signedMin = min - sampleCenter;
						int signedMax = max - sampleCenter;
						int aU = upCounter != 0 ? (int)(sumUp / upCounter) - sampleCenter: 0;
						int aD = doCounter != 0 ? (int)(sumDown / doCounter) - sampleCenter: 0;
						//System.err.println(counter + " " + max + "/" + min + " ."+ sumUp + " " + sumDown + "''#" + aU + " " + aD);
						SuperSample superSample = new SuperSample(signedMin, signedMax, counter, aU, aD);
						list.add(superSample);
						if(signedMin < overalls[0]) {
							overalls[0] = signedMin;
						}
						if(signedMax > overalls[1]) {
							overalls[1] = signedMax;
						}
						counter = 0;
						min = Integer.MAX_VALUE;
						max = 0;
						sumUp = 0;
						sumDown = 0;
						upCounter = 0;
						doCounter = 0;
					}
					return true;
				}

			});

			callBack.finishedSampling(new SuperSampleData(list.toArray(new SuperSample[0]), overalls[0], overalls[1]));
			
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		finally {
			IOUtil.safeClose(ais);
		}
	}

}
