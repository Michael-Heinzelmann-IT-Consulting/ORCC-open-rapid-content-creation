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

public class EffectShape {

	public final int framesIn;
	public final int framesOut;
	public final int beginFrames;
	public final int endFrames;
	public final int begValueXPercent;
	public final int begValueYPercent;
	public final int midValueXPercent;
	public final int midValueYPercent;
	public final int endValueXPercent;
	public final int endValueYPercent;
	public EffectShape(int overlapBefore, int overlapAfter, int lateIn, int earlyOut,
			int begValueXPercent, int begValueYPercent, int midValueXPercent, int midValueYPercent,
			int endValueXPercent, int endValueYPercent) {
		this.framesIn = overlapBefore;
		this.framesOut = overlapAfter;
		this.beginFrames = lateIn;
		this.endFrames = earlyOut;
		this.begValueXPercent = begValueXPercent;
		this.begValueYPercent = begValueYPercent;
		this.midValueXPercent = midValueXPercent;
		this.midValueYPercent = midValueYPercent;
		this.endValueXPercent = endValueXPercent;
		this.endValueYPercent = endValueYPercent;
	}

	public EffectShape(int overlapBefore, int overlapAfter, int lateIn, int earlyOut,
			int begValuePercent, int midValuePercent, int endValuePercent) {
		this(overlapBefore, overlapAfter, lateIn, earlyOut, begValuePercent, begValuePercent, midValuePercent, midValuePercent, endValuePercent, endValuePercent);
	}
}
