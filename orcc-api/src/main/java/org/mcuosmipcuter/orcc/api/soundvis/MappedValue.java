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

import java.util.Set;
import java.util.function.Supplier;

/**
 * decouples display (readable) names from internally used values
 * @author Michael Heinzelmann
 */
public class  MappedValue <V extends Comparable<V>> implements Comparable<MappedValue<V>>{

	private final V value;
	private final String displayString;
	private final Supplier<Set<MappedValue<V>>> getAll;
	
	public MappedValue(V value, String displayString, Supplier<Set<MappedValue<V>>> getAll) {
		this.value = value;
		this.displayString = displayString;
		this.getAll = getAll;
	}
	public V getValue() {
		return value;
	}
	public String getDisplayString() {
		return displayString;
	}
	public Set<MappedValue<V>> getAll() {
		return getAll.get();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappedValue other = (MappedValue) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	@Override
	public int compareTo(MappedValue<V> o) {
		return value.compareTo(o.value);
	}
	@Override
	public String toString() {
		// TODO return "MappedValue [value=" + value + ", displayString=" + displayString + "]";
		return displayString;
	}
	
}
