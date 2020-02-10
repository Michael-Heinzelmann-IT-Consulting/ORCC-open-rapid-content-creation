/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2014 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.api.util;

import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;

/**
 * @author Michael Heinzelmann
 *
 */
public class DimensionHelper {
	final int videoWidth;
	final int videoHeight;
	public DimensionHelper(VideoOutputInfo videoOutputInfo) {
		videoWidth = videoOutputInfo.getWidth();
		videoHeight = videoOutputInfo.getHeight();
	}
	public int realX(int percentageX){
		return Math.round(videoWidth / 100f * percentageX);
	}
	public int realY(int percentageY){
		return Math.round(videoHeight / 100f * percentageY);
	}
	public int getVideoWidth() {
		return videoWidth;
	}
	public int getVideoHeight() {
		return videoHeight;
	}

}
