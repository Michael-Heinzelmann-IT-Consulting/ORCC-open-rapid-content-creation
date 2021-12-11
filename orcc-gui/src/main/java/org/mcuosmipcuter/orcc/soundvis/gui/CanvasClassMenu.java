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

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.mcuosmipcuter.orcc.soundvis.Context;

/**
 * Specialized menu to show the canvas class names
 * @author Michael Heinzelmann
 */
public class CanvasClassMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	public final static String CANVAS_PACKAGE = "org.mcuosmipcuter.orcc.soundvis.defaultcanvas";

	/**
	 * Same constructor as for standard menus
	 * @param title menu title
	 */
	public CanvasClassMenu(String title) {
		super(title);

		for(final String className : Context.getCanvasClassNames()) {
			final JMenuItem item = new JMenuItem(className.substring(CANVAS_PACKAGE.length() + 1));
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						Context.addCanvas(className);
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			});

			add(item);
		}
	}

}

