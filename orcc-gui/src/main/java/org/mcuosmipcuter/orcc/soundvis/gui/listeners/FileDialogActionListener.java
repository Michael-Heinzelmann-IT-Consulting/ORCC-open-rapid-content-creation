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
package org.mcuosmipcuter.orcc.soundvis.gui.listeners;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * action listener that brings up a file dialog and calls back if a file was selected.
 * @author Michael Heinzelmann
 */
public class FileDialogActionListener implements ActionListener {
	public interface CallBack {
		public void fileSelected(File file);
	}
	private final Component owner;
	private final CallBack callBack;
	private final String buttonText;
	private final JFileChooser chooser = new JFileChooser();

	/**
	 * New listener
	 * @param owner the owner of the dialog that will be shown
	 * @param callBack the call back to notify when a file was selected
	 * @param buttonText text to show on the dialog button
	 */
	public FileDialogActionListener(Component owner, CallBack callBack, String buttonText) {
		this.owner = owner;
		this.callBack = callBack;
		this.buttonText = buttonText;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	
        int returnVal = chooser.showDialog(owner, buttonText);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
        	callBack.fileSelected(chooser.getSelectedFile());
        }
	}

}
