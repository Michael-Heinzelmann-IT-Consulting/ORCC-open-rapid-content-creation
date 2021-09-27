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

import java.awt.geom.AffineTransform;

import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayObject;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;

public class Rotator implements DisplayObject {
	
	@UserProperty(description="in rotate 0 means none", unit = Unit.FRAMES)
	private int rotateInFrames;
	@UserProperty(description="rotate in speed", unit = Unit.DEGREES_PER_FRAME)
	private int rotateInSpeed;
	
	
	@UserProperty(description="in move 0 means none", unit = Unit.FRAMES)
	private int rotateOutFrames;
	@UserProperty(description="rotate out speed", unit = Unit.DEGREES_PER_FRAME)
	private int rotateOutSpeed;
	
	@UserProperty(description="rotate permanent", unit = Unit.DEGREES)
	private int permanentRotation;
	
	public AffineTransform rotate(int posInSlideDuration, int numberOfFramesSlideIsVisible, int centerX, int centerY) {
		AffineTransform transform = new AffineTransform();
		
		double theta = 0;
		double rotatePosition = 0;
		
		if(permanentRotation != 0) {
			double permRotation = Math.PI * permanentRotation / 180;
			transform.rotate(permRotation, centerX , centerY);
		}
		
		if(rotateInFrames != 0 && posInSlideDuration <= Math.abs(rotateInFrames)) {
			
			theta = Math.PI * rotateInSpeed / 180;
			double beginRotation = Math.abs(rotateInFrames) * -theta;

			rotatePosition = beginRotation + posInSlideDuration * theta;
			transform.rotate(rotatePosition, centerX , centerY);
		}

		if(rotateOutFrames != 0 && posInSlideDuration > (numberOfFramesSlideIsVisible - Math.abs(rotateOutFrames))) {
			theta = Math.PI * rotateOutSpeed / 180;
			int movedFrames = posInSlideDuration - (numberOfFramesSlideIsVisible - Math.abs(rotateOutFrames));
			rotatePosition = movedFrames * theta;
			transform.rotate(rotatePosition, centerX , centerY);
		}

				
		return transform;

	}


	@Override
	public String getDisplayKey() {
		return "rotator";
	}


	@Override
	public DisplayDuration<?> getDisplayDuration(long frameFrom, long frameTo) {
		DisplayDuration<Rotator> duration = new DisplayDuration<>();
		duration.setDisplayObject(this);
		duration.setFrom(frameFrom);
		duration.setTo(frameTo);
		duration.setOverlapBefore(rotateInFrames);
		duration.setOverlapAfter(rotateOutFrames);
		duration.setDegreesBefore(rotateInFrames * rotateInSpeed);
		duration.setDegreesAfter(rotateOutFrames * rotateOutSpeed);
		return duration;
	}


	public int getRotateInFrames() {
		return rotateInFrames;
	}


	public int getRotateOutFrames() {
		return rotateOutFrames;
	}

}
