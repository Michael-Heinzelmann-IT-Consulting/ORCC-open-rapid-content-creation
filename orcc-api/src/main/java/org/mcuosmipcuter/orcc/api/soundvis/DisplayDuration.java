
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

/**
 * @author Michael Heinzelmann
 *<br/>
 * <code>
 *   |--------------------------------------------|
 *   from                                         to
 *   |---------------|             |--------------|
 *     overlapBefore                 overlapAfter
 * </code>
 *
 */
public class DisplayDuration <T extends DisplayObject> {
	private long from;
	private long to;
	private long overlapBefore;
	private long overlapAfter;
	private T displayObject;

	public boolean contains(long frame) {
		return frame >= from && frame <= to;
	}
	/**
	 * Overall from including overlapBefore
	 * @return
	 */
	public long getFrom() {
		return from;
	}
	public void setFrom(long from) {
		this.from = from;
	}
	/**
	 * Overall from including overlapAfter
	 * @return
	 */
	public long getTo() {
		return to;
	}
	public void setTo(long to) {
		this.to = to;
	}
	public T getDisplayObject() {
		return displayObject;
	}
	public void setDisplayObject(T slide) {
		this.displayObject = slide;
	}
	
	public long getOverlapBefore() {
		return overlapBefore;
	}
	public void setOverlapBefore(long overlapBefore) {
		this.overlapBefore = overlapBefore;
	}
	public long getOverlapAfter() {
		return overlapAfter;
	}
	public void setOverlapAfter(long overlapAfter) {
		this.overlapAfter = overlapAfter;
	}
	@Override
	public String toString() {
		return "DisplayDuration [from=" + from + ", to=" + to + ", displayObject=" + displayObject + ", overlapBefore="
				+ overlapBefore + ", overlapAfter=" + overlapAfter + "]";
	}

}
