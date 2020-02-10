package org.mcuosmipcuter.orcc.soundvis.effects;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;


public class Fader {
	
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
	
	@UserProperty(description="number of frames to fade in")
	private int fadeIn = 0;
	
	AlphaComposite ac  = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
	@UserProperty(description="rule for composite in")
	private RULE inRule = RULE.SRC_OVER;
	
	@UserProperty(description="number of frames to fade out")
	private int fadeOut = 0;
	
	@UserProperty(description="rule for composite out")
	private RULE outRule = RULE.SRC_OVER;
	
	
	public Composite fade(Graphics2D graphics2D, int posInSlideDuration, int numberOfFramesSlideIsVisible) {
		
		final Composite saveComposite = graphics2D.getComposite();
		float transparency = 1.0f;
		
		if(fadeIn != 0 && posInSlideDuration <= Math.abs(fadeIn)) {
			float fadeRate = 100f / (Math.abs(fadeIn) * 100f);
			transparency = posInSlideDuration * fadeRate;
			transparency = transparency < 0 ? 0 : (transparency > 1 ? 1f : transparency);
			graphics2D.setComposite(AlphaComposite.getInstance(inRule.getNumber(), transparency));  
		}
		if(fadeOut != 0 && posInSlideDuration > (numberOfFramesSlideIsVisible - Math.abs(fadeOut))) {
			float fadeRate = 100f / (fadeOut * 100f);
			transparency = 1-( (numberOfFramesSlideIsVisible - Math.abs(fadeOut)) - posInSlideDuration - 1) * fadeRate;
			transparency = transparency < 0 ? 0 : (transparency > 1 ? 1f : transparency);
			graphics2D.setComposite(AlphaComposite.getInstance(outRule.getNumber(), transparency));  
		}
		return saveComposite;
	}

}
