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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;


/**
 * Panel for {@link Color} properties using a {@link JColorChooser} that pops up on pressing the button.
 * @author Michael Heinzelmann
 */
public class ColorPropertyPanel extends PropertyPanel<Color> {

	private static final long serialVersionUID = 1L;
	private JButton colorButton = new JButton();
	private JLabel colorLabel = new JLabel("");
	/**
	 * Constructor
	 * @param soundCanvas the canvas to work with
	 */
	public ColorPropertyPanel(SoundCanvas soundCanvas) {
		super(soundCanvas);
		colorLabel.setOpaque(true);
		JPanel valueSelect = new JPanel();
		valueSelect.setLayout(new GridLayout(1, 2));
		valueSelect.add(colorLabel);
		valueSelect.add(colorButton);
		add(valueSelect);
		colorButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				colorButton.setEnabled(false);
				Color newColor = JColorChooser.showDialog(null, getName(), getCurrentValue());
				colorButton.setEnabled(true);
				if(newColor != null) {
					colorLabel.setBackground(newColor);
					setNewValue(newColor);
				}
			}
		});
	}
	@Override
	public void setCurrentValue(Color currentValue) {
		super.setCurrentValue(currentValue);
		colorLabel.setBackground(currentValue);
		this.repaint();
	}

}

