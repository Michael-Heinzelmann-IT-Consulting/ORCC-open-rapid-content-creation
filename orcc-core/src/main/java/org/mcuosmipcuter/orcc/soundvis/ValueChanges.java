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
package org.mcuosmipcuter.orcc.soundvis;

import java.util.Arrays;

/**
 * @author Michael Heinzelmann
 */
public class  ValueChanges  {
	private Object original;
	public void setOriginal(Object original) {
		this.original = original;
	}
	private Object current;
	public ValueChanges() {
		
	}
	/**
	 * 
	 */
	public ValueChanges(Object originalValue, Object firstChanged) {
		original = originalValue;
		current = firstChanged;
	}
	public void addChangeValue(Object value) {
		current = value;
	}
	public boolean isLogicallyChanged() {
		if(original == null) {
			return current != null;
		}
		if(original.getClass().isArray()) {
			return !handleArrays();
		}
		boolean result= !original.equals(current);
		return result;
	}
	@Override
	public String toString() {
		return "ValueChanges [o=" + original +  " c=" + current + " changed=" + isLogicallyChanged() + "]";
	}
	
	private boolean handleArrays() {
		Object[] currArr = (Object[])current;
		Object[] origArr = (Object[])original;
		return Arrays.equals(currArr, origArr);
	}
	public Object getOriginal() {
		return original;
	}
	public Object getCurrent() {
		return current;
	}
	public void setCurrent(Object current) {
		this.current = current;
	}

}
