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
package org.mcuosmipcuter.orcc.soundvis;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;

/**
 * Decorator / wrapper of a sound canvas, adding methods for internal use
 * @author Michael Heinzelmann
 */
public interface SoundCanvasWrapper extends SoundCanvas {
	/**
	 * Gets the name that should be used for displaying
	 * @return the name
	 */
	public String getDisplayName();
	/**
	 * Whether the canvas is visible
	 * @return true if visible
	 */
	public boolean isVisible();
	/**
	 * Set true if the canvas rendering should be visible
	 * @param visible
	 */
	public void setVisible(boolean visible);
	
	public long getFrameFrom();
	
	public long getFrameTo();
	
	public void setFrameFrom(long frameFrom);
	
	public void setFrameTo(long frameTo);
	
	/**
	 * Get the wrapped canvas for direct work e.g. reflection
	 * @return the canvas that is wrapped
	 */
	public SoundCanvas getSoundCanvas();
}
