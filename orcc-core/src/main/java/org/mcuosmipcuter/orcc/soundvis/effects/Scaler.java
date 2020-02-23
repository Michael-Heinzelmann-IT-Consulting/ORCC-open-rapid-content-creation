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
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;

/**
 * @author user
 *
 */
public class Scaler implements DisplayObject{
	
	public static enum SCALE_RULE {
		HORIZONTAL, VERTICAL, BOTH
	}
	@UserProperty(description="max size x")
	private int maxScaleXPercent = 100;
	@UserProperty(description="max size y")
	private int maxScaleYPercent = 100;
	
	@UserProperty(description="in scale 0 means none")
	private int scaleIn;
	@UserProperty(description="direction rule to scale in")
	private SCALE_RULE scaleRuleIn = SCALE_RULE.BOTH;
	
	@UserProperty(description="in scale 0 means none")
	private int scaleOut;
	@UserProperty(description="direction rule to scale out")
	private SCALE_RULE scaleRuleOut = SCALE_RULE.BOTH;
	
	
	
	public AffineTransform scale(int posInSlideDuration, int numberOfFramesSlideIsVisible) {
		AffineTransform transform = new AffineTransform();

		float scaleX = maxScaleXPercent / 100f;
		float scaleY = maxScaleYPercent / 100f;
		float currentScaleIn = 1;
		float currentScaleOut = 1;

		boolean isScaleIn = scaleIn != 0 && posInSlideDuration <= Math.abs(scaleIn);
		boolean isScaleOut = scaleOut != 0 && posInSlideDuration > (numberOfFramesSlideIsVisible - Math.abs(scaleOut));
		
		if(isScaleIn) {
			float scaleRateIn = 100f / (Math.abs(scaleIn) * 100f);
			currentScaleIn = posInSlideDuration * scaleRateIn;
		}
		if(isScaleOut) {
			float scaleRateOut = 100f / (Math.abs(scaleOut) * 100f);
			currentScaleOut = (numberOfFramesSlideIsVisible - posInSlideDuration - 1) * scaleRateOut;
		}
		if(isScaleIn||isScaleOut) {
			float currentScale;
			SCALE_RULE scaleRule;
			if(currentScaleIn < currentScaleOut) {
				scaleRule = scaleRuleIn;
				currentScale = currentScaleIn;
			}
			else {
				scaleRule = scaleRuleOut;
				currentScale = currentScaleOut;
			}					
			if(scaleRule == SCALE_RULE.HORIZONTAL || scaleRule == SCALE_RULE.BOTH) {
				scaleX *= currentScale;
			}
			if(scaleRule == SCALE_RULE.VERTICAL || scaleRule == SCALE_RULE.BOTH) {
				scaleY *= currentScale;
			}
			transform.scale(scaleX, scaleY);
		}
		else {
			transform.scale(maxScaleXPercent / 100f, maxScaleYPercent / 100f);
		}

		
		return transform;
	}


	@Override
	public DisplayDuration<?> getDisplayDuration(long frameFrom, long frameTo) {
		DisplayDuration<Scaler> duration = new DisplayDuration<>();
		duration.setDisplayObject(this);
		duration.setFrom(frameFrom);
		duration.setTo(frameTo);
		duration.setEffectDurationIn(scaleIn);
		duration.setEffectDurationOut(scaleOut);
		return duration;
	}


	@Override
	public String getDisplayText() {
		return "Scaler";
	}

}
