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


import java.awt.Color;

import javax.swing.JLabel;

import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;

/**
 * Label to display the frame numbers and playback speed information
 * @author Michael Heinzelmann
 */
public class FrameLabel extends JLabel implements Listener{
	
	private static final long serialVersionUID = 1L;
	
	private long framesOerSecond = 0;
	long startTime = 0;
	long startCount = 0;
	int speed = 0;
	
	public FrameLabel() {
		super();
		setHorizontalAlignment(CENTER);
	}
	/**
	 * Update the frame count
	 * @param frameCount
	 */
	public void update(long frameCount) {
		if(startTime == 0) {
			startCount = frameCount;
			startTime = System.currentTimeMillis();
		}
		long count = frameCount - startCount;
		if(count != 0 && framesOerSecond != 0 && count % framesOerSecond == 0) {
			long actualForSecond = System.currentTimeMillis() - startTime;
			if(actualForSecond != 0) {
				speed =  (int)(100000f / actualForSecond);
			}
			startTime = System.currentTimeMillis();
		}
		setBackground(speed != 0 && speed < 98 ? Color.ORANGE : Color.GREEN);
		setText("frame " + frameCount + " speed " + speed + "%");
		
	}
	/**
	 * reset the count
	 */
	public void reset() {
		update(0);
		startTime = 0;
		speed = 0;
		setOpaque(false);
	}
	@Override
	public void contextChanged(PropertyName propertyName) {
		if(Context.PropertyName.VideoFrameRate == propertyName){
			framesOerSecond = Context.getVideoOutputInfo().getFramesPerSecond();
		}
		if(Context.PropertyName.AppState == propertyName ) {
			setOpaque(Context.getAppState() == AppState.PLAYING);
		}
		if(Context.PropertyName.SongPositionPointer == propertyName) {
			speed = 0;
			update(Context.getSongPositionPointer());
		}
	}
}