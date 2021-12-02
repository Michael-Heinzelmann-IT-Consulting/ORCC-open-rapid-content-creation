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

import java.util.ArrayList;
import java.util.List;

/**
 * An exception thrown when application logic is violated
 * @author Michael Heinzelmann
 */
public class AppLogicException extends Exception {

	private static final long serialVersionUID = 1L;
	
	float sampleRate;
	int frameRate;

	public AppLogicException(float sampleRate, int frameRate) {
		super("sample rate " + sampleRate + " % frame rate " + frameRate + " is not 0");
		this.sampleRate = sampleRate;
		this.frameRate = frameRate;
	}

	public String getAllowedMessage(int[] configured){
		List<Integer> result = new ArrayList<>();
		for(int fr : configured) {
			if(sampleRate % fr == 0) {
				result.add(fr);
			}
		}
		return "framerate " + frameRate + " is not allowed for this audio, please use " + result;
	}

}
