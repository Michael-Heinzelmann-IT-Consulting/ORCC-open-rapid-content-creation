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

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties.PropertyPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties.PropertyPanelFactory;


/**
 * Panel to display the canvas properties editors, it updates itself by being a {@link Context.Listener}
 * @author Michael Heinzelmann
 */
public class CanvasPropertyPanel extends JPanel implements Context.Listener {
	
	private static final long serialVersionUID = 1L;
	/**
	 * Sets up a grid layout
	 */
	public CanvasPropertyPanel() {
		setBorder(new LineBorder(Color.WHITE, 5));
		GridLayout gl = new GridLayout(10, 1, 5, 4);		
		setLayout(gl);
	}
	
	@Override
	public void contextChanged(PropertyName propertyName) {
		if(PropertyName.SoundCanvas.equals(propertyName)) {
			removeAll();
			Set<PropertyPanel<?>> props = PropertyPanelFactory.getCanvasPanels(Context.getSoundCanvas());

			for(final PropertyPanel<?> p : props) {
				add(p);
			}
			revalidate();
			repaint();
		}
	}
}
