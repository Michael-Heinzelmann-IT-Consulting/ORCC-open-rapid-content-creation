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

import org.mcuosmipcuter.orcc.api.soundvis.DisplayUnit;
import org.mcuosmipcuter.orcc.api.soundvis.EffectShape;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;

public class EffectShaper {
	
	EffectShape initial = new EffectShape(0, 0, 0, 0, 0, 100, 0);
	private int minValues = Integer.MIN_VALUE;
	private int maxValues = Integer.MAX_VALUE;
	
	@LimitedIntProperty(description = "configurable", minGetterMethod = "getMinValues", maxGetterMethod = "getMaxValues")
	@UserProperty(description="begin value x", unit=Unit.PERCENT_OBJECT)
	private int begValueXPercent = initial.begValueXPercent;
	@LimitedIntProperty(description = "configurable", minGetterMethod = "getMinValues", maxGetterMethod = "getMaxValues")
	@UserProperty(description="begin value y", unit=Unit.PERCENT_OBJECT)
	private int begValueYPercent = initial.begValueYPercent;
	
	@LimitedIntProperty(description = "configurable", minGetterMethod = "getMinValues", maxGetterMethod = "getMaxValues")
	@UserProperty(description="mid value x", unit=Unit.PERCENT_OBJECT)
	private int midValueXPercent = initial.midValueXPercent;
	@LimitedIntProperty(description = "configurable", minGetterMethod = "getMinValues", maxGetterMethod = "getMaxValues")
	@UserProperty(description="mid value y", unit=Unit.PERCENT_OBJECT)
	private int midValueYPercent = initial.midValueYPercent;
	
	@LimitedIntProperty(description = "configurable", minGetterMethod = "getMinValues", maxGetterMethod = "getMaxValues")
	@UserProperty(description="end value x", unit=Unit.PERCENT_OBJECT)
	private int endValueXPercent = initial.endValueXPercent;
	@LimitedIntProperty(description = "configurable", minGetterMethod = "getMinValues", maxGetterMethod = "getMaxValues")
	@UserProperty(description="end value y", unit=Unit.PERCENT_OBJECT)
	private int endValueYPercent = initial.endValueYPercent;
	
	@UserProperty(description="slope in frames", unit=Unit.FRAMES)
	private int framesIn = initial.framesIn;
	@LimitedIntProperty(minimum = 0, description = "only positive integers")
	@UserProperty(description="static in frames", unit=Unit.FRAMES)
	private int beginFrames = initial.beginFrames;
	
	@UserProperty(description="slope out frames", unit=Unit.FRAMES)
	private int framesOut = initial.framesOut;
	@LimitedIntProperty(maximum = 0, description = "only negative integers")
	@UserProperty(description="static out frames", unit=Unit.FRAMES)
	private int endFrames = initial.endFrames;
	
	
	public EffectShaper() {
	}
	public EffectShaper(EffectShape initial, int minValues, int maxValues) {
		this.initial = initial;
		this.minValues = minValues;
		this.maxValues = maxValues;

		this.begValueXPercent = initial.begValueXPercent;
		this.begValueYPercent = initial.begValueYPercent;
		this.midValueXPercent = initial.midValueXPercent;
		this.midValueYPercent = initial.midValueYPercent;
		this.endValueXPercent = initial.endValueXPercent;
		this.endValueYPercent = initial.endValueYPercent;
		this.framesIn = initial.framesIn;
		this.beginFrames = initial.beginFrames;
		this.framesOut = initial.framesOut;
		this.endFrames = initial.endFrames;
	}

	public int getMinValues() {
		return minValues;
	}
	public int getMaxValues() {
		return maxValues;
	}
	public void currentValues(DisplayUnit displayUnit, BiConsumer<Float, Float> valueConsumer) {
		currentValues(displayUnit, valueConsumer, getEffectShape());
	}
	protected void currentValues(DisplayUnit displayUnit, BiConsumer<Float, Float> valueConsumer, EffectShape current) {

		int posInSlideDuration = recalcPosInSlideDuration(displayUnit, current);
		int numberOfFramesSlideIsVisible =  recalcNumberOfFramesSlideIsVisible(displayUnit, current);
		if(posInSlideDuration >= numberOfFramesSlideIsVisible) {
			posInSlideDuration = numberOfFramesSlideIsVisible -1; // INFINITY SLOPE VERTICAL //
		}
		
		//System.err.println(valueConsumer + " " + displayUnit.index+ ": " + displayUnit.currentPosition + " "+ posInSlideDuration + " / " + displayUnit.duration + " " + numberOfFramesSlideIsVisible);
		
		float begScaleX = current.begValueXPercent / 100f;
		float begScaleY = current.begValueYPercent / 100f;
		float midScaleX = current.midValueXPercent / 100f;
		float midScaleY = current.midValueYPercent / 100f;		
		float endScaleX = current.endValueXPercent / 100f;
		float endScaleY = current.endValueYPercent / 100f;
		float currentScaleIn = 1;
		float currentScaleOut = 1;

		boolean isScalingIn = current.framesIn != 0 && posInSlideDuration >= current.beginFrames && posInSlideDuration <= Math.abs(current.framesIn);
		boolean isScalingOut = current.framesOut != 0 && posInSlideDuration < (numberOfFramesSlideIsVisible + current.endFrames) && posInSlideDuration >= (numberOfFramesSlideIsVisible - Math.abs(current.framesOut));
		
		if(isScalingIn) {
			float distanceIn = ((Math.abs(current.framesIn) - current.beginFrames) * 100f);
			if(distanceIn != 0) {
				float scaleRateIn = 100f / distanceIn;
				currentScaleIn =   (posInSlideDuration - current.beginFrames) * scaleRateIn;
			}
		}
		if(isScalingOut) {
			float distanceOut = ((Math.abs(current.framesOut) + current.endFrames) * 100f);
			if(distanceOut != 0) {
				float scaleRateOut = 100f / distanceOut;
				currentScaleOut = (numberOfFramesSlideIsVisible + current.endFrames - posInSlideDuration) * scaleRateOut;
			}
		}
		if(isScalingIn) {
			float scaleRangeX = midScaleX - begScaleX;
			float scaleRangeY = midScaleY - begScaleY;					
			scaleRangeX *= currentScaleIn;		
			scaleRangeY *= currentScaleIn;
			
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
			if(posInSlideDuration < current.beginFrames) {
				valueConsumer.accept(current.begValueXPercent / 100f, current.begValueYPercent / 100f);
			}
			else if(posInSlideDuration >= (numberOfFramesSlideIsVisible + current.endFrames)) {  // INFINITY SLOPE VERTICAL //
				valueConsumer.accept(current.endValueXPercent / 100f, current.endValueYPercent / 100f);
			}
			else {
				valueConsumer.accept(current.midValueXPercent / 100f, current.midValueYPercent / 100f);
			}
		}

	}
	private int recalcPosInSlideDuration(DisplayUnit displayUnit, EffectShape current) {
		int posInSlideDuration = displayUnit.currentPosition; 
		if(displayUnit.overLapBefore < 0) {
			if(current.framesIn >= 0) {
				posInSlideDuration += displayUnit.overLapBefore;
			}
			else {
				posInSlideDuration += (displayUnit.overLapBefore - current.framesIn);
			}
		}
		return posInSlideDuration >= 0 ? posInSlideDuration : 0;
	}
	private int recalcNumberOfFramesSlideIsVisible(DisplayUnit displayUnit, EffectShape current) {
		int numberOfFramesSlideIsVisible = displayUnit.duration;
		
		if(displayUnit.overLapBefore < 0) {
			numberOfFramesSlideIsVisible += (displayUnit.overLapBefore - Math.min(current.framesIn, 0));
		}
		if(displayUnit.overLapAfter > 0) {
			numberOfFramesSlideIsVisible -= (displayUnit.overLapAfter - Math.max(current.framesOut, 0));
		}
		
		return numberOfFramesSlideIsVisible;
	}
	public EffectShape getEffectShape() {
		EffectShape effectShape = new EffectShape(framesIn, framesOut, beginFrames, endFrames,
				begValueXPercent, begValueYPercent, midValueXPercent, midValueYPercent, endValueXPercent,
				endValueYPercent);

		return effectShape;
	}

}
