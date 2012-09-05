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
package org.mcuosmipcuter.orcc.api.soundvis;

/**
 * Facade for a canvas background that can draw the background in different ways,
 * like image, solid color etc. If the canvas wants the background be drawn externally
 * it should call this facade {@link #drawBackGround()} method.
 * Canvas that take full responsibility for the background can just ignore
 * this interface.
 * @author Michael Heinzelmann
 */
public interface CanvasBackGround {
	/**
	 * Draws the background in a specific way that is determined by the user
	 */
	public void drawBackGround();
}