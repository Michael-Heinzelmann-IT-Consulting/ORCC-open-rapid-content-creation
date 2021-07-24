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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.lang.reflect.Field;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.mcuosmipcuter.orcc.api.soundvis.TimedChange;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;


/**
 * Abstract base class to be extended for different types of properties
 * @author Michael Heinzelmann
 */
public abstract  class PropertyPanel <T> extends JPanel implements EditorLifeCycle {
	
	private static final long serialVersionUID = 1L;
	
	protected Field field;
	protected String name;
	protected String parentName;
	protected T defaultValue;
	protected T currentValue;
	protected String description;
	
	/**
	 * Common name label
	 */
	protected JLabel nameLabel = new JLabel();

	// reference for writing the property
	private final Object valueOwner;
	private final SoundCanvasWrapper soundCanvasWrapper;
	
	/**
	 * New panel with a grid layout
	 */
	public PropertyPanel(SoundCanvasWrapper soundCanvasWrapper, Object valueOwner) {
		if(soundCanvasWrapper == null) {
			throw new IllegalArgumentException("soundCanvas null not allowed!");
		}
		this.soundCanvasWrapper = soundCanvasWrapper;
		this.valueOwner = valueOwner;
		//setLayout(new BorderLayout(6, 6));
		GridLayout gl = new GridLayout(1 , 2, 12, 12);		
		setLayout(gl);
		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(nameLabel);
		//setPreferredSize(new Dimension(150, 25));
	}
	/**
	 * This is where we actually write the new value by using reflection
	 * @param value the new value
	 */
	protected void setNewValue(T value) {
		 try {
			Field field = valueOwner.getClass().getDeclaredField(getName());

			if(field.isAnnotationPresent(TimedChange.class)) {
				Context.beforePropertyUpdate(field.getName());
			}
			field.setAccessible(true);
        	//System.err.println(System.currentTimeMillis()  + " before field.set ");
			Object oldVale = field.get(valueOwner);
			field.set(valueOwner, value);
			//System.err.println(System.currentTimeMillis()  + " after field.set ");
			setCurrentValue(value);
			soundCanvasWrapper.propertyWritten(field, parentName, oldVale, value);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(100, 25);
	}
	protected void addSelectorComponent(Component c) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridwidth = GridBagConstraints.REMAINDER; //end row
		add(c, BorderLayout.EAST);
	}
	@Override
	public void activate() {
		// for setup listeners etc.
	}
	@Override
	public void passivate() {
		// for hiding popups etc.
	}
	/* (non-Javadoc)
	 * @see java.awt.Component#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the field represented by this panel
	 * @param field
	 */
	public void setField(Field field) {
		String name = field.getName();
		nameLabel.setText(name);
		this.name = name;
	}
	/**
	 * Get the default value, this is the object instance value
	 *  e.g. 0 for primitives, null for objects or any value set 
	 *  by the canvas developer
	 * @return the default value
	 */
	public T getDefaultValue() {
		return defaultValue;
	}
	/**
	 * Set the default value, see {@link #getDefaultValue()}
	 * @param defaultValue the value to set
	 */
	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
	}
	/**
	 * This is the value of the property right now
	 * @return the actual value
	 */
	public T getCurrentValue() {
		return currentValue;
	}
	/**
	 * Sets the current value, note that {@link #setNewValue(Object)} always
	 * calls this method to set the current value, but not vice versa
	 * @param currentValue the value to set
	 */
	public void setCurrentValue(T currentValue) {
		this.currentValue = currentValue;
	}
	/**
	 * Get description of the property
	 * @return property description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * Set description of the property, updates the tool tip text
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
		setToolTipText(description);
	}
	/**
	 * Get the field represented by this panel
	 * @return
	 */
	public Field getField() {
		return field;
	}
	/**
	 * @return parent name for a nested property or null
	 */
	public String getParentName() {
		return parentName;
	}
	/**
	 * Set parent name if this is a nested property: sound canvas name / parentName / field name
	 * @param parentName
	 */
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	
}
