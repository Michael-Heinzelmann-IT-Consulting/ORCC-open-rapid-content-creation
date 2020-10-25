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

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;

/**
 * @author user
 *
 */
public class Positioner {
	
	@UserProperty(description="center x position", unit = Unit.PERCENT_VIDEO)
	private int centerX = 0;
	
	@UserProperty(description="center y position", unit = Unit.PERCENT_VIDEO)
	private int centerY = 0;
	
	public AffineTransform position(DimensionHelper dimensionHelper, Rectangle outline) {
		AffineTransform transform = new AffineTransform();
			int width = outline != null ? outline.width : dimensionHelper.getVideoWidth();
			int height = outline != null ? outline.height : dimensionHelper.getVideoHeight(); 
			float translateX =  ((float)(dimensionHelper.getVideoWidth() + dimensionHelper.realX(centerX) - width)) / 2f ;
			float translateY =    ((float)(dimensionHelper.getVideoHeight() + dimensionHelper.realY(centerY) - height )) / 2f;
			transform.translate(translateX, translateY);
		return transform;
	}

	public AffineTransform position(DimensionHelper dimensionHelper) {
		return position(dimensionHelper, null);
	}
}
