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

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;

/**
 * Intermediate interface for controller components
 * @author Michael Heinzelmann
 */
public interface Mixin extends DecodingCallback {
	/**
	 * Start the processing with the given parameters
	 * @param audioInputInfo audio input
	 * @param videoOutputInfo the output to produce
	 */
	public void start(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo);
	/**
	 * Process a new frame
	 * @param frameCount current the frame number
	 */
	void newFrame(long frameCount);
}
