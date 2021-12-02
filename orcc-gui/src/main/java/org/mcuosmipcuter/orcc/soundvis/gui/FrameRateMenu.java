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
import javax.swing.JRadioButtonMenuItem;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.gui.Configuration;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;

/**
 * Specialized menu to show the canvas class names
 * @author Michael Heinzelmann
 */
public class FrameRateMenu extends JMenu  implements Context.Listener{

	private static final long serialVersionUID = 1L;

	/**
	 * Same constructor as for standard menus
	 * @param title menu title
	 */
	public FrameRateMenu(String title, final int initialFrameRate) {
		super(title);
		ButtonGroup group = new ButtonGroup();
		for(final int frameRate : Configuration.FRAME_RATES) {
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(frameRate + " fps");
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						Context.setOutputFrameRate(frameRate);
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			});
			if(frameRate == initialFrameRate) {
				item.setSelected(true);
			}
			group.add(item);
			add(item);
		}
	}

	/**
	 * Check and set the frame rates according to audio input
	 * @param audioInputInfo
	 */
	public void checkFrameRatesEnabled(AudioInputInfo audioInputInfo) {
		float sampleRate = audioInputInfo.getAudioFormat().getSampleRate();
		int pos = 0;
		for(int frameRate : Configuration.FRAME_RATES) {
			boolean isWorkingFrameRate = sampleRate % frameRate == 0;
			getItem(pos).setEnabled(isWorkingFrameRate);
			pos++;
		}
	}
	
	@Override
	public void contextChanged(PropertyName propertyName) {
		if(PropertyName.VideoFrameRate.equals(propertyName)) {
			int f = Context.getVideoOutputInfo().getFramesPerSecond();
			int c = 0;
			for(final int  frameRate : Configuration.FRAME_RATES) {
				getItem(c++).setSelected(frameRate == f);
			}
		}
	}
}

