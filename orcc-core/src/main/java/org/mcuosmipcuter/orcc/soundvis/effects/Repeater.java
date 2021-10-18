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
import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.types.LongSequence;

/**
 * @author user
 *
 */
public class Repeater {
	
	@LimitedIntProperty(minimum=0, description="number cannot be lower than 1")
	@UserProperty(description="number of repeat", unit=Unit.TIMES)
	@NumberMeaning(numbers = 0, meanings = "fixed tos")
	int repeat = 1;
	@LimitedIntProperty(minimum=0, description="number cannot be lower than 0 = auto")
	@UserProperty(description="number of repeat", unit = Unit.FRAMES)
	@NumberMeaning(numbers = 0, meanings = "auto")
	int frames = 0;
	
	@UserProperty(description="sequence of fixed tos to repeat over", unit=Unit.TIMES)	
	LongSequence fixedTos = new LongSequence();
	
	private DisplayObject[] displayObjects;
	
	public Repeater(DisplayObject ...displayObjects) {
		this.displayObjects = displayObjects;
	}

	public DisplayUnit[] repeat(long frameFrom, long frameTo, long frameCount) {
		
		int relFrameCount = (int) (frameCount - frameFrom);
		
		int oLapBef = 0;
		int oLapAft = 0;
		for(DisplayObject d : displayObjects) {
			oLapBef = (int) Math.min(oLapBef, d.getDisplayDuration(frameFrom, frameTo).getOverlapBefore());
			oLapAft = (int) Math.max(oLapAft, d.getDisplayDuration(frameFrom, frameTo).getOverlapAfter());
		}
		
		List<DisplayUnit> units = new ArrayList<>();
		int index = 0;
		int relStart = 0;
		for(int r = 0; r < repeats(); r++) {
			int repeatDurationFrames = getRepeatDurationFrames(frameFrom, frameTo, r);
			int duration = repeatDurationFrames + Math.abs(oLapBef) + oLapAft;
			if(relFrameCount  <= frameTo + oLapAft) {
				int start = relStart + oLapBef;
				relStart += repeatDurationFrames;
				int end = start + duration - 1;

				if(relFrameCount >= start && relFrameCount <= end ) {
					//IOUtil.log(start + " * " + end + " = " + (relFrameCount - start));
					if(index >= repeats() ) {
						index = 0;
					}
					DisplayUnit d = new DisplayUnit(relFrameCount - start, duration, oLapBef, oLapAft, index);
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

	private int getRepeatDurationFrames(long frameFrom, long frameTo, int index) {
		int duration = (int) (frameTo - frameFrom);
		long[] validTos = fixedTos.validSequence();
		if(validTos.length > 0 && repeat == 0) {
			int currentTo = 0;
			int prevTo = 0;
			for(int i = 0; i < validTos.length; i++) {
				int toi = (int)validTos[i];
				currentTo = toi != 0 ? toi : currentTo;
				if(i == index) {
					return currentTo - prevTo;
				}
				prevTo = currentTo;
			}
		}
		int repeatDurationFrames;
		if(frames == 0) {
			repeatDurationFrames = duration / repeats();
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
		for(DisplayObject d : displayObjects) {
			oLapBef = (int) Math.min(oLapBef, d.getDisplayDuration(frameFrom, frameTo).getOverlapBefore());
			oLapAft = (int) Math.max(oLapAft, d.getDisplayDuration(frameFrom, frameTo).getOverlapAfter());
		}
		int effects = displayObjects.length;
		
		DisplayDuration<?>[]result = new DisplayDuration<?>[repeats() * effects];
		int c = 0;
		int relStart = 0;
		for(int r = 0; r < repeats() * effects; r += effects) {
			int repeatDurationFrames = getRepeatDurationFrames(frameFrom, frameTo, c);
			long end = repeat == 1 ? frameTo : frameFrom + relStart + repeatDurationFrames - 1;
			for(int j = 0; j < effects; j++) {
				DisplayDuration<?> dd = displayObjects[j].getDisplayDuration(frameFrom + relStart, end);
				dd.setFrom(dd.getFrom() + oLapBef);
				dd.setTo(dd.getTo() + oLapAft);
				result[r + j] = dd;
			}
			relStart += repeatDurationFrames;
			c++;
		}
		return result;
	}
	
	private int repeats() {
		return repeat > 0 ? repeat : fixedTos.validSequence().length;
	}

	public void setFrames(int frames) {
		this.frames = frames;
	}

	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}
	
}
