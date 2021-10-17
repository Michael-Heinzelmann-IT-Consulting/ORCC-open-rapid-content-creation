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
package org.mcuosmipcuter.orcc.api.types;

import java.util.Arrays;

import org.mcuosmipcuter.orcc.api.LogicalEqual;

/**
 * a sequence that accepts and persists invalid user input, 
 * for applications the valid part can be accessed
 * 
 * @author Michael Heinzelmann
 */
public class LongSequence implements LogicalEqual{
	
	private long [] rawSequence = new long[0];
	private long minValidElement = 1;
	private long maxValidElement = Long.MAX_VALUE;

	public LongSequence() {
		// persistence
	}
	public LongSequence(LongSequence base, long newValue, int atUserIndex) {
		long[] baseSequence = base.rawSequence;
		System.err.println(Arrays.toString(baseSequence));
		int i = atUserIndex - 1;

		if(i >= baseSequence.length) {
			this.rawSequence = new long[i + 1];
			
		}
		else {
			this.rawSequence = new long[baseSequence.length];
		}
		System.arraycopy(baseSequence, 0, this.rawSequence, 0, baseSequence.length);
		
		this.rawSequence[i] = newValue;
		System.err.println(Arrays.toString(rawSequence));
	}

	public long[] validSequence() {
		if(rawSequence.length == 0) {
			return rawSequence;
		}
			
		int maxValidLen = 0;
		long currentMax = 0;
		for(long elem : rawSequence) {
			if(elem >= minValidElement && elem <= maxValidElement && elem > currentMax) {
				maxValidLen++;
				currentMax = elem;
			}
			else {
				break;
			}
		}
		return Arrays.copyOf(rawSequence, maxValidLen);
	}
	public long getValueAt(int userIndex, long defaultValue) {
		if(userIndex >= 0 && rawSequence.length >= userIndex) {
			return rawSequence[userIndex - 1];
		}
		return defaultValue;
	}
	@Override
	public boolean isLogicalEqual(Object other) {
		if(other instanceof LongSequence) {
			long[] valid1 = ((LongSequence) other).validSequence();
			long[] valid2 = validSequence();
			return Arrays.equals(valid1, valid2);
		}
		return false;
	}

	@Override
	public String toString() {
		
		return Arrays.toString(validSequence());
	}
	public long[] getRawSequence() {
		return rawSequence;
	}
	public void setRawSequence(long[] rawSequence) {
		this.rawSequence = rawSequence;
	}
	public long getMinValidElement() {
		return minValidElement;
	}
	public void setMinValidElement(long minValidElement) {
		this.minValidElement = minValidElement;
	}
	public long getMaxValidElement() {
		return maxValidElement;
	}
	public void setMaxValidElement(long maxValidElement) {
		this.maxValidElement = maxValidElement;
	}
	
}
