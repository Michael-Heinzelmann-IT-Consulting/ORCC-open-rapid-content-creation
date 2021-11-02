/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2020 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.soundvis.gui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.util.TextHelper;

/**
 * @author Michael Heinzelmann
 *
 */
public class WidgetUtil  {

	public static JPanel getMessagePanel(String msg, int fontSize, Graphics graphics) {
		JPanel popUpContentPanel = new JPanel();
		popUpContentPanel.setBackground(Color.YELLOW);
		popUpContentPanel.setBorder(new  LineBorder(Color.YELLOW, 8, false));
		JLabel label = new JLabel(msg);
		Font font = new Font("dialog", Font.PLAIN, fontSize);
		label.setOpaque(true);
		label.setFont(font);
		label.setForeground(Color.RED);
		label.setBackground(Color.YELLOW);
		Graphics copy = graphics.create();
		copy.setFont(font);
		label.setPreferredSize(TextHelper.getTextDimesion(new String[] {msg}, copy));
		popUpContentPanel.add(label);

		return popUpContentPanel;
	}
	
	public static JSpinner getIntegerSpinner(int value, int minimum, int maximum, int stepSize, Unit unit, NumberMeaning numberMeaning) {
		SpinnerNumberModel model = new SpinnerNumberModel(value, minimum, maximum, stepSize);
		JSpinner jSpinner = new JSpinner(model);
		String format;

		switch(unit != null ? unit : Unit.OTHER) {
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
		
		return jSpinner;
	}
	private static Integer checked(Integer value, int min, int max) {
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

}
