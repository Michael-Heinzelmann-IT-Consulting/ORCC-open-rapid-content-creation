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

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.SessionToken;

/**
 * @author Michael Heinzelmann
 *
 */
public class ChangsBox implements Listener{

	JTextArea ta = new JTextArea(10, 80);
	JScrollPane sp = new JScrollPane(ta);
	JDialog jd;
	/**
	 * 
	 */
	public ChangsBox() {
		ta.setEditable(false);
		Object[] array = {sp}; 
		JOptionPane jp = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE);		
		jd = jp.createDialog("open changes");
	}
	public void showUnsavedChanges(boolean modal) {

		try {
			update();
			ta.setCaretPosition(ta.getText().length());
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
	private void update() {
		StringBuilder stringBuilder = new StringBuilder();
		SessionToken st = Context.getSessionToken();
		if(st != null) {
			String name = st.isNamed() ? st.getFullPath() : "unnamed";
			stringBuilder.append("Session: " + name);
			stringBuilder.append("\n");
			for(String change : st.getChangeLog(true)) {
				stringBuilder.append(change);
				stringBuilder.append("\n");
			}
		}
		ta.setText(stringBuilder.toString());
	}
	@Override
	public void contextChanged(PropertyName propertyName) {
		if(jd.isVisible() && PropertyName.SessionChanged == propertyName) {
			update();
			sp.revalidate();
		}
	}

}
