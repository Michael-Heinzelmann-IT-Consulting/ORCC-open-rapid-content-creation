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
package org.mcuosmipcuter.orcc.soundvis.effects;

import java.awt.geom.AffineTransform;

import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;

/**
 * @author Michael Heinzelmann
 *
 */
public class AutoFit {
	
	public enum Mode{
		OFF, FITHEIGHT, FITWIDTH, BESTFIT, BOTH
	}

	@UserProperty(description="auto fit mode")
	private Mode mode = Mode.OFF;
	
	public AffineTransform autoZoom(DimensionHelper dimensionHelper, int objectW, int objectH) {
		return autoZoom(dimensionHelper, objectW, objectH, mode);
	}
	
	public static AffineTransform autoZoom(DimensionHelper dimensionHelper, int objectW, int objectH, Mode modeToUse) {
		float wf = (float)dimensionHelper.getVideoWidth() / (float)objectW;
		float hf = (float)dimensionHelper.getVideoHeight() / (float)objectH;
		
		AffineTransform transform = new AffineTransform();
		switch(modeToUse != null ? modeToUse : Mode.OFF) {
			case BOTH:
				transform.scale(wf, hf);
				break;
			case FITHEIGHT:
				transform.scale(hf, hf);
				break;
			case FITWIDTH:
				transform.scale(wf, wf);
				break;
			case BESTFIT:
				float f = Math.min(hf, wf);
				transform.scale(f, f);
				break;			
			default:
				// no transform		
		}
		return transform;
		
	}

}
