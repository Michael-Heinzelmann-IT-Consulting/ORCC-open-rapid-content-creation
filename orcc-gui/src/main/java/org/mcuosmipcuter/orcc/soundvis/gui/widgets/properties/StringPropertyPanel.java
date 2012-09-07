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

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;


/**
 *  Panel for string properties using a {@link JTextArea} {@link JScrollPane} and a {@link JOptionPane}
 * @author Michael Heinzelmann
 */
public class StringPropertyPanel extends PropertyPanel<String> {
	
	private static final long serialVersionUID = 1L;
	
	private JLabel textLabel = new JLabel();
	private JButton button = new JButton("...");
	/**
	 * Constructor
	 * @param soundCanvas the canvas to work with
	 */
	public StringPropertyPanel(SoundCanvas soundCanvas) {
		super(soundCanvas);
		JPanel valueSelect = new JPanel();
		valueSelect.setLayout(new GridLayout(1, 2));
		textLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
		valueSelect.add(textLabel);
		valueSelect.add(button);
		add(valueSelect);
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JTextArea ta = new JTextArea(20, 80);
				ta.setText(getCurrentValue());
				JScrollPane sp = new JScrollPane(ta);
				Object[] array = {getName(), sp}; 
				int res = JOptionPane.showConfirmDialog(null, array, "set value for string property", 
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if(res == JOptionPane.OK_OPTION) {
					setNewValue(ta.getText());
				}
			}
		});
	}

	@Override
	public void setCurrentValue(String currentValue) {
		super.setCurrentValue(currentValue);
		textLabel.setText(currentValue);
	}

}
