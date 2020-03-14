
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
	private int[] effectX;
	private int[] effectY;
	
	
	public DisplayDuration() {
		
	}
	public DisplayDuration(T displayObject, long frameFrom, long frameTo, EffectShape effectShape) {
		this.displayObject = displayObject;
		this.effectX = new int[12];
		this.effectY = new int[12];
		
		this.from = frameFrom;
		this.to = frameTo;
		this.overlapBefore = effectShape.framesIn;
		this.overlapAfter = effectShape.framesOut;
		int[] x = effectX;
		int from = (int) frameFrom;
		int to = (int) frameTo + 1;
		x[0] = effectShape.framesIn > 0 ? from : from + effectShape.framesIn;
		x[11] = x[0];
		x[1] = x[0] + effectShape.beginFrames;
		x[10] = x[1];
		x[2] = x[0] + Math.abs(effectShape.framesIn);
		x[9] = x[2];
		
		x[5] = effectShape.framesOut < 0 ? to : to + effectShape.framesOut;
		x[6] = x[5];
		x[4] = x[5] + effectShape.endFrames; 
		x[7] = x[4];
		x[3] = x[5] - Math.abs(effectShape.framesOut);
		x[8] = x[3];
		
		int[] y = effectY;
		y[0] = 50 - (effectShape.begValueXPercent   / 2);
		y[1] = y[0];
		y[2] = 50 - (effectShape.midValueXPercent  / 2);
		y[3] = y[2];
		y[4] = 50 - (effectShape.endValueXPercent  / 2);
		y[5] = y[4];
		y[6] = 50 + (effectShape.endValueYPercent  / 2);
		y[7] = y[6];
		y[8] = 50 + (effectShape.midValueYPercent  / 2);
		y[9] = y[8];
		y[10] = 50 + (effectShape.begValueYPercent  / 2);
		y[11] = y[10];
		//System.err.println(x[0] + ", " + x[1] + ", " + x[2] + ", " + x[3] + ", " + x[4] + ", " + x[5] + ", " + x[6] + ", " +x[7] + ", " +x[8] + ", " +x[9] + ", " + x[10] + ", " + x[11] );
		//System.err.println(y[0] + ", " + y[1] + ", " + y[2] + ", " + y[3] + ", " + y[4] + ", " + y[5] + ", " + y[6] + ", " +y[7] + ", " +y[8] + ", " +y[9] + ", " + y[10] + ", " + y[11] );
 		//System.err.println(frameFrom + "<->" + frameTo);

	}
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
	public int[] getEffectX() {
		return effectX;
	}
	public int[] getEffectY() {
		return effectY;
	}
	@Override
	public String toString() {
		return "DisplayDuration [from=" + from + ", to=" + to + ", displayObject=" + displayObject + ", overlapBefore="
				+ overlapBefore + ", overlapAfter=" + overlapAfter + "]";
	}

}
