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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.gui.table.Row;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;


/**
 * Panel for {@link Color} properties using a {@link JColorChooser} that pops up on pressing the button.
 * @author Michael Heinzelmann
 */
public class ColorPropertyPanel extends PropertyPanel<Color> {

	private static final long serialVersionUID = 1L;
	private JButton colorButton = new JButton(" + ");
	private JLabel colorLabel = new JLabel("      ");
	final JColorChooser chooser = new JColorChooser();
	boolean expanded;
	/**
	 * Constructor
	 * @param soundCanvas the canvas to work with
	 */
	public ColorPropertyPanel(SoundCanvasWrapper soundCanvasWrapper) {
		super(soundCanvasWrapper);
		colorLabel.setOpaque(true);
		JPanel valueSelect = new JPanel();
		//valueSelect.setLayout(new GridLayout(1, 2));
		valueSelect.add(colorLabel);
		valueSelect.add(colorButton);
		colorButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//chooser.setPreferredSize(new Dimension(660, 200));
				System.err.println(" m  l l  " + chooser.getMinimumSize());
				chooser.setMinimumSize(new Dimension(460, 180));
				chooser.setPreferredSize(new Dimension(460, 180));
				if(expanded) {
					remove(chooser);
					//add(colorLabel);
					colorButton.setText(" + ");
				}
				else {
					//remove(colorLabel);
					add(chooser);
					colorButton.setText(" - ");
				}
				ColorPropertyPanel.this.revalidate();
				((Row)ColorPropertyPanel.this.getParent().getParent()).changeSize(ColorPropertyPanel.this);
				
				expanded = !expanded;
			}
		});
		

		//add(valueSelect);

		chooser.setPreviewPanel(new JPanel());
		chooser.getSelectionModel().addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				
				Color newColor = chooser.getColor();
				//System.err.println("newColor: " + newColor);
				if(newColor != null) {
					//colorLabel.setBackground(newColor);
					setNewValue(newColor);
				}
			}
		});

		//addSelectorComponent(chooser);
		addSelectorComponent(valueSelect);

	}
	@Override
	public void setCurrentValue(Color currentValue) {
		System.err.println("currentValue " + currentValue);
		super.setCurrentValue(currentValue);
		colorLabel.setBackground(currentValue);
		this.repaint();
	}
	@Override
	public void setField(Field field) {
		String name = field.getName();
		nameLabel.setText("");
		this.name = name;
	}
}

