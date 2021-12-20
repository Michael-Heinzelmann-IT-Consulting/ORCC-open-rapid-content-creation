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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.api.soundvis.InputEnabling;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.MappedValue;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyGroup;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.TimedChange;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.types.LongSequence;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.defaultcanvas.model.Slide;
import org.mcuosmipcuter.orcc.util.IOUtil;


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
	public static Set<JPanel> getCanvasPanels(SoundCanvasWrapper soundCanvasWrapper, final JFrame parentFrame)  {

		Set<JPanel> result = new LinkedHashSet<>();
		Set<String> groupedNames = new HashSet<String>();
		SoundCanvas soundCanvas = soundCanvasWrapper.getSoundCanvas();

		for(Field field : soundCanvas.getClass().getDeclaredFields()) {
			
			field.setAccessible(true);
			
			if(field.isAnnotationPresent(UserProperty.class) && !groupedNames.contains(field.getName())) {
				// if not created for group create top level
				Object value = getValue(field, soundCanvas);
				@SuppressWarnings("unchecked")
				PropertyPanel<Object> c = panelFromFieldType(field, soundCanvasWrapper, soundCanvasWrapper.getSoundCanvas(), parentFrame);
				c.setField(field);
				c.setDefaultValue(value);
				c.setCurrentValue(value);
				c.setDescription(field.getAnnotation(UserProperty.class).description());
				result.add(c);
				c.activate();
				addToController(soundCanvas, field, c);
			}
			if(field.isAnnotationPresent(NestedProperty.class)) {
				 
				Set<PropertyPanel<?>> props  = new LinkedHashSet<>();
				Object nestedValue = getValue(field, soundCanvas);
				for(Field nestedField : nestedValue.getClass().getDeclaredFields()) {
					if(nestedField.isAnnotationPresent(UserProperty.class)) {
						PropertyPanel<?> pn = getPropertyPanel(nestedField, nestedValue, soundCanvasWrapper, field.getName(), parentFrame);
						props.add(pn);
						addToController(nestedValue, nestedField, pn);
					}
					// only 2 levels, no recursion:
					if(nestedField.isAnnotationPresent(NestedProperty.class)) {
						Object nested2 = getValue(nestedField, nestedValue);
						for(Field nested2Field : nested2.getClass().getDeclaredFields()) {
							if(nested2Field.isAnnotationPresent(UserProperty.class)) {
								PropertyPanel<?> pn2 = getPropertyPanel(nested2Field, nested2, soundCanvasWrapper, field.getName(), parentFrame);
								props.add(pn2);
								addToController(nested2, nested2Field, pn2);
							}
						}
						controllerReady(nested2);
					}	
				}
				controllerReady(nestedValue);
				NestedPropertyPanel npp = new NestedPropertyPanel(props, soundCanvasWrapper.getDisplayName(), field);
				result.add(npp);
				addToController(soundCanvas, field, npp);
			}
			if(PropertyGroup.class.isAssignableFrom(field.getType())){
				PropertyGroup pg = (PropertyGroup)getValue(field, soundCanvas);
				Set<PropertyPanel<?>> props  = new LinkedHashSet<>();
				for(String fieldName : pg.getFieldNames()) {
					try {
						Field groupField = soundCanvas.getClass().getDeclaredField(fieldName); // top level field that is grouped
						if(!groupField.isAnnotationPresent(UserProperty.class)) {
							throw new RuntimeException("@UserProperty must be set for " + fieldName + " to be in group " + field.getName());
						}
						PropertyPanel<?> pp = getAndRemovePanelByName(result, fieldName); // take from top level if already created
						if(pp == null) {
							pp = getPropertyPanel(groupField, soundCanvas, soundCanvasWrapper, field.getName(), parentFrame);
							addToController(soundCanvas, groupField, pp);
						}
						groupedNames.add(fieldName);
						props.add(pp);
					
					} catch (NoSuchFieldException | SecurityException e) {
						e.printStackTrace();
					} 
				}
				NestedPropertyPanel gpp = new NestedPropertyPanel(props, soundCanvasWrapper.getDisplayName(), field);
				addToController(soundCanvas, field, gpp);
				result.add(gpp);
			}

		}
		controllerReady(soundCanvas);
		return result;
	}
	private static PropertyPanel<?> getPropertyPanel(Field field, Object object, SoundCanvasWrapper soundCanvasWrapper, String parentName, final JFrame parentFrame) {
		field.setAccessible(true);
		Object value = getValue(field, object);
		@SuppressWarnings("unchecked")
		PropertyPanel<Object> c = panelFromFieldType(field, soundCanvasWrapper, object, parentFrame);
		c.setField(field);
		c.setDefaultValue(value);
		c.setCurrentValue(value);
		c.setDescription(field.getAnnotation(UserProperty.class).description());
		c.setParentName(parentName);
		c.activate();
		return c;
		
	}
	@SuppressWarnings("rawtypes")
	private static PropertyPanel panelFromFieldType(Field field, SoundCanvasWrapper soundCanvasWrapper, Object valueOwner, final JFrame parentFrame) {
		Class<?> type = field.getType();
		final boolean timed = field.isAnnotationPresent(TimedChange.class);
		if(boolean.class.equals(type)) {
			return new BooleanPropertyPanel(soundCanvasWrapper, valueOwner);
		}
		if(int.class.equals(type)) {
			NumberMeaning numberMeaning = field.isAnnotationPresent(NumberMeaning.class) ? field.getAnnotation(NumberMeaning.class) : null;
			final IntegerPropertyPanel i;
			Unit unit = field.isAnnotationPresent(UserProperty.class) ? field.getAnnotation(UserProperty.class).unit() : Unit.OTHER;
			if(field.isAnnotationPresent(LimitedIntProperty.class)) {
				LimitedIntProperty l = field.getAnnotation(LimitedIntProperty.class);
				Integer integer = getValue(field, valueOwner);
				int value = integer != null ? integer.intValue() : 0;
				int minimum = l.minimum();
				if(l.minGetterMethod().length() > 0 ) {
					try {
						minimum = (Integer)valueOwner.getClass().getDeclaredMethod(l.minGetterMethod()).invoke(valueOwner);
					} catch (Exception ex) {
						IOUtil.log(ex.getMessage());
					}
				}
				int maximum = l.maximum();
				if(l.maxGetterMethod().length() > 0 ) {
					try {
						maximum = (Integer)valueOwner.getClass().getDeclaredMethod(l.maxGetterMethod()).invoke(valueOwner);
					} catch (Exception ex) {
						IOUtil.log(ex.getMessage());
					}
				}
				int origValue = value;
				if(value < minimum) {
					value = minimum;
				}
				if(value > maximum) {
					value = maximum;
				}
				if(value != origValue) {
					IOUtil.log(field + " value out of bounds [" + minimum + " =< " + origValue + " >= " + maximum + "] using: " + value);
				}

				i = new IntegerPropertyPanel(soundCanvasWrapper, valueOwner, timed, value, minimum, maximum, l.stepSize(), unit, numberMeaning);
			}
			else {
				i = new IntegerPropertyPanel(soundCanvasWrapper, valueOwner, timed, unit, numberMeaning);
			}
			return i;
		}
		if(String.class.equals(type)) {
			return new StringPropertyPanel(soundCanvasWrapper, valueOwner, parentFrame);
		}
		if(Color.class.equals(type)) {
			return new ColorPropertyPanel(soundCanvasWrapper, valueOwner);
		}
		if(BufferedImage.class.equals(type)) {
			return new BufferedImagePropertyPanel(soundCanvasWrapper, valueOwner);
		}
		if(Slide[].class.equals(type)) {
			return new MultiImagePropertyPanel(soundCanvasWrapper, valueOwner, parentFrame);
		}
		if(type.isEnum()) {
			Object[] es = type.getEnumConstants();
			Object value = getValue(field, valueOwner);
			return new EnumPropertyPanel(soundCanvasWrapper, valueOwner, (Enum<?>[]) es, (Enum<?>)value);
		}
		if(MappedValue.class.equals(type)) {
			Object value = getValue(field, valueOwner);
			Set vs = ((MappedValue)value).getAll();
			@SuppressWarnings("unchecked")
			Set<MappedValue<?>> values = vs;
			return new MappedValuePropertyPanel(soundCanvasWrapper, valueOwner, values, value);
		}
		if(LongSequence.class.equals(type)) {
			return new LongSequencePropertyPanel(soundCanvasWrapper, valueOwner);
		}
		throw new RuntimeException(type + " type not supported");
	}
	@SuppressWarnings("unchecked")
	private static <T> T  getValue(Field field, Object soundCanvas) {
		try {
			field.setAccessible(true);
			return (T)field.get(soundCanvas);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static PropertyPanel<?> getAndRemovePanelByName(Set<JPanel> result, String name){
		Iterator<JPanel> iter = result.iterator();
		while(iter.hasNext()) {
			JPanel jp = iter.next();
			if(jp instanceof PropertyPanel && jp.getName().equals(name)) {
				iter.remove();
				return (PropertyPanel<?>) jp;
			}
		}
		return null;
	}
	
	private static void addToController(Object fieldOwner, Field field, InputEnabling enabling) {
		if(fieldOwner instanceof InputEnabling.Controller) {
			((InputEnabling.Controller)fieldOwner).addEnablingReference(field.getName(), enabling);
		}
	}
	private static void controllerReady(Object fieldOwner) {
		if(fieldOwner instanceof InputEnabling.Controller) {
			((InputEnabling.Controller)fieldOwner).enablingsReady();
		}
	}
}

