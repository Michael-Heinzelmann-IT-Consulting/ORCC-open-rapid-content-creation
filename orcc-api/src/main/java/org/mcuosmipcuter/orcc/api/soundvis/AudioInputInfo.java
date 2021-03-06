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
package org.mcuosmipcuter.orcc.api.soundvis;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;


/**
 * Provides information about the audio input
 * @author Michael Heinzelmann
 */
public interface AudioInputInfo {
	/**
	 * Info about the format of the audio
	 * @see {@link AudioFormat}
	 * @return the format
	 */
	public AudioFormat getAudioFormat();

	/**
	 * Info about the length of the audio expressed in samples,
	 * this is also the number of times {@link SoundCanvas#nextSample(int[])} will be called.
	 * @see {@link AudioInputStream#getFrameLength()}
	 * @return length in audio frames (samples)
	 */
	public long getFrameLength();

	/**
	 * @return the {@link AudioLayout}
	 */
	AudioLayout getLayout();
}
