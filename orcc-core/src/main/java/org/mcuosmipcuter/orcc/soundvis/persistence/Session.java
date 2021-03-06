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


import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.mcuosmipcuter.orcc.api.soundvis.MappedValue;
import org.mcuosmipcuter.orcc.soundvis.AppLogicException;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.ImageStore.Key;
import org.mcuosmipcuter.orcc.soundvis.SessionToken;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Transient session
 * @author Michael Heinzelmann
 */
public class Session implements Serializable {
	
	public final static String FILE_EXTENSION = ".xml";
	public final static String DEFAULT_FILE_NAME = "latest_session";
	public final static String DEFAULT_BACkUP_FILE_NAME = DEFAULT_FILE_NAME + "_bu";
	

	/**
	 * versioning
	 */
	private static final long serialVersionUID = 1L;
	
	
	public static void newSession() {
		Context.clearCanvasList();
		Context.setSessionToken(new SessionToken(null));
		try {
			Context.setAudioFromClasspath("/silence_pcm_16bit_wav_30s.wav");
			Context.addCanvas("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.SolidColor");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static boolean restoreSession(List<String> reportList) {
		File defaultFile = new File(DEFAULT_FILE_NAME + FILE_EXTENSION);
		PersistentSession persistentSession;
		try {
			persistentSession = loadSessionImpl(defaultFile, reportList);
			if(persistentSession.getSessionPath() != null) {
				File file = new File(persistentSession.getSessionPath());
				return userLoadSession(file, reportList);
			}
			else {
				setUpApplication(persistentSession);
				Context.setSessionToken(new SessionToken(null));
			}
			
			return persistentSession != null;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	public static boolean userLoadSession(File file, List<String> reportList) {
		try {
			PersistentSession persistentSession =  loadSessionImpl(file, reportList);
			setUpApplication(persistentSession);
			Context.setSessionToken(new SessionToken(file.getAbsolutePath()));
			saveDefaultSession();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static PersistentSession loadSessionImpl(File file, List<String> reportList) throws Exception {
		
		IOUtil.log("restore from: " + file.getAbsolutePath());
		try (FileInputStream fis = new FileInputStream(file);
				XMLDecoder in = new XMLDecoder(fis);) {
			in.setExceptionListener(new ExceptionListener() {		
				@Override
				public void exceptionThrown(Exception e) {
					reportList.add( e.getClass().getName() + " " + e.getMessage());
				}
			});
			PersistentSession persistentSession = (PersistentSession) in.readObject();
			return persistentSession;
		} catch (Exception e) {
			IOUtil.log("load session failed: " + e.getMessage());
			e.printStackTrace();
			reportList.add(e.getMessage());
			throw(e);
		}
		
	}
	private static void setUpApplication(PersistentSession persistentSession) throws AppLogicException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		Context.setOutputDimension(persistentSession.getVideoOutPutWidth(), persistentSession.getVideoOutPutHeight());
		Context.setAudio(persistentSession.getAudioInputType(), persistentSession.getAudioInputName(), persistentSession.getVideoOutPutFrames());

		Context.clearCanvasList();
		for(PersistentSoundCanvasWrapper psw : persistentSession.getSoundCanvasList()) {
			Context.addCanvasWrapper(psw.restore());
		}
	}
	
	public static File saveDefaultSession() throws IllegalArgumentException, IllegalAccessException, IOException {
		File file = new File(DEFAULT_FILE_NAME + FILE_EXTENSION);
		String path = Context.getSessionToken().isNamed() ? Context.getSessionToken().getFullPath() : null;
		if(file.exists()) {
			Files.move(file.toPath(), file.toPath().resolveSibling(DEFAULT_BACkUP_FILE_NAME + FILE_EXTENSION), StandardCopyOption.REPLACE_EXISTING);
		}
		saveSessionImpl(file, path);
		return file;
	}
	public static void userSaveSession(File file) throws IllegalArgumentException, IllegalAccessException, IOException {
		saveSessionImpl(file, null);
		Context.setSessionToken(new SessionToken(file.getAbsolutePath()));
	}
	private static PersistentSession saveSessionImpl(File file, String persitentSessionPath) throws IllegalArgumentException, IllegalAccessException, IOException {

		List<SoundCanvasWrapper> appObjects = Context.getSoundCanvasList();
		
		List<PersistentSoundCanvasWrapper> persistentWrappers = new ArrayList<PersistentSoundCanvasWrapper>();
		
		for(SoundCanvasWrapper sw : appObjects) {
			PersistentSoundCanvasWrapper psw = new PersistentSoundCanvasWrapper(sw);
			persistentWrappers.add(psw);
		}

		PersistentSession persistentSession = new PersistentSession();
		persistentSession.setSessionPath(persitentSessionPath);
		persistentSession.setSoundCanvasList(persistentWrappers);
		persistentSession.setAudioInputType(Context.getAudioInput().getType());
		persistentSession.setAudioInputName(Context.getAudioInput().getName());
		persistentSession.setVideoOutPutFrames(Context.getVideoOutputInfo().getFramesPerSecond());
		persistentSession.setVideoOutPutHeight(Context.getVideoOutputInfo().getHeight());
		persistentSession.setVideoOutPutWidth(Context.getVideoOutputInfo().getWidth());

		try (FileOutputStream out = new FileOutputStream(file); 
				XMLEncoder encoder = new XMLEncoder(out)) {
			encoder.setPersistenceDelegate(MappedValue.class, new MappedValuePersistenceDelegate());
			encoder.setPersistenceDelegate(Key.class, new KeyPersistenceDelegate());
			encoder.writeObject(persistentSession);
			encoder.flush();
			IOUtil.log("saved: " + file.getAbsolutePath() + " persitentSessionPath: " + persitentSessionPath);
			return persistentSession;
		} catch (IOException e) {
			IOUtil.log(e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
