/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2012 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.soundvis.model;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyListener;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;

/**
 * Implementation of a sound canvas wrapper
 * @author Michael Heinzelmann
 */
public class SoundCanvasWrapperImpl implements SoundCanvasWrapper {
	
	private Set<PropertyListener> propertyListeners = new HashSet<PropertyListener>();
	
	private final SoundCanvas soundCanvas;
	private boolean enabled = true;
	private long frameFrom = 0;
	private long frameTo = 0;
	private long displayFrameFrom = 0;
	private long displayFrameTo = 0;
	private boolean frameToAuto = true;
	private static Graphics2D devNullGraphics;
	private boolean selected;
	private boolean editorOpen;
	private int repaintThreshold;
	private boolean thresholdExceeded;
	protected AmplitudeHelper amplitudeHelper;
	private int transparency = 100;
	private boolean xor;
	private Image iconImage;
	int max;
	int maxBefore;
	
	static {
		//since this image is for nothing it can be small
		BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		devNullGraphics = bi.createGraphics();
	}
	
	public SoundCanvasWrapperImpl(SoundCanvas soundCanvas) {
		this.soundCanvas = soundCanvas;
	}
	@Override
	public void nextSample(int[] amplitudes) {
		soundCanvas.nextSample(amplitudes);
		int mono = amplitudeHelper.getSignedMono(amplitudes);
		int percent = amplitudeHelper.getSignedPercent(Math.abs(mono));
		if(percent > max) {
			max = percent;
		}
//		if(repaintThreshold > 0) {
//			int mono = amplitudeHelper.getSignedMono(amplitudes);
//			int percent = amplitudeHelper.getSignedPercent(Math.abs(mono));	
//			if(percent > repaintThreshold) {
//				thresholdExceeded = true;
//			}
//		}
	}

	@Override
	public void newFrame(long frameCount, Graphics2D graphics2d) {
		if((max - maxBefore) > repaintThreshold) {
			thresholdExceeded = true;
		}
		if(
				(
						enabled && 
						frameCount >= Math.min(displayFrameFrom, frameFrom) && (frameCount <= Math.max(displayFrameTo, frameTo) || frameTo <= 0)
				) 
				&&
				(
						repaintThreshold == 0 || 
						repaintThreshold > 0 && thresholdExceeded
				) 
			)
		{
			if(xor) {
				graphics2d.setXORMode(graphics2d.getColor());
			}
			Composite origComposite = null;
			if(transparency != 100) {
				origComposite = graphics2d.getComposite();
				graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency / 100f));  
			}

			// draws to the real graphics
			soundCanvas.newFrame(frameCount, graphics2d);
			
			if(transparency != 100) {
				graphics2d.setComposite(origComposite);
			}
			if(xor) {
				graphics2d.setPaintMode();
			}
		}
		else {
			// it's not reasonable to proxy graphics or make a wrapper with 
			//limited number of methods, use a dummy graphics object 
			soundCanvas.newFrame(frameCount, devNullGraphics);
		}
		maxBefore = max;
		max = 0;
	}
	private void touchSession(String propertyName, Object oldValue, Object newValue) {
		// TODO maybe undo manager
		Context.touchSession(soundCanvas.getClass().getSimpleName() + "#" + System.identityHashCode(this) + "::" + propertyName, oldValue, newValue);
		//IOUtil.log("touch session " + obj);
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		soundCanvas.prepare(audioInputInfo, videoOutputInfo);
		amplitudeHelper = new AmplitudeHelper(audioInputInfo);
		getFrameFromTos();
	}

	@Override
	public String getDisplayName() {
		return soundCanvas.getClass().getSimpleName();
	}

	@Override
	public boolean isVisible() {
		return enabled;
	}
	@Override
	public void setVisible(boolean enabled) {
		touchSession("enabled", this.enabled, enabled);
		this.enabled = enabled;
		
	}
	@Override
	public SoundCanvas getSoundCanvas() {
		return soundCanvas;
	}
	@Override
	public String toString() {
		return getDisplayName();
	}
	@Override
	public void postFrame() {
		soundCanvas.postFrame();
		thresholdExceeded = false;
	}
	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		soundCanvas.updateUI(width, height, graphics);
	}
	@Override
	public long getFrameFrom() {
		return frameFrom;
	}
	@Override
	public long getFrameTo() {
		return frameTo;
	}
	@Override
	public void setFrameFrom(long frameFrom) {
		touchSession("frameFrom", this.frameFrom, frameFrom);
		this.frameFrom = frameFrom;
		soundCanvas.setFrameRange(frameFrom, calculateFrameToConcrete(frameTo));
	}
	@Override
	public void setFrameTo(long frameTo) {
		this.frameToAuto = frameTo == 0;
		long newFrameTo = calculateFrameToConcrete(frameTo);
		touchSession("frameTo ", this.frameTo, newFrameTo);
		this.frameTo = newFrameTo;
		soundCanvas.setFrameRange(frameFrom, this.frameTo);
	}
	private long calculateFrameToConcrete(long to) {
		long frameToConcrete = to;
		if (to == 0) {
			frameToConcrete = Context.getMaxFrame();
		}
		return frameToConcrete;
	}
	@Override
	public boolean isSelected() {
		return selected;
	}
	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	@Override
	public int getRepaintThreshold() {
		return repaintThreshold;
	}
	@Override
	public void setRepaintThreshold(int repaintThreshold) {
		touchSession("repaintThreshold", this.repaintThreshold, repaintThreshold);
		this.repaintThreshold = repaintThreshold;
	}
	@Override
	public boolean isXor() {
		return xor;
	}
	@Override
	public void setXor(boolean xor) {
		touchSession("xor ", this.xor, xor);
		this.xor = xor;
	}
	@Override
	public void setTransparency(int transparency) {
		touchSession("transparency ", this.transparency, transparency);
		this.transparency = transparency;
	}
	@Override
	public int getTransparency() {
		return transparency;
	}
	@Override
	public void propertyWritten(Field field, String parentName, Object oldValue, Object newValue) {
		if(soundCanvas instanceof PropertyListener) {
			((PropertyListener)soundCanvas).propertyWritten(field);
		}
		for(PropertyListener pl : propertyListeners) {
			pl.propertyWritten(field);
		}
		System.err.println(field.getName());
		
		String prePath = parentName != null ? parentName + "::" : "";
		touchSession(prePath + field.getName(), oldValue, newValue);
	}
	@Override
	public void addPropertyChangeListener(PropertyListener propertyListener) {
		propertyListeners.add(propertyListener);
	}
	@Override
	public Image getIconImage() {
		return iconImage;
	}
	@Override
	public void setIconImage(Image iconImage) {
		this.iconImage = iconImage;
	}
	@Override
	public DisplayDuration<?>[] getFrameFromTos() {
		DisplayDuration<?>[] fromTos = soundCanvas.getFrameFromTos();
		if(fromTos != null && fromTos.length > 0) {
			displayFrameFrom = fromTos[0].getFrom();
			displayFrameTo = fromTos[fromTos.length - 1].getTo();
			return fromTos;
		}
		else {
			displayFrameFrom = Long.MAX_VALUE;
			displayFrameTo = Long.MIN_VALUE;
		}
		return SoundCanvasWrapper.super.getFrameFromTos();
	}
	@Override
	public boolean isEditorOpen() {
		return editorOpen;
	}
	@Override
	public void setEditorOpen(boolean editorOpen) {
		this.editorOpen = editorOpen;
	}
	@Override
	public boolean isFrameToAuto() {
		return frameToAuto;
	}
	
}
