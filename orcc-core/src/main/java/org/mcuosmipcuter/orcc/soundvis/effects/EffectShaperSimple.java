package org.mcuosmipcuter.orcc.soundvis.effects;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.mcuosmipcuter.orcc.api.soundvis.DisplayUnit;
import org.mcuosmipcuter.orcc.api.soundvis.EffectShape;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;

public class EffectShaperSimple extends EffectShaper {
	
	@LimitedIntProperty(description = "configurable", minGetterMethod = "getMinValues", maxGetterMethod = "getMaxValues")
	@UserProperty(description="begin value x", unit=Unit.PERCENT_OBJECT)
	private int begValuePercent = initial.begValueXPercent;
	
	@LimitedIntProperty(description = "configurable", minGetterMethod = "getMinValues", maxGetterMethod = "getMaxValues")
	@UserProperty(description="mid value x", unit=Unit.PERCENT_OBJECT)
	private int midValuePercent = initial.midValueXPercent;
	
	@LimitedIntProperty(description = "configurable", minGetterMethod = "getMinValues", maxGetterMethod = "getMaxValues")
	@UserProperty(description="end value x", unit=Unit.PERCENT_OBJECT)
	private int endValuePercent = initial.endValueXPercent;
	
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

	public EffectShaperSimple() {
	}

	public EffectShaperSimple(EffectShape initial, int minValues, int maxValues) {
		super(initial, minValues, maxValues);
	}
	public void currentValues(DisplayUnit displayUnit, Consumer<Float> valueConsumer) {
		super.currentValues(displayUnit, new BiConsumer<Float, Float>() {
			
			@Override
			public void accept(Float x, Float y) {
				valueConsumer.accept(x);
			}
		}, getEffectShape());
	}
	public int getMinValues() {
		return super.getMinValues();
	}
	public int getMaxValues() {
		return super.getMaxValues();
	}
	
	public EffectShape getEffectShape() {
		EffectShape effectShape = new EffectShape(framesIn, framesOut, beginFrames, endFrames,
				begValuePercent, midValuePercent, endValuePercent);

		return effectShape;
	}
}
