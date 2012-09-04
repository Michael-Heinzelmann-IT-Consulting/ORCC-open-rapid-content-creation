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
package org.mcuosmipcuter.orcc.soundvis;

import java.io.Closeable;

import javax.sound.sampled.AudioInputStream;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;

/**
 * Abstraction of an audio input
 * @author Michael Heinzelmann
 */
public interface AudioInput extends Closeable {
	
	enum Type {
		FILE, STREAM
	}

	/**
	 * Get the name e.g. file name
	 * @return input name
	 */
	public String getName();
	
	/**
	 * @return get the type
	 */
	public Type getType();
	
	/**
	 * Open input and return stream, when done reading close with {@link #close()}
	 * @return the stream for reading
	 */
	public AudioInputStream open();
	
	/**
	 * Returns the audio info object belonging to this audio input
	 * @return the info
	 */
	public AudioInputInfo getAudioInputInfo();
}