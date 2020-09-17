
/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2020 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.api.soundvis;

/**
 * @author Michael Heinzelmann
 * Unit of display relative to timeline
 *<br/>
 * 
 * <pre>
 *   | ov bef |                          | ov aft |
 *   |--------------------------------------------|
 *                     duration
 *   |---------------|   
 *            currentPosition    
 * </pre>
 *
 */
public class DisplayUnit {
	public final int currentPosition;
	public final int duration;
	public final int overLapBefore;
	public final int overLapAfter;
	public final int index;
	public DisplayUnit(int currentPosition, int duration, int overLapBefore, int overLapAfter, int index) {
		this.currentPosition = currentPosition;
		this.duration = duration;
		this.overLapBefore = overLapBefore;
		this.overLapAfter = overLapAfter;
		this.index = index;
	}
	@Override
	public String toString() {
		return "DisplayUnit [currentPosition=" + currentPosition + ", duration=" + duration + ", overLapBefore="
				+ overLapBefore + ", overLapAfter=" + overLapAfter + ", index=" + index + "]";
	}
	
}
