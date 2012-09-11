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
package org.mcuosmipcuter.orcc.api.util;

import javax.sound.sampled.AudioFormat;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;

/**
 * Helps with amplitude tasks, stateful
 * @author Michael Heinzelmann
 */
public class AmplitudeHelper {
	
	private final AudioFormat audioFormat;
	private final long amplitudeRange;
	
	/**
	 * New helper using the given info
	 * @param audioInputInfo the info to use
	 */
	public AmplitudeHelper(AudioInputInfo audioInputInfo) {
		this.audioFormat = audioInputInfo.getAudioFormat();
		int sampleSizeBits = audioFormat.getSampleSizeInBits();
		amplitudeRange = (long)Math.pow(2, sampleSizeBits);
	}

	/**
	 * Gets the decimal amplitude range ( 2^sample-bits )
	 * @return the range
	 */
	public long getAmplitudeRange() {
		return  amplitudeRange;
	}
	/**
	 * Return signed mono on 1 channel or left + right / 2 on 2 channels, channels in excess of 2 are ignored 
	 * @param amplitudes input amplitudes
	 * @return the mono
	 */
	public int getSignedMono(int[] amplitudes) {
		final int value = amplitudes.length == 2 ? (amplitudes[0] + amplitudes[1]) / 2 : amplitudes[0];
		return value - (int)amplitudeRange / 2 ;
	}
	/**
	 * Same as {@link #getSignedMono(int[])} but unsigned
	 * @param amplitudes
	 * @return the mono
	 */
	public int getUnSignedMono(int[] amplitudes) {
		return amplitudes.length == 2 ? (amplitudes[0] + amplitudes[1]) / 2 : amplitudes[0];
	}

}
