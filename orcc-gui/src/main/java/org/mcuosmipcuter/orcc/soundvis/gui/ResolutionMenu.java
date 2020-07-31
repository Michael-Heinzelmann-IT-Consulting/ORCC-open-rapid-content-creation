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
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;

/**
 * Specialized menu showing video resolution
 * @author Michael Heinzelmann
 */
public class ResolutionMenu extends JMenu implements Context.Listener{

	private static final long serialVersionUID = 1L;

	private int[][] resolutions = new int[][] {
			new int[] {192, 108},
			new int[] {160, 120},
			new int[] {240, 180},
			new int[] {320, 240},
			new int[] {480, 360},
			new int[] {640, 480},
			new int[] {720, 576},
			new int[] {960, 540},
			new int[] {1280, 720},
			new int[] {1920, 1080}
	};
	
	/**
	 * New menu
	 * @param title menu title
	 * @param initialWidth initial width of the video
	 * @param initialHeight initial height of the video
	 */
	public ResolutionMenu(String title, final int initialWidth, final int initialHeight) {
		super(title);
		ButtonGroup group = new ButtonGroup();
		for(final int [] r : resolutions) {
			final JMenuItem item = new JRadioButtonMenuItem(r[0] + "x" + r[1]);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
				Context.setOutputDimension(r[0], r[1]);
				}
			});
			if(r[0] == initialWidth && r[1] == initialHeight) {
				item.setSelected(true);
			}
			group.add(item);
			add(item);
		}
	}

	@Override
	public void contextChanged(PropertyName propertyName) {
		if(PropertyName.VideoDimension.equals(propertyName)) {
			int w = Context.getVideoOutputInfo().getWidth();
			int h = Context.getVideoOutputInfo().getHeight();
			int c = 0;
			for(final int [] r : resolutions) {
				getItem(c++).setSelected(r[0] == w && r[1] == h);
			}
		}
	}
}

