/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2013 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;

/**
 * @author Michael Heinzelmann
 *
 */
public class EnumPropertyPanel extends PropertyPanel<Enum<?>>  {

	private JComboBox<Enum<?>> jComboBox = new JComboBox<Enum<?>>();

	public EnumPropertyPanel(SoundCanvasWrapper soundCanvasWrapper, final Enum<?>[] values, Enum<?> value) {
		super(soundCanvasWrapper);
		for(Enum<?> v : values) {
			jComboBox.addItem(v);
		}
		jComboBox.setSelectedItem(value);
		
		jComboBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				for(Enum<?> v : values) {
					if(v.equals(e.getItem())) {
						setNewValue(v);
					}
				}
			}
		});
		add(jComboBox);
	}

	private static final long serialVersionUID = 1L;	

}
