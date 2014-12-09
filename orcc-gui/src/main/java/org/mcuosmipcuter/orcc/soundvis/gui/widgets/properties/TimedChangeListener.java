/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2014 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Michael Heinzelmann
 *
 */
public class TimedChangeListener implements ChangeListener {
	
	private ChangeListener wrappedChangeListener;
	
	private long start;
	private long timeOut = 150;
	
	Timer t = new Timer(true);

	public TimedChangeListener(ChangeListener wrappedChangeListener) {
		this.wrappedChangeListener = wrappedChangeListener;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(final ChangeEvent e) {

		if(start == 0) {
		
		t.schedule(new TimerTask() {
			
			@Override
			public void run() {
				long diff = System.currentTimeMillis() - start;
				System.err.print("." + diff);
				if(diff > timeOut) {
					System.err.println("System.currentTimeMillis()");
					wrappedChangeListener.stateChanged(e);
					cancel();
					start = 0;
				}
				
			}
		}, 100, 100);
		}
	
		start = System.currentTimeMillis();
	}

}
