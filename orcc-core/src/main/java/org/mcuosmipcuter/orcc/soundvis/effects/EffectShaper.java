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

import java.util.function.BiConsumer;

import org.mcuosmipcuter.orcc.api.soundvis.EffectShape;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;

public class EffectShaper {
	@UserProperty(description="begin size x")
	private int begValueXPercent = 0;
	@UserProperty(description="begin size y")
	private int begValueYPercent = 0;
	
	@UserProperty(description="mid size x")
	private int midValueXPercent = 100;
	@UserProperty(description="mid size y")
	private int midValueYPercent = 100;
	
	@UserProperty(description="end size x")
	private int endValueXPercent = 0;
	@UserProperty(description="end size y")
	private int endValueYPercent = 0;
	
	@UserProperty(description="in scale 0 means none")
	private int framesIn;
	@LimitedIntProperty(minimum = 0, description = "only positive integers")
	@UserProperty(description="delay in scaling 0 means scale from the beginning")
	private int beginFrames;
	
	@UserProperty(description="in scale 0 means none")
	private int framesOut;
	@LimitedIntProperty(maximum = 0, description = "only negative integers")
	@UserProperty(description="early out scaling 0 means scale to the end")
	private int endFrames;
	

	public void currentValues(int posInSlideDuration, int numberOfFramesSlideIsVisible, BiConsumer<Float, Float> valueConsumer) {

		float begScaleX = begValueXPercent / 100f;
		float begScaleY = begValueYPercent / 100f;
		float midScaleX = midValueXPercent / 100f;
		float midScaleY = midValueYPercent / 100f;		
		float endScaleX = endValueXPercent / 100f;
		float endScaleY = endValueYPercent / 100f;
		float currentScaleIn = 1;
		float currentScaleOut = 1;

		boolean isScalingIn = framesIn != 0 && posInSlideDuration > beginFrames && posInSlideDuration <= Math.abs(framesIn);
		boolean isScalingOut = framesOut != 0 && posInSlideDuration < (numberOfFramesSlideIsVisible + endFrames) && posInSlideDuration >= (numberOfFramesSlideIsVisible - Math.abs(framesOut));
		
		if(isScalingIn) {
			float scaleRateIn = 100f / ((Math.abs(framesIn) - beginFrames) * 100f);
			currentScaleIn =   (posInSlideDuration - beginFrames) * scaleRateIn;
		}
		if(isScalingOut) {
			float scaleRateOut = 100f / ((Math.abs(framesOut) + endFrames) * 100f);
			currentScaleOut = (numberOfFramesSlideIsVisible + endFrames - posInSlideDuration + 1) * scaleRateOut;
		}
		if(isScalingIn) {
			float scaleRangeX = midScaleX - begScaleX;
			float scaleRangeY = midScaleY - begScaleY;					
			scaleRangeX *= currentScaleIn;		
			scaleRangeY *= currentScaleIn;
			
			//transform.scale(begScaleX + scaleRangeX, begScaleY + scaleRangeY);
			valueConsumer.accept(begScaleX + scaleRangeX, begScaleY + scaleRangeY);
		}
		else if(isScalingOut) {
			float scaleRangeX = midScaleX - endScaleX;
			float scaleRangeY = midScaleY - endScaleY;				
			scaleRangeX *= currentScaleOut;		
			scaleRangeY *= currentScaleOut;
			
			valueConsumer.accept(endScaleX + scaleRangeX, endScaleY + scaleRangeY);
		}
		else {
			if(posInSlideDuration <= beginFrames) {
				valueConsumer.accept(begValueXPercent / 100f, begValueYPercent / 100f);
			}
			else if(posInSlideDuration >= (numberOfFramesSlideIsVisible + endFrames)) {
				valueConsumer.accept(endValueXPercent / 100f, endValueYPercent / 100f);
			}
			else {
				valueConsumer.accept(midValueXPercent / 100f, midValueYPercent / 100f);
			}
		}

	}
	
	public EffectShape getEffectShape(long frameFrom, long frameTo) {
		EffectShape effectShape = new EffectShape(frameFrom, frameTo, framesIn, framesOut, beginFrames, endFrames,
				begValueXPercent, begValueYPercent, midValueXPercent, midValueYPercent, endValueXPercent,
				endValueYPercent);

		return effectShape;
	}

}
