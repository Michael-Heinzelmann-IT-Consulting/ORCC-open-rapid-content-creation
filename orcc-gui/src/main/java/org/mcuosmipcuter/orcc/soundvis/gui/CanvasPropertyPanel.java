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
package org.mcuosmipcuter.orcc.soundvis.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties.PropertyPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties.PropertyPanelFactory;


/**
 * Panel to display the canvas properties editors, it updates itself by being a {@link Context.Listener}
 * @author Michael Heinzelmann
 */
public class CanvasPropertyPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sets up a grid layout
	 */
	public CanvasPropertyPanel(final SoundCanvasWrapper soundCanvasWrapper) {

		GridLayout gl = new GridLayout(10, 1, 5, 4);		
		setLayout(gl);
		
		SoundCanvas soundCanvas = soundCanvasWrapper.getSoundCanvas();
		Set<PropertyPanel<?>> props = PropertyPanelFactory.getCanvasPanels(soundCanvas);

		for(final PropertyPanel<?> p : props) {
			add(p);
		}
	}
	
}
