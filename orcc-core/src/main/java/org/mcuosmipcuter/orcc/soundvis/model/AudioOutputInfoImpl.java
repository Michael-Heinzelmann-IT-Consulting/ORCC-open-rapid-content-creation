/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2020 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.soundvis.model;

import org.mcuosmipcuter.orcc.api.soundvis.AudioLayout;
import org.mcuosmipcuter.orcc.api.soundvis.AudioOutputInfo;

/**
 * @author Michael Heinzelmann
 */
public class AudioOutputInfoImpl implements AudioOutputInfo {
	private final AudioLayout audioLayout;
	/**
	 * output with layout
	 */
	public AudioOutputInfoImpl(AudioLayout audioLayout) {
		this.audioLayout = audioLayout;
	}

	@Override
	public AudioLayout getLayOut() {
		return audioLayout;
	}

}
