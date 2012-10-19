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
package org.mcuosmipcuter.orcc.gui;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;

/**
 * Desktop manager to implement the restrictive behavior we need, no moving of frames, iconify playback on maximized graph
 * @author Michael Heinzelmann
 */
public class CustomDeskTopManager extends DefaultDesktopManager {

	private static final long serialVersionUID = 1L;
	
	final JInternalFrame frameToHide;
	final JInternalFrame frameOnTop;
	/**
	 * Construct a manager 
	 * @param frameToHide frame that should be hidden in favor of {@link #frameOnTop}
	 * @param frameOnTop see {@link #frameToHide}
	 */
	public CustomDeskTopManager(JInternalFrame frameToHide,
			JInternalFrame frameOnTop) {
		this.frameToHide = frameToHide;
		this.frameOnTop = frameOnTop;
	}
	@Override
	public void maximizeFrame(JInternalFrame f) {
		if(f == frameOnTop) {
			iconifyFrame(frameToHide);
		}
		super.maximizeFrame(f);
	}
	@Override
	public void minimizeFrame(JInternalFrame f) {
		if(f == frameOnTop) {
			deiconifyFrame(frameToHide);
		}
		super.minimizeFrame(f);
	}
	@Override
	public void beginDraggingFrame(JComponent f) {
	}
	@Override
	public void beginResizingFrame(JComponent f, int direction) {
	}
	@Override
	public void dragFrame(JComponent f, int newX, int newY) {
	}
	@Override
	public void endDraggingFrame(JComponent f) {
	}
	@Override
	public void endResizingFrame(JComponent f) {
	}
	
}
