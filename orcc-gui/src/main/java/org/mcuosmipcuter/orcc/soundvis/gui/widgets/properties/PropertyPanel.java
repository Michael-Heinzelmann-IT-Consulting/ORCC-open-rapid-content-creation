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

import java.awt.GridLayout;
import java.lang.reflect.Field;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;


/**
 * Abstract base class to be extended for different types of properties
 * @author Michael Heinzelmann
 */
public abstract  class PropertyPanel <T> extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	protected String name;
	protected T defaultValue;
	protected T currentValue;
	protected String description;
	
	/**
	 * Common name label
	 */
	protected JLabel nameLabel = new JLabel();

	// reference for writing the property
	private final SoundCanvas soundCanvas;
	
	/**
	 * New panel with a grid layout
	 */
	public PropertyPanel(SoundCanvas soundCanvas) {
		if(soundCanvas == null) {
			throw new IllegalArgumentException("soundCanvas null not allowed!");
		}
		this.soundCanvas = soundCanvas;
		GridLayout gl = new GridLayout(1, 2);		
		setLayout(gl);
		add(nameLabel);
	}
	/**
	 * This is where we actually write the new value by using reflection
	 * @param value the new value
	 */
	protected void setNewValue(T value) {
		 try {
			Field field = soundCanvas.getClass().getDeclaredField(getName());
			field.setAccessible(true);
			field.set(soundCanvas, value);
			setCurrentValue(value);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	/* (non-Javadoc)
	 * @see java.awt.Component#getName()
	 */
	public String getName() {
		return name;
	}
	/* (non-Javadoc)
	 * @see java.awt.Component#setName(java.lang.String)
	 */
	public void setName(String name) {
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
	
}
