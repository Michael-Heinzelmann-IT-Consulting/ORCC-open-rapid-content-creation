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
import java.util.Arrays;

import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayObject;
import org.mcuosmipcuter.orcc.api.soundvis.EffectShape;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;

/**
 * @author user
 *
 */
public class Scaler implements DisplayObject{
	
	@UserProperty(description="begin size x")
	private int begScaleXPercent = 0;
	@UserProperty(description="begin size y")
	private int begScaleYPercent = 0;
	
	@UserProperty(description="mid size x")
	private int midScaleXPercent = 100;
	@UserProperty(description="mid size y")
	private int midScaleYPercent = 100;
	
	@UserProperty(description="end size x")
	private int endScaleXPercent = 0;
	@UserProperty(description="end size y")
	private int endScaleYPercent = 0;
	
	@UserProperty(description="in scale 0 means none")
	private int scaleIn;
	@LimitedIntProperty(minimum = 0, description = "only positive integers")
	@UserProperty(description="delay in scaling 0 means scale from the beginning")
	private int lateIn;
	
	@UserProperty(description="in scale 0 means none")
	private int scaleOut;
	@LimitedIntProperty(maximum = 0, description = "only negative integers")
	@UserProperty(description="early out scaling 0 means scale to the end")
	private int earlyOut;
	
	
	public AffineTransform scale(int posInSlideDuration, int numberOfFramesSlideIsVisible, int width, int height) {
		AffineTransform transform = new AffineTransform();

		float begScaleX = begScaleXPercent / 100f;
		float begScaleY = begScaleYPercent / 100f;
		float midScaleX = midScaleXPercent / 100f;
		float midScaleY = midScaleYPercent / 100f;		
		float endScaleX = endScaleXPercent / 100f;
		float endScaleY = endScaleYPercent / 100f;
		float currentScaleIn = 1;
		float currentScaleOut = 1;

		boolean isScalingIn = scaleIn != 0 && posInSlideDuration > lateIn && posInSlideDuration <= Math.abs(scaleIn);
		boolean isScalingOut = scaleOut != 0 && posInSlideDuration < (numberOfFramesSlideIsVisible + earlyOut) && posInSlideDuration >= (numberOfFramesSlideIsVisible - Math.abs(scaleOut));
		
		if(isScalingIn) {
			float scaleRateIn = 100f / ((Math.abs(scaleIn) - lateIn) * 100f);
			currentScaleIn =   (posInSlideDuration - lateIn) * scaleRateIn;
		}
		if(isScalingOut) {
			float scaleRateOut = 100f / ((Math.abs(scaleOut) + earlyOut) * 100f);
			currentScaleOut = (numberOfFramesSlideIsVisible + earlyOut - posInSlideDuration + 1) * scaleRateOut;
		}
		if(isScalingIn) {
			float scaleRangeX = midScaleX - begScaleX;
			float scaleRangeY = midScaleY - begScaleY;					
			scaleRangeX *= currentScaleIn;		
			scaleRangeY *= currentScaleIn;
			
			transform.scale(begScaleX + scaleRangeX, begScaleY + scaleRangeY);
		}
		else if(isScalingOut) {
			float scaleRangeX = midScaleX - endScaleX;
			float scaleRangeY = midScaleY - endScaleY;				
			scaleRangeX *= currentScaleOut;		
			scaleRangeY *= currentScaleOut;
			
			transform.scale(endScaleX + scaleRangeX, endScaleY + scaleRangeY);
		}
		else {
			if(posInSlideDuration <= lateIn) {
				transform.scale(begScaleXPercent / 100f, begScaleYPercent / 100f);
			}
			else if(posInSlideDuration >= (numberOfFramesSlideIsVisible + earlyOut)) {
				transform.scale(endScaleXPercent / 100f, endScaleYPercent / 100f);
			}
			else {
				transform.scale(midScaleXPercent / 100f, midScaleYPercent / 100f);
			}
		}
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
		EffectShape effectShape = new EffectShape(frameFrom, frameTo, scaleIn, scaleOut, lateIn, earlyOut,
				begScaleXPercent, begScaleYPercent, midScaleXPercent, midScaleYPercent, endScaleXPercent,
				endScaleYPercent);
		DisplayDuration<Scaler> duration = new DisplayDuration<>(this, effectShape);
		return duration;
	}


	@Override
	public String getDisplayKey() {
		return "scaler";
	}

}
