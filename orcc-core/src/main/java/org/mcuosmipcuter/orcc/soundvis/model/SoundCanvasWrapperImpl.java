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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyListener;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.effects.Positioner;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Implementation of a sound canvas wrapper
 * @author Michael Heinzelmann
 */
public class SoundCanvasWrapperImpl implements SoundCanvasWrapper {
	
	private Set<PropertyListener> propertyListeners = new HashSet<PropertyListener>();
	
	private final SoundCanvas soundCanvas;
	private final String sessionId;
	private boolean enabled = true;
	private long frameFrom = 0;
	private long frameTo = 0;
	private long displayFrameFrom = 0;
	private long displayFrameTo = 0;
	private boolean frameToAuto = true;
	private static Graphics2D devNullGraphics;
	private boolean selected;
	private boolean editorOpen;
	private int scale = 100;
	private int posX;
	private int posY;
	protected AmplitudeHelper amplitudeHelper;
	private int transparency = 100;
	private boolean xor;
	private Image iconImage;

	private Shape screen;
	private DimensionHelper dimensionHelper;
	Positioner positioner = new Positioner();

	
	static {
		//since this image is for nothing it can be small
		BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		devNullGraphics = bi.createGraphics();
	}
	
	public SoundCanvasWrapperImpl(SoundCanvas soundCanvas, String sessionId) {
		this.soundCanvas = soundCanvas;
		this.sessionId = sessionId != null ? sessionId : soundCanvas.getClass().getSimpleName() + "_"+ UUID.randomUUID().toString();
	}

	@Override
	public void nextSample(int[] amplitudes) {	
		try {
			soundCanvas.nextSample(amplitudes);
		}
		catch(Exception ex) {
			IOUtil.log(soundCanvas + ".nextSample " + ex);
		}
	}

	private void wrappedNewFrame(long frameCount, Graphics2D graphics2d) {
		try {
			soundCanvas.newFrame(frameCount, graphics2d);
		}
		catch(Exception ex) {
			IOUtil.logWithStack(ex);
		}
	}
	
	@Override
	public void newFrame(long frameCount, Graphics2D graphics2d) {

		if(enabled && scale != 0 && frameCount >= Math.min(displayFrameFrom, frameFrom) 
				&& (frameCount <= Math.max(displayFrameTo, frameTo) || frameTo <= 0))
		{
			if(xor) {
				graphics2d.setXORMode(graphics2d.getColor());
			}
			Composite origComposite = null;

			if(transparency != 100) {
				origComposite = graphics2d.getComposite();
				graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency / 100f));  
			}
			if(scale != 100  || posX != 0 || posY != 0) {
				Area fillArea = new Area(screen);
				AffineTransform ats = new AffineTransform();
				double sc = (double)scale / 100d;
				ats.scale(sc, sc);
				if(scale < 0) {
					ats.translate(-fillArea.getBounds().width , -fillArea.getBounds().height);
				}
				fillArea.transform(ats);
				positioner.setCenterX(posX);
				positioner.setCenterY(posY);
				AffineTransform atp = positioner.position(dimensionHelper, fillArea.getBounds());
				graphics2d.transform(atp);
				graphics2d.transform(ats);

			}

			// draws to the real graphics
			wrappedNewFrame(frameCount, graphics2d);
			if(scale != 100 || posX != 0 || posY != 0) {
				graphics2d.setTransform(new AffineTransform());
			}
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
			wrappedNewFrame(frameCount, devNullGraphics);
		}
	}
	@Override
	public void changeSession(String propertyName, Object oldValue, Object newValue) {
		Context.changeSession(sessionId + "::" + propertyName, oldValue, newValue);
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		soundCanvas.prepare(audioInputInfo, videoOutputInfo);
		amplitudeHelper = new AmplitudeHelper(audioInputInfo);
		getFrameFromTos();
		int width = videoOutputInfo.getWidth();
		int height = videoOutputInfo.getHeight();
		screen = new Rectangle2D.Double(0,0,width, height);
		dimensionHelper = new DimensionHelper(videoOutputInfo);
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
		final boolean oldEnabled = this.enabled;
		this.enabled = enabled;
		changeSession("enabled", oldEnabled, enabled);	
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
		final long oldFrameFrom = this.displayFrameFrom;
		this.frameFrom = frameFrom;
		soundCanvas.setFrameRange(frameFrom, calculateFrameToConcrete(frameTo));
		changeSession("frameFrom", oldFrameFrom, frameFrom);
	}
	@Override
	public void setFrameTo(long frameTo) {
		this.frameToAuto = frameTo == 0;
		long newFrameTo = calculateFrameToConcrete(frameTo);
		final long oldFrameTo = this.frameTo;
		this.frameTo = newFrameTo;
		soundCanvas.setFrameRange(frameFrom, this.frameTo);
		changeSession("frameTo", oldFrameTo, newFrameTo);
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
	public int getScale() {
		return scale;
	}

	@Override
	public void setScale(int scale) {
		int oldScale = this.scale;
		this.scale = scale;
		changeSession("scale", oldScale, scale);
	}
	
	@Override
	public int getPosX() {
		return posX;
	}
	
	@Override
	public void setPosX(int posX) {
		int oldPosX = this.posX;
		this.posX = posX;
		changeSession("posX", oldPosX, posX);
	}
	
	@Override
	public int getPosY() {
		return posY;
	}

	@Override
	public void setPosY(int posY) {
		int oldPosY = this.posY;
		this.posY = posY;
		changeSession("posY", oldPosY, posY);
	}
	
	@Override
	public boolean isXor() {
		return xor;
	}
	@Override
	public void setXor(boolean xor) {
		final boolean oldXor = this.xor;
		this.xor = xor;
		changeSession("xor ", oldXor, xor);
	}
	@Override
	public void setTransparency(int transparency) {
		final int oldTransparency = this.transparency;
		this.transparency = transparency;
		changeSession("transparency", oldTransparency, transparency);
	}
	@Override
	public int getTransparency() {
		return transparency;
	}
	@Override
	public void propertyWritten(Field field, String parentName, Object oldValue, Object newValue) {

		for(PropertyListener pl : propertyListeners) {
			pl.propertyWritten(field);
		}
		
		String prePath = parentName != null ? parentName + "::" : "";
		changeSession(prePath + field.getName(), oldValue, newValue);
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
			displayFrameFrom = frameFrom;//Long.MAX_VALUE;
			displayFrameTo = frameTo;//Long.MIN_VALUE;
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
	@Override
	public String getSessionId() {
		return sessionId;
	}
	
}
