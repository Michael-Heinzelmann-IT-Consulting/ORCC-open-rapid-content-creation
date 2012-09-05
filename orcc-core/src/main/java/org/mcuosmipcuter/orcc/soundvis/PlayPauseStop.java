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
