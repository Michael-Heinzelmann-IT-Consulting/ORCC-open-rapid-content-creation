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

import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;

public class Mover {
	
	@UserProperty(description="in move 0 means none")
	private int moveInXFrames;
	@UserProperty(description="move in x speed")
	private int moveInXSpeed;
	
	@UserProperty(description="in move 0 means none")
	private int moveOutXFrames;
	@UserProperty(description="move out x speed")
	private int moveOutXSpeed;
	
	@UserProperty(description="in move 0 means none")
	private int moveInYFrames;
	@UserProperty(description="move in y speed")
	private int moveInYSpeed;
	
	@UserProperty(description="in move 0 means none")
	private int moveOutYFrames;
	@UserProperty(description="move out y speed")
	private int moveOutYSpeed;
	
	public AffineTransform move(int posInSlideDuration, int numberOfFramesSlideIsVisible) {
		float translateX = 0;
		float translateY = 0;
		
		if(moveInXFrames != 0 && posInSlideDuration <= Math.abs(moveInXFrames)) {
			int framesToGo = Math.abs(moveInXFrames) - posInSlideDuration;
			framesToGo = framesToGo > 0 ? framesToGo : 0;
			translateX = translateX -  framesToGo * moveInXSpeed;
		}

		if(moveOutXFrames != 0 && posInSlideDuration > (numberOfFramesSlideIsVisible - Math.abs(moveOutXFrames))) {
			int movedFrames = posInSlideDuration - (numberOfFramesSlideIsVisible - Math.abs(moveOutXFrames));
			translateX = translateX +  movedFrames * moveOutXSpeed;
		}
		if(moveInYFrames != 0 && posInSlideDuration <= Math.abs(moveInYFrames)) {
			int framesToGo = Math.abs(moveInYFrames) - posInSlideDuration;
			framesToGo = framesToGo > 0 ? framesToGo : 0;
			translateY = translateY -  framesToGo * moveInYSpeed;
		}

		if(moveOutYFrames != 0 && posInSlideDuration > (numberOfFramesSlideIsVisible - Math.abs(moveOutYFrames))) {
			int movedFrames = posInSlideDuration - (numberOfFramesSlideIsVisible - Math.abs(moveOutYFrames));
			translateY = translateY +  movedFrames * moveOutYSpeed;
		}
				
		return translateX != 0 || translateY != 0 ? new AffineTransform(1, 0, 0, 1, translateX,translateY) : new AffineTransform();

	}

}
