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
package org.mcuosmipcuter.orcc.soundvis.gui.widgets;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop.Status;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStopFactory;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStopHolder;


/**
 * Toggle button for play and pause action
 * @author Michael Heinzelmann
 */
public class PlayPauseButton extends JButton implements PlayPauseStopHolder{
	
	private static final long serialVersionUID = 1L;
	private PlayPauseStop playPauseStop;
	private final PlayPauseStopFactory ppsFactory;
	private final Font big = new Font(Font.MONOSPACED, Font.BOLD, 20);
	private final Font small = new Font(Font.MONOSPACED, Font.BOLD, 12);
	
	
	/**
	 * New button using the given factory
	 * @param ppsFactory
	 */
	public PlayPauseButton(PlayPauseStopFactory ppsFactory) {
		this.ppsFactory = ppsFactory;
		playPauseStop = ppsFactory.newPlayPauseStop();
		
		addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if(playPauseStop.getStatus() == Status.DONE) {
					playPauseStop = PlayPauseButton.this.ppsFactory.newPlayPauseStop();
					playPauseStop.startPlaying();
					changeVisuals(true);
				}
				else if(playPauseStop.getStatus() == Status.NEW) {
					playPauseStop.startPlaying();
					changeVisuals(true);
				}
				else if(playPauseStop.getStatus() == Status.PAUSED) {
						playPauseStop.continuePlaying();
						changeVisuals(true);
				}
				else if(playPauseStop.getStatus() == Status.RUNNING) {
					playPauseStop.pausePlaying();
					changeVisuals(false);
				}

			}
		});
		
		setFont(big);
		
		changeVisuals(false);
	}
	
	/**
	 * Changes the display, currently using string graphic
	 * @param nowPlaying whether the PPS is playing
	 */
	protected void changeVisuals(boolean nowPlaying) {
		// TODO icon
		if(nowPlaying) {
			setFont(small);
			setText("||");
			setToolTipText("pause");
		}
		else {
			setFont(big);
			setText(">");
			setToolTipText("play");
		}
	}

	@Override
	public PlayPauseStop getPlayPauseStop() {
		return playPauseStop;
	}
	
	/**
	 * Reset the button
	 */
	public void reset() {
		changeVisuals(false);
	}
}
