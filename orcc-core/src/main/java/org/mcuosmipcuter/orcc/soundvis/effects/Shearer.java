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
import java.util.function.BiConsumer;

import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayObject;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayUnit;
import org.mcuosmipcuter.orcc.api.soundvis.EffectShape;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;

/**
 * @author user
 *
 */
public class Shearer implements DisplayObject {
	
	@NestedProperty(description = "shaer effects")
	EffectShaper effectShaper = new EffectShaper(new EffectShape(0, 0, 0, 0, 0, 0, 0), Integer.MIN_VALUE, Integer.MAX_VALUE);
	
	public AffineTransform shear(DisplayUnit displayUnit) {
		AffineTransform transform = new AffineTransform();
		
		effectShaper.currentValues(displayUnit, new BiConsumer<Float, Float>() {
			@Override
			public void accept(Float x, Float y) {
				transform.shear(x, y);
			}		
		});
		return transform;
	}

	@Override
	public String getDisplayKey() {
		return "shearer";
	}

	@Override
	public DisplayDuration<?> getDisplayDuration(long frameFrom, long frameTo) {
		EffectShape effectShape = effectShaper.getEffectShape();
		DisplayDuration<Shearer> duration = new DisplayDuration<>(this, frameFrom, frameTo, effectShape);
		return duration;
	}

}
