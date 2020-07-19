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
package org.mcuosmipcuter.orcc.soundvis.persistence;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mcuosmipcuter.orcc.api.soundvis.MappedValue;
import org.mcuosmipcuter.orcc.soundvis.AudioInput.Type;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.util.IOUtil;

public class Session implements Serializable {
	
	/**
	 * versioning
	 */
	private static final long serialVersionUID = 1L;
	
	public static boolean restoreSession() {
		File file = new File("latest_session.xml");
		IOUtil.log("restore from: " + file.getAbsolutePath());
		try (FileInputStream fis = new FileInputStream(file);
				XMLDecoder in = new XMLDecoder(fis);) {
			PersistentSession persistentSession = (PersistentSession) in.readObject();
			Type inputType = persistentSession.getAudioInputType();
			switch(inputType) {
			case FILE:
					Context.setAudioFromFile(persistentSession.getAudioInputName());
					break;
				case STREAM:
					Context.setAudioFromClasspath(persistentSession.getAudioInputName());
					break;
				default:
					throw new IllegalArgumentException();
			}
			
			Context.setOutputDimension(persistentSession.getVideoOutPutWidth(), persistentSession.getVideoOutPutHeight());
			Context.setOutputFrameRate(persistentSession.getVideoOutPutFrames());
			
			for(PersistentSoundCanvasWrapper psw : persistentSession.getSoundCanvasList()) {
				Context.addCanvasWrapper(psw.restore());
			}
		} catch (Exception e) {
			IOUtil.log("restore failed: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	public static void saveSession() throws IllegalArgumentException, IllegalAccessException {

		List<SoundCanvasWrapper> appObjects = Context.getSoundCanvasList();
		
		List<PersistentSoundCanvasWrapper> persistentWrappers = new ArrayList<PersistentSoundCanvasWrapper>();
		
		for(SoundCanvasWrapper sw : appObjects) {
			PersistentSoundCanvasWrapper psw = new PersistentSoundCanvasWrapper(sw);
			persistentWrappers.add(psw);
		}

			PersistentSession persistentSession = new PersistentSession();
			persistentSession.setSoundCanvasList(persistentWrappers);
			persistentSession.setAudioInputType(Context.getAudioInput().getType());
			persistentSession.setAudioInputName(Context.getAudioInput().getName());
			persistentSession.setVideoOutPutFrames(Context.getVideoOutputInfo().getFramesPerSecond());
			persistentSession.setVideoOutPutHeight(Context.getVideoOutputInfo().getHeight());
			persistentSession.setVideoOutPutWidth(Context.getVideoOutputInfo().getWidth());
		
		File file = new File("latest_session.xml");

		try (FileOutputStream out = new FileOutputStream(file); 
				XMLEncoder encoder = new XMLEncoder(out)) {
			encoder.setPersistenceDelegate(MappedValue.class, new MappedValuePersistenceDelegate());
			encoder.writeObject(persistentSession);
			encoder.flush();
			IOUtil.log("saved: " + file.getAbsolutePath());
		} catch (IOException e) {
			IOUtil.log(e.getMessage());
		}
	}

}
