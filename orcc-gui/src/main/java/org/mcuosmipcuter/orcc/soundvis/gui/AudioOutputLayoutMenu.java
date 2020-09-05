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

import org.mcuosmipcuter.orcc.api.soundvis.AudioLayout;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;

/**
 * Specialized menu to show the audio output layout
 * @author Michael Heinzelmann
 */
public class AudioOutputLayoutMenu extends JMenu  implements Context.Listener{

	private static final long serialVersionUID = 1L;	

	/**
	 * Same constructor as for standard menus
	 * @param title menu title
	 */
	public AudioOutputLayoutMenu(String title) {
		super(title);
		ButtonGroup group = new ButtonGroup();
		for(final AudioLayout al : AudioLayout.values()) {
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(al.name());
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Context.setAudioOutputLayout(al);
				}
			});
			if(al == AudioLayout.COMPRESSED) {
				item.setSelected(true);
			}
			group.add(item);
			add(item);
		}
	}
	
	@Override
	public void contextChanged(PropertyName propertyName) {
		if(PropertyName.AudioOutputInfo.equals(propertyName)) {
			AudioLayout current = Context.getAudioOutputInfo().getLayOut();
			for(int i = 0; i < getItemCount(); i++) {
				JMenuItem item = getItem(i);
				item.setSelected(current.name().equals(item.getText()));
			}
		}
	}
}

