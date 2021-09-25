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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.persistence.FileConfiguration;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Utility to show the about box
 * @author Michael Heinzelmann
 */
public class AboutBox {
	/**
	 * Show modal about box using the /license.txt resource file as model
	 * and  {@link JTextArea} {@link JScrollPane} and a {@link JOptionPane} view components
	 */
	public static void showFileText(String filePath, boolean modal) {
		InputStream is = AboutBox.class.getResourceAsStream(filePath);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
		try {
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while((line = bufferedReader.readLine()) != null){
				stringBuilder.append(line);
				stringBuilder.append("\n");
			}
			JTextArea ta = new JTextArea(20, 50);
			JScrollPane sp = new JScrollPane(ta);
			ta.setText(stringBuilder.toString());
			ta.setCaretPosition(0);
			Object[] array = {sp}; 
			JOptionPane jp = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE);		
			JDialog jd = jp.createDialog("ORCC is free software and comes WITHOUT ANY WARRANTY see license");
			jd.setModal(modal);
			jd.setAlwaysOnTop(!modal);
			jd.setVisible(true);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		finally {
			IOUtil.safeClose(bufferedReader);
		}
	}
	public static void showSystemProperties(boolean modal) {
		try {

			Properties vp = Context.getVersionProperties();

			StringBuilder stringBuilder = new StringBuilder();
			
			for(Entry<Object, Object> ee : vp.entrySet()){
				stringBuilder.append(ee.getKey() + "=" + ee.getValue());
				stringBuilder.append("\n");
			}
			stringBuilder.append("bootDir=" + FileConfiguration.getBootDir());
			stringBuilder.append("\n");
			stringBuilder.append("targetConfDir=" + FileConfiguration.getTargetConfDir());
			stringBuilder.append("\n");
			stringBuilder.append("appDir=" + FileConfiguration.getAppDir());
			stringBuilder.append("\n");
			
			stringBuilder.append("\n");
			List<String> envList = new ArrayList<>();
			Map <String, String> envMap = System.getenv();
			for(Entry<String, String> ee : envMap.entrySet()){
				envList.add(ee.getKey() + "=" + ee.getValue());
			}
			envList.sort(null);
			for(String s : envList) {
				stringBuilder.append(s);
				stringBuilder.append("\n");
			}
			
			stringBuilder.append("\n");
			List<String> propList = new ArrayList<>();
			Properties properties = System.getProperties();
			for(Entry<Object, Object> pe : properties.entrySet()){
				propList.add(pe.getKey() + "=" + pe.getValue());
			}
			propList.sort(null);
			for(String s : propList) {
				stringBuilder.append(s);
				stringBuilder.append("\n");
			}
			JTextArea ta = new JTextArea(20, 50);
			JScrollPane sp = new JScrollPane(ta);
			ta.setText(stringBuilder.toString());
			ta.setCaretPosition(0);
			Object[] array = {sp}; 
			JOptionPane jp = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE);		
			JDialog jd = jp.createDialog("ORCC is free software and comes WITHOUT ANY WARRANTY see license");
			jd.setModal(modal);
			jd.setAlwaysOnTop(!modal);
			jd.setVisible(true);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		finally {
			
		}
	}
}
