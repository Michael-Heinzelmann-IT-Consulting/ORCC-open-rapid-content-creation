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
package org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.TimedChange;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;


/**
 * Factory that delivers property editor panels for a sound canvas
 * @author Michael Heinzelmann
 */
public class PropertyPanelFactory {

	/**
	 * Get property panels for the given canvas by inspecting the canvas using reflection.
	 * @see PropertyPanel
	 * @see UserProperty
	 * @param soundCanvas the canvas to work on
	 * @return the panel set, can be empty if the canvas has no editable properties
	 */
	public static Set<PropertyPanel<?>> getCanvasPanels(SoundCanvasWrapper soundCanvasWrapper)  {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Set<PropertyPanel<?>> result = new LinkedHashSet();
		
		SoundCanvas soundCanvas = soundCanvasWrapper.getSoundCanvas();

		for(Field field : soundCanvas.getClass().getDeclaredFields()) {
			
			field.setAccessible(true);
			
			if(field.isAnnotationPresent(UserProperty.class)) {
				
				Object value = getValue(field, soundCanvas);
				@SuppressWarnings("unchecked")
				PropertyPanel<Object> c = panelFromFieldType(field, soundCanvasWrapper);
				c.setField(field);
				c.setDefaultValue(value);
				c.setCurrentValue(value);
				c.setDescription(field.getAnnotation(UserProperty.class).description());
				result.add(c);
			}

		}
		return result;
	}
	@SuppressWarnings("rawtypes")
	private static PropertyPanel panelFromFieldType(Field field, SoundCanvasWrapper soundCanvasWrapper) {
		Class<?> type = field.getType();
		final boolean timed = field.isAnnotationPresent(TimedChange.class);
		if(boolean.class.equals(type)) {
			return new BooleanPropertyPanel(soundCanvasWrapper);
		}
		if(int.class.equals(type)) {
			final IntegerPropertyPanel i;
			if(field.isAnnotationPresent(LimitedIntProperty.class)) {
				LimitedIntProperty l = field.getAnnotation(LimitedIntProperty.class);
				Integer integer = getValue(field, soundCanvasWrapper.getSoundCanvas());
				int value = integer != null ? integer.intValue() : 0;
				i = new IntegerPropertyPanel(soundCanvasWrapper, timed, value, l.minimum(), l.maximum(), l.stepSize());
			}
			else {
				i = new IntegerPropertyPanel(soundCanvasWrapper, timed);
			}
			return i;
		}
		if(String.class.equals(type)) {
			return new StringPropertyPanel(soundCanvasWrapper);
		}
		if(Color.class.equals(type)) {
			return new ColorPropertyPanel(soundCanvasWrapper);
		}
		if(BufferedImage.class.equals(type)) {
			return new BufferedImagePropertyPanel(soundCanvasWrapper);
		}
		if(BufferedImage[].class.equals(type)) {
			return new MultiImagePropertyPanel(soundCanvasWrapper);
		}
		if(type.isEnum()) {
			Object[] es = type.getEnumConstants();
			return new EnumPropertyPanel(soundCanvasWrapper, (Enum<?>[]) es);
		}
		throw new RuntimeException(type + " type not supported");
	}
	@SuppressWarnings("unchecked")
	private static <T> T  getValue(Field field, SoundCanvas soundCanvas) {
		try {
			return (T)field.get(soundCanvas);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}

