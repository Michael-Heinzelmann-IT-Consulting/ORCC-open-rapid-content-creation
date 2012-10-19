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

/**
 * Listener to communicate with the table
 * @author Michael Heinzelmann
 */
public interface CustomTableListener {
	/**
	 * A frame range limit has been set
	 */
	public void frameSet();
	/**
	 * Row has been selected or un selected by the user
	 * @param selected whether selected or not
	 */
	public void rowSelected(boolean selected);
	/**
	 * Tells the table which frame is currently selected in the time line
	 * @return the selected frame
	 */
	public long getFrameSelected();
}
