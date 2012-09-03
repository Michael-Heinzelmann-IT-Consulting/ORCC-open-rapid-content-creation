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

/**
 * Interface defining a 'playable' object that can be paused.
 * Method names use the 'Playing' annex to avoid conflict with Java keywords and Thread methods.
 * @author Michael Heinzelmann
 */
public interface PlayPauseStop {
	/**
	 * Status of this object
	 * @author Michael Heinzelmann
	 */
	public enum Status {
		NEW, RUNNING, PAUSED, ABORTING, DONE
	}
	/**
	 * Issue the start of the playing at the beginning
	 */
	public void startPlaying();
	/**
	 * Continue playing from some point
	 */
	public void continuePlaying();
	/**
	 * Pause the playing, keep resources
	 */
	public void pausePlaying();
	/**
	 * Stop playing and release resources
	 */
	public void stopPlaying();
	
	/**
	 * Information about the status
	 * @return the current status
	 */
	public Status getStatus();
}
