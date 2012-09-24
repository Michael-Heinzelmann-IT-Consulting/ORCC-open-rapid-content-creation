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

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;

/**
 * Helper for time and sample rate frame rate related tasks
 * @author Michael Heinzelmann
 */
public class TimeAndRateHelper {

	/**
	 * gets the number of audio samples per video frame e.g. 44100 / 25 = 1764
	 * @param audioInputInfo input audio
	 * @param videoOutputInfo output video
	 * @return the samples per frame
	 */
	public static int getSamplesPerFrame(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo) {
		int videoFrameRate = videoOutputInfo.getFramesPerSecond();
		int sampleRate = (int)audioInputInfo.getAudioFormat().getSampleRate(); // non integer sample rates are rare
		int samplesPerFrame = sampleRate / videoFrameRate; // e.g. 44100 / 25 = 1764
		return samplesPerFrame;
	}

}
