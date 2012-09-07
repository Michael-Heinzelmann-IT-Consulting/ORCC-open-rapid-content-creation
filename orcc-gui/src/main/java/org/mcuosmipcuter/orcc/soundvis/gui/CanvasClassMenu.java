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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.mcuosmipcuter.orcc.soundvis.Context;

/**
 * Specialized menu to show the canvas class names
 * @author Michael Heinzelmann
 */
public class CanvasClassMenu extends JMenu {

	private static final long serialVersionUID = 1L;


	/**
	 * Same constructor as for standard menus
	 * @param title menu title
	 */
	public CanvasClassMenu(String title) {
		super(title);

		ButtonGroup group = new ButtonGroup();
		for(final String className : Context.getCanvasClassNames()) {
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(className);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
				try {
					Context.setCanvas(className);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				}
			});

			add(item);
			group.add(item);
		}
	}
	
	/**
	 * Set a class name as pre selected
	 * @param className the class name to select
	 */
	public void setClassName(String className) {

		for(int i = 0; i < getItemCount(); i++) {
			JMenuItem item = getItem(i);
			if(className.equals(item.getText())) {
				item.setSelected(true);
			}

		}
	}

}

