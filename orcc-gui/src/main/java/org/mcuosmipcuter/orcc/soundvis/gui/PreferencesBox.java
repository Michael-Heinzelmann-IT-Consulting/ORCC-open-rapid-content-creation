/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2021 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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

import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.soundvis.persistence.FileConfiguration;

/**
 * @author Michael Heinzelmann
 *
 */
public class PreferencesBox {

	/**
	 * 
	 */
	public PreferencesBox() {
		// TODO Auto-generated constructor stub
	}
	public static void showPreferncesDialog() {
		Properties config = FileConfiguration.getProperties();
		JPanel panelAskApdir = new JPanel();
		JCheckBox ask = new JCheckBox();
		ask.setSelected("true".equals(config.get(FileConfiguration.SOUNDVIS_PROPERTY_ASK_APP_DIR_ON_STARTUP)));
		ask.addChangeListener(new ChangeListener() {		
			@Override
			public void stateChanged(ChangeEvent e) {
				config.setProperty(FileConfiguration.SOUNDVIS_PROPERTY_ASK_APP_DIR_ON_STARTUP, String.valueOf(ask.isSelected()));
				FileConfiguration.storeProperties(config);
			}
		});
		panelAskApdir.add(new JLabel("ask for appdir on startup: "));
		panelAskApdir.add(ask);
		
		JPanel panelLookAndFeel = new JPanel();
		panelLookAndFeel.add(new JLabel("look and feel: "));
		ButtonGroup lfGroug = new ButtonGroup();
		Object configuredLf = config.get(FileConfiguration.SOUNDVIS_PROPERTY_LOOK_AND_FEEL);
		for(LookAndFeelInfo lfi : UIManager.getInstalledLookAndFeels()) {
			JRadioButton lf = new JRadioButton(lfi.getName(), lfi.getClassName().equals(configuredLf));
			lf.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if(lf.isSelected()) {
						config.setProperty(FileConfiguration.SOUNDVIS_PROPERTY_LOOK_AND_FEEL, lfi.getClassName());
						FileConfiguration.storeProperties(config);
					}
				}
			});
			lfGroug.add(lf);
			panelLookAndFeel.add(lf);
		}
		
		Object[] array = {panelAskApdir, panelLookAndFeel}; 
		JOptionPane jp = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE);		
		jp.setOptionType(JOptionPane.DEFAULT_OPTION);
		JDialog jd = jp.createDialog("preferences");
		
		jd.setVisible(true);
	}
}
