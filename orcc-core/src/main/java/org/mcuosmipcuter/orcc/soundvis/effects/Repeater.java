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
import org.mcuosmipcuter.orcc.util.IOUtil;

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
		int repeatDurationFrames = getRepeatDurationFrames(frameFrom, frameTo);
		
		int oLapBef = 0;
		int oLapAft = 0;
		for(DisplayObject d : dispayObjects) {
			oLapBef = (int) Math.min(oLapBef, d.getDisplayDuration(frameFrom, frameTo).getOverlapBefore());
			oLapAft = (int) Math.max(oLapAft, d.getDisplayDuration(frameFrom, frameTo).getOverlapAfter());
		}
		int duration = repeatDurationFrames + Math.abs(oLapBef) + oLapAft;
		List<DisplayUnit> units = new ArrayList<>();
		int index = 0;
		for(int r = 0; r < repeat; r++) {
			if(relFrameCount  <= frameTo + oLapAft) {
				int start = repeatDurationFrames * r + oLapBef;
				int end = start + duration - 1;

				if(relFrameCount >= start && relFrameCount <= end ) {
					//IOUtil.log(start + " * " + end + " = " + (relFrameCount - start));
					if(index >= repeat ) {
						index = 0;
					}
					DisplayUnit d = new DisplayUnit(relFrameCount - start, duration, index);
					units.add(d);
					index++;
				}
				else {
					index++;
				}

			}
		}
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

	public DisplayDuration<?>[] getFrameFromTos(long frameFrom, long frameTo) {
		int oLapBef = 0;
		int oLapAft = 0;
		for(DisplayObject d : dispayObjects) {
			oLapBef = (int) Math.min(oLapBef, d.getDisplayDuration(frameFrom, frameTo).getOverlapBefore());
			oLapAft = (int) Math.max(oLapAft, d.getDisplayDuration(frameFrom, frameTo).getOverlapAfter());
		}
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
			}
			c++;
		}
		return result;
	}

	public void setFrames(int frames) {
		this.frames = frames;
	}

	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}
	
	
}
