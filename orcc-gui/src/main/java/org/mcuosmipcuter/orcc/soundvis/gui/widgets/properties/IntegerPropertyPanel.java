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

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;


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
		SpinnerNumberModel model = new SpinnerNumberModel(value, minimum, maximum, stepSize);
		jSpinner = new JSpinner(model);
			String format;
			switch(unit) {
				case PERCENT_OBJECT:
				case PERCENT_VIDEO:
					format = "0'%'";
					break;
				case DEGREES:
					format = "0'°'";
					break;
				case FRAMES:
					format = "0'fr'";
					break;
				case PIXEL:
					format = "0'px'";
					break;
				case POINTS:
					format = "0'pt'";
					break;
				case TIMES:
					format = "0'x'";
					break;
				case PIXEL_PER_FRAME:
					format = "0'px/fr'";
					break;
				case DEGREES_PER_FRAME:
					format = "0'°/fr'";
					break;
			default:
				format = "0";
				break;
			}
			
			 NumberFormatter displayFormat = new NumberFormatter(new DecimalFormat(format) {
				private static final long serialVersionUID = 1L;
				@Override
				public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
					if(numberMeaning != null) {
						for(int i = 0; i < numberMeaning.numbers().length; i++) {
							if(number == numberMeaning.numbers()[i]) {
								String meaning = i < numberMeaning.meanings().length ? numberMeaning.meanings()[i] : "";
								result.append(meaning);
								return result;
							}
						}
					}
					
					return super.format(number, result, fieldPosition);
				}
				

				@Override
				public Number parse(String text, ParsePosition pos) {
					if(numberMeaning != null) {
						for(int i = 0; i < numberMeaning.meanings().length; i++) {
							if(text.equals(numberMeaning.meanings()[i])) {
								int number = i < numberMeaning.numbers().length ? numberMeaning.numbers()[i] : 0;
								pos.setIndex(text.length());
								return checked(Integer.valueOf(number), minimum, maximum);
							}
						}
					}
					try {
						Number number = checked(Integer.parseInt(text), minimum, maximum);
						pos.setIndex(text.length());
						return number;
					}
					catch(NumberFormatException ex) {
						// forward to super
					}
					Number num = super.parse(text, pos);
					return checked(Integer.valueOf(num != null ? num.intValue() : (int)0), minimum, maximum); // convert oversized Long
				}});

			DefaultFormatterFactory factory = new DefaultFormatterFactory(displayFormat, displayFormat, displayFormat);
			JSpinner.NumberEditor editor = new JSpinner.NumberEditor(jSpinner,format);
			editor.getTextField().setFormatterFactory(factory);
			jSpinner.setEditor(editor);
		

		add(jSpinner);
		this.timed = timed;
	}
	private Integer checked(Integer value, int min, int max) {
		if(value != null) {
			if(value.intValue() < min) {
				return min;
			}
			if(value > max) {
				return max;
			}
		}
		return value;
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

