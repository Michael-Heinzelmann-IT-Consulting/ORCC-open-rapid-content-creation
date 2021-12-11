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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
	public static void showPreferncesDialog(Component parent) {
		Properties config = FileConfiguration.getProperties();
		
		JPanel panelLookAndFeel = new JPanel();
		panelLookAndFeel.add(new JLabel("look and feel: "));
		ButtonGroup lfGroug = new ButtonGroup();
		Object configuredLf = config.get(FileConfiguration.SOUNDVIS_PROPERTY_LOOK_AND_FEEL);
		for(LookAndFeelInfo lfi : UIManager.getInstalledLookAndFeels()) {
			JRadioButton lf = new JRadioButton(lfi.getName(), lfi.getClassName().equals(configuredLf));
			lf.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(lf.isSelected()) {
						config.setProperty(FileConfiguration.SOUNDVIS_PROPERTY_LOOK_AND_FEEL, lfi.getClassName());
						FileConfiguration.storeProperties(config);
					}
				}
			});
			lfGroug.add(lf);
			panelLookAndFeel.add(lf);
		}
		JPanel panelSize = new JPanel();
		JCheckBox maximized = new JCheckBox();
		maximized.setSelected("true".equals(config.get(FileConfiguration.SOUNDVIS_PROPERTY_APP_SIZE_MAXIMIZED)));
		maximized.addItemListener(new ItemListener() {		
			@Override
			public void itemStateChanged(ItemEvent e)  {
				config.setProperty(FileConfiguration.SOUNDVIS_PROPERTY_APP_SIZE_MAXIMIZED, String.valueOf(maximized.isSelected()));
				FileConfiguration.storeProperties(config);
			}
		});

		panelSize.add(new JLabel("window maximized on startup: "));
		panelSize.add(maximized);
		
		Object[] array = {panelLookAndFeel, panelSize}; 
		JOptionPane jp = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE);		
		jp.setOptionType(JOptionPane.DEFAULT_OPTION);
		JDialog jd = jp.createDialog(parent, "preferences");
		
		jd.setVisible(true);
	}
}
