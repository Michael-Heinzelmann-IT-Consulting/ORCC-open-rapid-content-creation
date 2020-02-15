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

/**
 * @author user
 *
 */
public class Shearer {
	
	@UserProperty(description="")
	private int shearX;
	@UserProperty(description="")
	private int shearY;
	
	public AffineTransform shear(int posInSlideDuration, int numberOfFramesSlideIsVisible) {
		AffineTransform transform = new AffineTransform();
		if(shearX != 0 || shearY != 0) {		
			transform.shear((double)shearX/100, (double)shearY/100);
		}		
		return transform;
	}

}
