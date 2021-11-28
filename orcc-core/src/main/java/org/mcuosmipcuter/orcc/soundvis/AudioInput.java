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
package org.mcuosmipcuter.orcc.soundvis;

import javax.sound.sampled.AudioInputStream;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;

/**
 * Abstraction of an audio input
 * @author Michael Heinzelmann
 */
public interface AudioInput  {
	
	enum Type {
		FILE, STREAM
	}

	/**
	 * Get the name e.g. file name
	 * @return input name
	 */
	public String getName();
	
	/**
	 * Additional information whether this input 
	 * should be available from the classpath
	 * @return classpath or null
	 */
	public String getClasspath();
	
	/**
	 * @return get the type
	 */
	public Type getType();
	
	/**
	 * Open input and return stream, when done reading close with {@link #close()}
	 * @return the stream for reading
	 */
	public AudioInputStream getAudioStream();
	
	/**
	 * Returns the audio info object belonging to this audio input
	 * @return the info
	 */
	public AudioInputInfo getAudioInputInfo();
}