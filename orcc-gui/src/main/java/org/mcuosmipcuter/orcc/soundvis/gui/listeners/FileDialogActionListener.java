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
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.mcuosmipcuter.orcc.gui.util.ExtensionsFileFilter;

/**
 * action listener that brings up a file dialog and calls back if a file was selected.
 * @author Michael Heinzelmann
 */
public class FileDialogActionListener implements ActionListener {
	public interface CallBack {
		public void fileSelected(File file);
	}
	public interface PreSelectCallBack {
		public File preSelected();
	}
	private final Component owner;
	private final CallBack callBack;
	private final String buttonText;
	private final JFileChooser chooser = new JFileChooser();
	private FileFilter fileFilter;
	private String forcedExtension;
	private Set<String> choosableExtensions = new HashSet<>();
	private PreSelectCallBack preSelectCallBack;

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
		if(fileFilter != null) {
			chooser.setFileFilter(fileFilter);
		}
		if(preSelectCallBack != null) {
			chooser.setSelectedFile(preSelectCallBack.preSelected());
		}
        int returnVal = chooser.showDialog(owner, buttonText);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
			File fileSelected = chooser.getSelectedFile();

			boolean hasChoosableExtension = false;
			for(String ext : choosableExtensions) {
				if(fileSelected.getAbsolutePath().endsWith(ext)) {
					hasChoosableExtension = true;
				}
			}
			File fileToUse;
			if(hasChoosableExtension) {
				fileToUse = fileSelected;
			}
			else {
				String filterExtension = chooser.getFileFilter() instanceof ExtensionsFileFilter ? 
						((ExtensionsFileFilter)chooser.getFileFilter()).getSingleExtension() : null;
				if(filterExtension != null) {
					fileToUse = new File(fileSelected.getAbsolutePath() + filterExtension);
				}
				else if(forcedExtension != null) {
					fileToUse = new File(fileSelected.getAbsolutePath() + forcedExtension);
				}
				else {
					fileToUse = fileSelected; // no extension
				}

			}
        	callBack.fileSelected(fileToUse);
        }
	}

	public void setFileFilter(FileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}

	public String getForcedExtension() {
		return forcedExtension;
	}

	public void setForcedExtension(String forcedExtension) {
		this.forcedExtension = forcedExtension;
	}

	public void setPreSelectCallBack(PreSelectCallBack preSelectCallBack) {
		this.preSelectCallBack = preSelectCallBack;
	}
	public void addChoosableFilter(ExtensionsFileFilter fileFilter) {
		chooser.addChoosableFileFilter(fileFilter);
		String ext = fileFilter.getSingleExtension();
		if(ext != null) {
			choosableExtensions.add(ext);
		}
	}


}
