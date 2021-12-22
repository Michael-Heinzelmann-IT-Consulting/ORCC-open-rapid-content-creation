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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.function.Consumer;

import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayObject;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayUnit;
import org.mcuosmipcuter.orcc.api.soundvis.EffectShape;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;


public class Fader implements DisplayObject{
	
	public static enum RULE {
		CLEAR(1), SRC(2), DST(9), SRC_OVER(3), DST_OVER(4), SRC_IN(5), DST_IN(6), SRC_OUT(7), DST_OUT(8), SRC_ATOP(10),
		DST_ATOP(11), XOR(12);

		private int number;

		private RULE(int number) {
			this.number = number;
		}
		public int getNumber() {
			return this.number;
		}
	}
	@NestedProperty(description = "shape effects")
	EffectShaperSimple effectShaper = new EffectShaperSimple(new EffectShape(0, 0, 0, 0, 0, 100, 0), 0, 100);
	
	AlphaComposite ac  = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
	@UserProperty(description="rule for composite in")
	private RULE rule = RULE.SRC_OVER;
	
	
	public Composite fade(Graphics2D graphics2D, DisplayUnit displayUnit) {
		
		final Composite saveComposite = graphics2D.getComposite();
		
		effectShaper.currentValues(displayUnit, new Consumer<Float>() {				
			@Override
			public void accept(Float transparency) {
				transparency = transparency < 0 ? 0 : (transparency > 1 ? 1f : transparency);
				float has = 1.0f;
				if(saveComposite instanceof AlphaComposite) {
					has = ((AlphaComposite)saveComposite).getAlpha();
				}
				if(transparency < 1) {
					float effective = transparency * has;
					graphics2D.setComposite(AlphaComposite.getInstance(rule.getNumber(), effective));
				}
			}
		});
		return saveComposite;
	}

	@Override
	public DisplayDuration<?> getDisplayDuration(long frameFrom, long frameTo) {
		EffectShape effectShape = effectShaper.getEffectShape();
		DisplayDuration<Fader> duration = new DisplayDuration<>(this, frameFrom, frameTo, effectShape);
		return duration;
	}

	@Override
	public String getDisplayKey() {
		return "fader";
	}
	
}
