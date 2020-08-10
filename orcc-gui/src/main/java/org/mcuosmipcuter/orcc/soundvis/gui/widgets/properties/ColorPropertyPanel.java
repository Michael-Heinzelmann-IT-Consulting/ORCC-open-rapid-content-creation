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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;


/**
 * Panel for {@link Color} properties using a {@link JColorChooser} that pops up on pressing the button.
 * @author Michael Heinzelmann
 */
public class ColorPropertyPanel extends PropertyPanel<Color> {

	private static final long serialVersionUID = 1L;
	private JButton colorButton = new JButton("...");
	final JColorChooser chooser = new JColorChooser();
	boolean expanded;
	JPanel valueSelect = new JPanel();
	
	/**
	 * Constructor
	 * @param soundCanvas the canvas to work with
	 */
	public ColorPropertyPanel(SoundCanvasWrapper soundCanvasWrapper, Object valueOwner) {
		super(soundCanvasWrapper, valueOwner);

		colorButton.setPreferredSize(new Dimension(50, 15));
		valueSelect.setLayout(new BorderLayout());
		valueSelect.add(colorButton, BorderLayout.WEST);
		colorButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Color newColor = JColorChooser.showDialog(ColorPropertyPanel.this, "Color", Color.BLACK);
				if(newColor != null) {
					setNewValue(newColor);
					valueSelect.setBackground(newColor);
				}
			}
		});
		

		add(valueSelect);

		chooser.setPreviewPanel(new JPanel());

	}
	@Override
	public void setCurrentValue(Color currentValue) {
		super.setCurrentValue(currentValue);
		valueSelect.setBackground(currentValue);
		this.repaint();
	}

}

