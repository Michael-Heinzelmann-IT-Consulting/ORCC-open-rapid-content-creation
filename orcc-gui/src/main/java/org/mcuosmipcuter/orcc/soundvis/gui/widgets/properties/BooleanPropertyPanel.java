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

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;

/**
 * Panel for boolean properties using a {@link JCheckBox}
 * @author Michael Heinzelmann
 */
public class BooleanPropertyPanel extends PropertyPanel<Boolean> {

	private static final long serialVersionUID = 1L;

	private JCheckBox check = new JCheckBox();
	/**
	 * Constructor
	 * @param soundCanvas the canvas to work with
	 */
	public BooleanPropertyPanel(SoundCanvasWrapper soundCanvasWrapper, Object valueOwner) {
		super(soundCanvasWrapper, valueOwner);
		add(check);
		check.addChangeListener(new ChangeListener() {
			boolean checked = check.isSelected();
			public void stateChanged(ChangeEvent e) {
				if(check.isEnabled() && checked != check.isSelected()) {
					setNewValue(check.isSelected());
				}
				checked = check.isSelected();
			}
		});
	}
	@Override
	public void setCurrentValue(Boolean currentValue) {
		final boolean enabled = check.isEnabled();
		try {
			check.setEnabled(false);
			super.setCurrentValue(currentValue);
			check.setSelected(currentValue);
			this.repaint();
		}
		finally {
			check.setEnabled(enabled);
		}
	}

}
