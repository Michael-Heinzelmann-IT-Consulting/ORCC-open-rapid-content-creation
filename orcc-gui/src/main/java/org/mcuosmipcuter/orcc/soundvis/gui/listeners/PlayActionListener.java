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
package org.mcuosmipcuter.orcc.soundvis.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.mcuosmipcuter.orcc.soundvis.PlayPauseStopFactory;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStopHolder;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop.Status;


/**
 * Action listener that plays the {@link PlayPauseStop}
 * @author Michael Heinzelmann
 */
public class PlayActionListener implements ActionListener, PlayPauseStopHolder {
	
	private PlayPauseStop playPauseStop;
	private final PlayPauseStopFactory ppsFactory;
	
	/**
	 * New listener with a fixed factory to obtain {@link PlayPauseStop}
	 * @param ppsFactory factory to obtain the {@link PlayPauseStop} to work with
	 */
	public PlayActionListener(PlayPauseStopFactory ppsFactory) {
		this.ppsFactory = ppsFactory;
		playPauseStop = ppsFactory.newPlayPauseStop();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(playPauseStop.getStatus() == Status.DONE) {
			playPauseStop = ppsFactory.newPlayPauseStop();
		}
		if(playPauseStop.getStatus() == Status.NEW) {
			playPauseStop.startPlaying();
		}
		else {
			if(playPauseStop.getStatus() == Status.PAUSED) {
				playPauseStop.continuePlaying();
			}
		}
	}

	@Override
	public PlayPauseStop getPlayPauseStop() {
		return playPauseStop;
	}

}
