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
package org.mcuosmipcuter.orcc.soundvis.effects;

import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayObject;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;

/**
 * @author user
 *
 */
public class Repeater {
	
	@LimitedIntProperty(minimum=1, description="number cannot be lower than 1")
	@UserProperty(description="number of repeat")
	int repeat = 1;
	@LimitedIntProperty(minimum=0, description="number cannot be lower than 0 = auto")
	@UserProperty(description="number of repeat")
	int frames = 0;
	
	
	public int repeat(long frameFrom, long frameTo, long frameCount) {
		int relFrameCount = (int) (frameCount - frameFrom);
		int posInSlideDuration = relFrameCount;

		int repeatDurationFrames = getRepeatDurationFrames(frameFrom, frameTo);
		if(repeat > 1) {
			if(relFrameCount / repeatDurationFrames < repeat) {
				posInSlideDuration = relFrameCount % repeatDurationFrames;
			}
			else {
				posInSlideDuration = repeatDurationFrames ;
			}
		}
		return posInSlideDuration;
	}

	public int getRepeatDurationFrames(long frameFrom, long frameTo) {
		int duration = (int) (frameTo - frameFrom);
		int repeatDurationFrames;
		if(frames == 0) {
			repeatDurationFrames = duration / repeat;
		}
		else {
			repeatDurationFrames = frames;
		}
		if(repeatDurationFrames < 1) {
			repeatDurationFrames = 1;
		}
		return repeatDurationFrames;
	}

	public int getRepeat() {
		return repeat;
	}

	public DisplayDuration<?>[] getFrameFromTos(long frameFrom, long frameTo, DisplayObject ... dispayObjects) {
		int effects = dispayObjects.length;
		int repeatDurationFrames = getRepeatDurationFrames(frameFrom, frameTo);
		DisplayDuration<?>[]result = new DisplayDuration<?>[repeat * effects];
		int c = 0;
		for(int r = 0; r < repeat * effects; r += effects) {
			long end = repeat == 1 ? frameTo : frameFrom + repeatDurationFrames * (c + 1) - 1;
			for(int j = 0; j < effects; j++) {
				result[r + j] = dispayObjects[j].getDisplayDuration(frameFrom + repeatDurationFrames * c, end);
			}
			c++;
		}
		return result;
	}
}
