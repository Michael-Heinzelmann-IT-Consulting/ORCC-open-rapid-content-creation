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
package org.mcuosmipcuter.orcc.gui.table;

import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.CanvasPropertyPanel;

/**
 * Row for the custom table {@link CustomTable}
 * @author Michael Heinzelmann
 */
public class Row extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private final SoundCanvasWrapper soundCanvasWrapper;
	private final CanvasPropertyPanel canvasPropertyPanel;
	
	/**
	 * New row containing the given wrapped canvas
	 * @param soundCanvasWrapper the wrapped canvas that belongs to this layer row
	 */
	public Row(SoundCanvasWrapper soundCanvasWrapper) {
		this.soundCanvasWrapper = soundCanvasWrapper;
		this.canvasPropertyPanel = new CanvasPropertyPanel(soundCanvasWrapper);
	}

	/**
	 * Gets the panel (for showing it to the user)
	 * @return
	 */
	public CanvasPropertyPanel getCanvasPropertyPanel() {
		return canvasPropertyPanel;
	}

	/**
	 * Gets the wrapped canvas (for updating the model)
	 * @return
	 */
	public SoundCanvasWrapper getSoundCanvasWrapper() {
		return soundCanvasWrapper;
	}
	
	
}
