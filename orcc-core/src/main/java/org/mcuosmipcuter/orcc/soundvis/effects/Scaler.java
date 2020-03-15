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
import org.mcuosmipcuter.orcc.api.soundvis.EffectShape;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;

/**
 * @author user
 *
 */
public class Scaler implements DisplayObject{
	
	@NestedProperty(description = "shape effects")
	EffectShaper effectShaper = new EffectShaper();
	
	
	public AffineTransform scale(int posInSlideDuration, int numberOfFramesSlideIsVisible, int width, int height) {
		AffineTransform transform = new AffineTransform();
		
		effectShaper.currentValues(posInSlideDuration, numberOfFramesSlideIsVisible, new BiConsumer<Float, Float>() {

			@Override
			public void accept(Float x, Float y) {
				transform.scale(x, y);
			}
			
		});

		if(transform.getScaleX() < 0) {
			transform.translate(-width , 1);
		}
		if(transform.getScaleY() < 0) {
			transform.translate(1, -height);
		}
		return transform;
	}


	@Override
	public DisplayDuration<?> getDisplayDuration(long frameFrom, long frameTo) {
		EffectShape effectShape = effectShaper.getEffectShape();
		DisplayDuration<Scaler> duration = new DisplayDuration<>(this, frameFrom, frameTo, effectShape);
		return duration;
	}


	@Override
	public String getDisplayKey() {
		return "scaler";
	}

}
