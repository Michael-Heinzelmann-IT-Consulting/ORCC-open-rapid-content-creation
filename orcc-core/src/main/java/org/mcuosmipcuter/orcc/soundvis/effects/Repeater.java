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

import java.util.ArrayList;
import java.util.List;

import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayObject;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayUnit;
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
	
	private DisplayObject[] dispayObjects;
	
	
	public Repeater(DisplayObject ...dispayObjects) {
		this.dispayObjects = dispayObjects;
	}

	public DisplayUnit[] repeat(long frameFrom, long frameTo, long frameCount) {
		int relFrameCount = (int) (frameCount - frameFrom);
		int posInSlideDuration = relFrameCount;

		int repeatDurationFrames = getRepeatDurationFrames(frameFrom, frameTo);
		
		int oLapBef = 0;
		int oLapAft = 0;
		for(DisplayObject d : dispayObjects) {
			oLapBef = (int) Math.min(oLapBef, d.getDisplayDuration(frameFrom, frameTo).getOverlapBefore());
			oLapAft = (int) Math.max(oLapAft, d.getDisplayDuration(frameFrom, frameTo).getOverlapAfter());
		}
		int duration = repeatDurationFrames + Math.abs(oLapBef) + oLapAft;
		List<DisplayUnit> units = new ArrayList<>();
		for(int r = 0; r < repeat; r++) {
			if(relFrameCount / repeatDurationFrames < repeat) {
				int start = repeatDurationFrames * r + oLapBef + 1;
				int end = start + duration - 1;
				
				if(relFrameCount >= start && relFrameCount <= end ) {
					System.err.println(start + " * " + end + " = " + (relFrameCount - start + 1));
					DisplayUnit d = new DisplayUnit(relFrameCount - start + 1, duration);
					units.add(d);
				}
				else {
					System.err.println(start + "  " + end);
				}
				
			}
			else {
				posInSlideDuration = duration;
			}
		}
		int currentPos = posInSlideDuration;
		System.err.println(currentPos + " : " + duration);
		return units.toArray(new DisplayUnit[] {});
	}

	private int getRepeatDurationFrames(long frameFrom, long frameTo) {
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
		return repeatDurationFrames ;
	}

	public int getRepeat() {
		return repeat;
	}

	public DisplayDuration<?>[] getFrameFromTos(long frameFrom, long frameTo) {
		int oLapBef = 0;
		int oLapAft = 0;
		for(DisplayObject d : dispayObjects) {
			oLapBef = (int) Math.min(oLapBef, d.getDisplayDuration(frameFrom, frameTo).getOverlapBefore());
			oLapAft = (int) Math.max(oLapAft, d.getDisplayDuration(frameFrom, frameTo).getOverlapAfter());
		}
		//return repeatDurationFrames + Math.abs(oLapBef) + oLapAft;
		int effects = dispayObjects.length;
		int repeatDurationFrames = getRepeatDurationFrames(frameFrom, frameTo);
		DisplayDuration<?>[]result = new DisplayDuration<?>[repeat * effects];
		int c = 0;
		for(int r = 0; r < repeat * effects; r += effects) {
			long end = repeat == 1 ? frameTo : frameFrom + repeatDurationFrames * (c + 1) - 1;
			for(int j = 0; j < effects; j++) {
				DisplayDuration<?> dd = dispayObjects[j].getDisplayDuration(frameFrom + repeatDurationFrames * c, end);
				dd.setFrom(dd.getFrom() + oLapBef);
				dd.setTo(dd.getTo() + oLapAft);
				result[r + j] = dd;
				////System.err.println(dd);
			}
			c++;
		}
		return result;
	}
}
