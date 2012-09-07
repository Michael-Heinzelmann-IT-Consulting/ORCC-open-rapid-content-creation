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

import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStopHolder;

/**
 * Action listener that works with the given holder to stop the {@link PlayPauseStop}
 * @author Michael Heinzelmann
 */
public class StopActionListener implements ActionListener {
	PlayPauseStopHolder ppsHolder;
	
	/**
	 * New action listener that will stop the {@link PlayPauseStop} instance obtained from the holder.
	 * @param ppsHolder
	 */
	public StopActionListener(PlayPauseStopHolder ppsHolder) {
		this.ppsHolder = ppsHolder;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		ppsHolder.getPlayPauseStop().stopPlaying();
		
	}

}
