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
package org.mcuosmipcuter.orcc.util;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Michael Heinzelmann
 *
 */
public class RingMemory <T>{
	
	private int size = 0;

	Deque<T> deque = new ArrayDeque<T>();

	public Iterable<T> update(T newEntry) {
		while(deque.size() > 0 && deque.size() >= size) {
			deque.removeFirst();
		}
		if(size > 0) {
			deque.addLast(newEntry);
		}
		return deque;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
