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

/**
 * @author Michael Heinzelmann
 */
public class  ValueChanges  {
	//private List<Object> newValues = new ArrayList<Object>();
	private final Object first;
	private Object last;
	/**
	 * 
	 */
	public ValueChanges(Object originalValue, Object firstChanged) {
//		newValues.add(originalValue);
//		newValues.add(firstChanged);
		first = originalValue;
		last = firstChanged;
	}
	public void addChangeValue(Object value) {
		//newValues.add(value);
		last = value;
	}
	public boolean isLogicallyChanged() {
//		Object first = newValues.get(0);
//		Object last = newValues.get(newValues.size() - 1);
		//System.err.println("first:" + first);
		//System.err.println("lasst:" + last);
		if(first == null) {
			return last != null;
		}
		boolean result= !first.equals(last);
		//System.err.println(result);
		return result;
	}
	@Override
	public String toString() {
		return "ValueChanges [o=" + first +  " c=" + last + "]";
	}
	
}
