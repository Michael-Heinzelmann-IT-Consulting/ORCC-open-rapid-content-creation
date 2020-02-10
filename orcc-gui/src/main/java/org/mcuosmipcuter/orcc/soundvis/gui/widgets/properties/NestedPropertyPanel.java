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
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;


/**
 * Panel to display nested canvas properties editors
 * @author Michael Heinzelmann
 */
public class NestedPropertyPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sets up a grid layout
	 */
	public NestedPropertyPanel(Set<PropertyPanel<?>> props) {

		setBackground(Color.YELLOW);
		setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		GridLayout gl = new GridLayout(props.size()/2 , 2, 5, 4);		
		setLayout(gl);

		for(final JPanel p : props) {
			add(p);
		}
	}
	
}
