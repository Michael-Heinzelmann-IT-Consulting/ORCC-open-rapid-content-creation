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
package org.mcuosmipcuter.orcc.soundvis.threads;

import java.io.IOException;

import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.persistence.Session;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * @author user
 *
 */
public class SaveThread extends Thread implements Context.Listener{

	private boolean enabled;
	
	private long latestTouchCounter;
	
	@Override
	public synchronized void start() {
		enabled = true;
		super.start();
	}

	@Override
	public void run() {
		while(this.getState().equals(State.RUNNABLE)){
			try {
				sleep(60000); // TODO config
				long newCounter = Context.getTouchCounter();
				if(enabled && newCounter != latestTouchCounter) {
					IOUtil.log("save ...");
					saveLatestSession();
				}
				latestTouchCounter = newCounter;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	public void saveLatestSession(){		
		try {
			Session.saveDefaultSession(true);
		} catch (IllegalArgumentException | IllegalAccessException | IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void contextChanged(PropertyName propertyName) {
		if(Context.PropertyName.ExportFileName.equals(propertyName)) {
			// synchronous
			saveLatestSession();
		}
		if(Context.PropertyName.AppState.equals(propertyName)) {
			this.enabled = Context.getAppState() != AppState.INIT &&  Context.getAppState() != AppState.EXPORTING;
		}
	}

}
