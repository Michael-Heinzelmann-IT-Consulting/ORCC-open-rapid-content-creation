/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2021 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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

import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;

/**
 * @author michael
 *
 */
public class MovingAverage {
	private final int[] values;
	private int ampsCount;
	public MovingAverage(int maxSize) {
		this.values = new int[maxSize];
	}
	public int getMaxSize() {
		return values.length;
	}
	
	@UserProperty(description="smooth", unit = Unit.OTHER)
	@NumberMeaning(numbers = 1, meanings = "none")
	@LimitedIntProperty(minimum = 1, maxGetterMethod = "getMaxSize", description = "not negative")
	private int smooth = 1;
	
	public int average(final int newValue) {
		long sum = 0;
		int result = newValue;
		if(smooth > 1 && smooth <= values.length) {
			if(ampsCount < smooth - 1) {
				values[ampsCount] = newValue;

				for(int i = 0; i < ampsCount; i++) {
					sum += values[i];
					//System.err.println(sum);
				}
				ampsCount++;
				result = (int)(sum / ampsCount);

			}
			else {
				for(int i = 0; i < smooth-1; i++) {
					values[i] = values[i+1];
				}
				values[smooth - 1] = newValue;
				
				for(int i = 0; i < smooth; i++) {
					sum += values[i];
					//System.err.println(sum);
				}
				result = (int)(sum / smooth);
				//System.err.println(sum + ".." + amp);
			}
		}
		return result;
	}
}
