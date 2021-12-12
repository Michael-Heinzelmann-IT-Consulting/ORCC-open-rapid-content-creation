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
import java.util.HashMap;

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
	
	private static HashMap<String, String> class2tooltip = new HashMap<>();
	
	static {
		class2tooltip.put(CANVAS_PACKAGE, CANVAS_PACKAGE);
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.AudioWave", "ampilitude wave of the sound input with progress");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Blinds", "opening/closing blinds for transitions and effects");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Blinker", "pulsating circles similar to a loudspeaker");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Chameleon", "color changes depending on sound input");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.ClassicWaves", "horizontal amplitude wave form");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.ColorsLR", "quadrant color changes depending on sound input");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.GridPulse", "grid moving depending on sound input");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Pulsating", "pulsating shapes depending on sound input");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.RotatingAmplitudes", "rotating amplitude wave form");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Shutter", "opening/closing shutter for transitions and effects");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.SlideShow", "shows images in sequence supports also transitions");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.SolidColor", "one color for background or effects with transitions");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Text", "displays text from simple static to complex animations");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.ThresholdVerticalLines", "draws lines depending on the ampilitude threshold");
		class2tooltip.put("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Tiles", "fills screen with tiles colored depending on sound input");
	}

	/**
	 * Same constructor as for standard menus
	 * @param title menu title
	 */
	public CanvasClassMenu(String title) {
		super(title);

		for(final String className : Context.getCanvasClassNames()) {
			final JMenuItem item = new JMenuItem(className.substring(CANVAS_PACKAGE.length() + 1));
			String tp = class2tooltip.get(className);
			item.setToolTipText(tp != null ? tp : className);
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

