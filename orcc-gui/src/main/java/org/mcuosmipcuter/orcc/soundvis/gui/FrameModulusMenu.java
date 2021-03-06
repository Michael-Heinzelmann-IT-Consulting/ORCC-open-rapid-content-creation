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

import org.mcuosmipcuter.orcc.soundvis.RealtimeSettings;

/**
 * Specialized menu showing video frame reduction
 * @author Michael Heinzelmann
 */
public class FrameModulusMenu extends JMenu{

	private static final long serialVersionUID = 1L;

	private int[] reductions = new int[]{1, 2, 4, 8, 16};
	
	/**
	 * New menu
	 * @param title menu title
	 * @param initialreduction initial reduction
	 * @param realtimeSettings
	 */
	public FrameModulusMenu(String title, final int initialreduction, final RealtimeSettings realtimeSettings) {
		super(title);
		ButtonGroup group = new ButtonGroup();
		for(final int reduction : reductions) {
			final String text = reduction == 1 ? "none" : reduction + " frames";
			final JMenuItem item = new JRadioButtonMenuItem(text);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					realtimeSettings.setVideoRefresh(reduction);
				}
			});
			if(reduction == initialreduction) {
				item.setSelected(true);
			}
			group.add(item);
			add(item);
		}
	}
}

