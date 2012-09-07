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
package org.mcuosmipcuter.orcc.soundvis.util;

import java.lang.reflect.Field;

import javax.annotation.Resource;

import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
import org.mcuosmipcuter.orcc.soundvis.Renderer;
import org.mcuosmipcuter.orcc.soundvis.model.AudioFileInputImpl;

/**
 * Helper that checks for the external libraries and returns an export implementation.
 * @author Michael Heinzelmann
 */
public class ExportUtil {
	
	/**
	 * Checks the classpath whether we have appropriate export libraries
	 * @return true if export is enabled
	 */
	public static boolean isExportEnabled() {
		try {
			Class.forName("com.xuggle.mediatool.IMediaWriter");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Return a {@link PlayPauseStop} that will perform an export.
	 * @param renderer renderer to work with
	 * @return the implementation
	 */
	public static PlayPauseStop getExportPlayPause(Renderer renderer) {
		try {
			PlayPauseStop e = (PlayPauseStop) Class.forName("org.mcuosmipcuter.orcc.ert.xuggler.ExportThread").newInstance();
			for(Field field : e.getClass().getDeclaredFields()) {
				if(field.isAnnotationPresent(Resource.class)) {
					field.setAccessible(true);
					field.set(e, renderer);
				}
			}
			return e;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	public static AudioInput getAudioInputInfoFromFile(String audioFileName) {
		return new AudioFileInputImpl(audioFileName);	
	}
}