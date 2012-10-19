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

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;


/**
 * Panel for integer properties using a {@link JSpinner}
 * @author Michael Heinzelmann
 */
public class IntegerPropertyPanel extends PropertyPanel<Integer> {

	private static final long serialVersionUID = 1L;

	private JSpinner jSpinner = new JSpinner();
	
	/**
	 * Constructor with all relevant values
	 * @param soundCanvas the canvas to work with
	 * @param value the integer value
	 * @param minimum the minimum for the spinner
	 * @param maximum the maximum for the spinner
	 * @param stepSize the step size for the spinner
	 */
	public IntegerPropertyPanel(SoundCanvas soundCanvas, int value, int minimum, int maximum, int stepSize) {
		super(soundCanvas);
		SpinnerNumberModel model = new SpinnerNumberModel(value, minimum, maximum, stepSize);
		jSpinner = new JSpinner(model);
		add(jSpinner);
		jSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setNewValue((Integer)jSpinner.getValue());
			}
		});
	}
	/**
	 * Constructor with a canvas, all other values will be default
	 * @param soundCanvas the canvas to work with
	 */
	public IntegerPropertyPanel(SoundCanvas soundCanvas) {
		this(soundCanvas, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
	}

	@Override
	public void setCurrentValue(Integer currentValue) {
		super.setCurrentValue(currentValue);
		jSpinner.setValue(currentValue);
	}
	
}

