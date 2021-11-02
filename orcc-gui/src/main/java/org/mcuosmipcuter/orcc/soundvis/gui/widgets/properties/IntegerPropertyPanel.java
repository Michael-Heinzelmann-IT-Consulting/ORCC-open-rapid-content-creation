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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.WidgetUtil;


/**
 * Panel for integer properties using a {@link JSpinner}
 * @author Michael Heinzelmann
 */
public class IntegerPropertyPanel extends PropertyPanel<Integer> {

	private static final long serialVersionUID = 1L;

	private JSpinner jSpinner = new JSpinner();
	final boolean timed;
	
	/**
	 * Constructor with all relevant values
	 * @param soundCanvas the canvas to work with
	 * @param value the integer value
	 * @param minimum the minimum for the spinner
	 * @param maximum the maximum for the spinner
	 * @param stepSize the step size for the spinner
	 */
	public IntegerPropertyPanel(SoundCanvasWrapper soundCanvasWrapper, Object valueOwner, boolean timed, int value, int minimum, int maximum, int stepSize, Unit unit, NumberMeaning numberMeaning) {
		super(soundCanvasWrapper, valueOwner);
		jSpinner =  WidgetUtil.getIntegerSpinner(value, minimum, maximum, stepSize, unit, numberMeaning);
		add(jSpinner);
		this.timed = timed;
	}

	/**
	 * Constructor with no limits
	 * @param soundCanvas the canvas to work with
	 */
	public IntegerPropertyPanel(SoundCanvasWrapper soundCanvasWrapper, Object valueOwner, boolean timed, Unit unit, NumberMeaning  numberMeaning) {
		this(soundCanvasWrapper, valueOwner, timed, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, unit, numberMeaning);
	}

	@Override
	public void setCurrentValue(Integer currentValue) {
		super.setCurrentValue(currentValue);
		jSpinner.setValue(currentValue);
	}
	@Override
	public void activate() {
		ChangeListener cl = new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				setNewValue((Integer)jSpinner.getValue());
			}
		};
		jSpinner.addChangeListener(timed ? new TimedChangeListener(cl) : cl);
	}
	
}

