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
}
