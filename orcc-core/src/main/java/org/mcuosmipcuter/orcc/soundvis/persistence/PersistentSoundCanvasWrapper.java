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
package org.mcuosmipcuter.orcc.soundvis.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.model.SoundCanvasWrapperImpl;

/**
 * Bean conforming class to delegate to/from others that are not
 * @author user
 *
 */
public class PersistentSoundCanvasWrapper {
	
	private long frameFrom;
	private long frameTo;
	private boolean frameToAuto;
	private int repaintThreshold;
	private int transparency;
	private boolean visible;
	private boolean xor;
	
	private PersistentObject soundCanvas;

	/**
	 * 
	 */
	public PersistentSoundCanvasWrapper() {
		// bean
	}

	public PersistentSoundCanvasWrapper(SoundCanvasWrapper s) throws IllegalArgumentException, IllegalAccessException {

		this.frameFrom = s.getFrameFrom();
		this.frameTo = s.getFrameTo();
		this.frameToAuto = s.isFrameToAuto();
		this.repaintThreshold = s.getRepaintThreshold();
		this.transparency = s.getTransparency();
		this.visible = s.isVisible();
		this.xor = s.isXor();
		
		SoundCanvas sc = s.getSoundCanvas();
		soundCanvas =  PersistentObject.createTo(sc);
	}
	
	public SoundCanvasWrapper restore(List<String> reportList) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		SoundCanvas sc = (SoundCanvas) soundCanvas.getDelegate().getDeclaredConstructor((Class<?>[])null).newInstance((Object[])null);
		for(Map.Entry<String, Object> entry : soundCanvas.getPersistentProperties().entrySet()) {
			try {
			Field field = sc.getClass().getDeclaredField(entry.getKey());
			field.setAccessible(true);
			if(entry.getValue() instanceof PersistentObject) {
				PersistentObject persistentObject = (PersistentObject)entry.getValue();
				persistentObject.mergeInto(field.get(sc));
			}
			else {
				field.set(sc, entry.getValue());
			}
			}catch(Exception ex) {
				reportList.add(ex.getMessage());
			}
			
		}
		
		SoundCanvasWrapper soundCanvasWrapper = new SoundCanvasWrapperImpl(sc);
		soundCanvasWrapper.setFrameFrom(frameFrom);
		soundCanvasWrapper.setFrameTo(frameToAuto ? 0 : frameTo);
		soundCanvasWrapper.setRepaintThreshold(repaintThreshold);
		soundCanvasWrapper.setTransparency(transparency);
		soundCanvasWrapper.setVisible(visible);
		soundCanvasWrapper.setXor(xor);
		return soundCanvasWrapper;
	}

	public PersistentObject getSoundCanvas() {
		return soundCanvas;
	}

	public void setSoundCanvas(PersistentObject soundCanvas) {
		this.soundCanvas = soundCanvas;
	}

	public long getFrameFrom() {
		return frameFrom;
	}

	public void setFrameFrom(long frameFrom) {
		this.frameFrom = frameFrom;
	}

	public long getFrameTo() {
		return frameTo;
	}

	public void setFrameTo(long frameTo) {
		this.frameTo = frameTo;
	}

	public int getRepaintThreshold() {
		return repaintThreshold;
	}

	public void setRepaintThreshold(int repaintThreshold) {
		this.repaintThreshold = repaintThreshold;
	}

	public int getTransparency() {
		return transparency;
	}

	public void setTransparency(int transparency) {
		this.transparency = transparency;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isXor() {
		return xor;
	}

	public void setXor(boolean xor) {
		this.xor = xor;
	}

	public boolean isFrameToAuto() {
		return frameToAuto;
	}

	public void setFrameToAuto(boolean frameToAuto) {
		this.frameToAuto = frameToAuto;
	}


}
