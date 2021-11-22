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
import java.util.Map;
import java.util.Properties;

import org.mcuosmipcuter.orcc.api.soundvis.MappedValue;
import org.mcuosmipcuter.orcc.soundvis.AppLogicException;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.ImageStore.Key;
import org.mcuosmipcuter.orcc.soundvis.SessionToken;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.ValueChanges;
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
		Context.setSessionToken(new SessionToken());
		try {
			Context.setAudioFromClasspath("/audio/metronome_pcm_16bit_wav_30s.wav");
			Context.addCanvas("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.SolidColor");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static boolean restoreSession( List<String> reportList) {
		File defaultFile = new File(FileConfiguration.getTargetConfDir() + FileConfiguration.getSep() + DEFAULT_FILE_NAME + FILE_EXTENSION);
		if(!defaultFile.exists()){
			// attempt loading from backup
			defaultFile = new File(FileConfiguration.getTargetConfDir() + FileConfiguration.getSep() + DEFAULT_BACkUP_FILE_NAME + FILE_EXTENSION);
			if(!defaultFile.exists()){
				return false; // no files to restore exist
			}
		}
		PersistentSession persistentSession;
		try {
			persistentSession = loadSessionImpl(defaultFile, reportList);
			SessionToken st;
			if(persistentSession.getSessionPath() != null) {
				st = new SessionToken(persistentSession.getSessionPath(), reportList);
				if(persistentSession.getChanges() != null) {
					for(Map.Entry<String, ValueChanges> e : persistentSession.getChanges().entrySet()) {
						if(e.getValue().isLogicallyChanged()) {
							st.changeOccurred(e.getKey(), e.getValue().getOriginal(), e.getValue().getCurrent());
						}
					}
				}
			}
			else {
				st = new SessionToken();
			}

			setUpApplication(persistentSession, reportList);

			Context.setSessionToken(st);

			return persistentSession != null;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	public static boolean userLoadSession(File file, List<String> reportList) {
		try {
			PersistentSession persistentSession =  loadSessionImpl(file, reportList);
			setUpApplication(persistentSession, reportList);
			Context.setSessionToken(new SessionToken(file.getAbsolutePath(), reportList));
			saveDefaultSession(false);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			reportList.add(e.getMessage());
			if(e.getCause() != null) {
				reportList.add(e.getCause().getMessage());
			}
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
			Properties vp = Context.getVersionProperties();
			IOUtil.log("version loaded/running: " +  persistentSession.getVersion()  + "/" + vp.getProperty("version"));
			IOUtil.log("buildNr loaded/running: " + persistentSession.getBuildNumber() + "/" +vp.getProperty("buildNumber"));
			
			return persistentSession;
		} catch (Exception e) {
			IOUtil.log("load session failed: " + e.getMessage());
			e.printStackTrace();
			reportList.add(e.getMessage());
			throw(e);
		}
		
	}
	private static void setUpApplication(PersistentSession persistentSession, List<String> reportList) throws AppLogicException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		Context.loadFrameRateAndOutputDimension(persistentSession.getVideoOutPutFrames(), persistentSession.getVideoOutPutWidth(), persistentSession.getVideoOutPutHeight());
		try {
			Context.setAudio(persistentSession.getAudioInputType(), persistentSession.getAudioInputName(), persistentSession.getVideoOutPutFrames());
		} catch (Exception e) {
			IOUtil.log(e.getMessage() + " " + e.getCause());
			reportList.add(e.getMessage() + " " + e.getCause());
			Context.setAudioFromClasspath("/audio/metronome_pcm_16bit_wav_30s.wav");
		}

		Context.clearCanvasList();
		for(PersistentSoundCanvasWrapper psw : persistentSession.getSoundCanvasList()) {
			try {
				Context.addCanvasWrapper(psw.restore(reportList));
			}
			catch(Exception ex) {
				reportList.add(psw.getSoundCanvas().getDelegate() + ": " +  ex.getMessage());
			}
		}
	}
	
	public static File saveDefaultSession(boolean persistChanges) throws IllegalArgumentException, IllegalAccessException, IOException {
		File file = new File(FileConfiguration.getTargetConfDir() + FileConfiguration.getSep() + DEFAULT_FILE_NAME + FILE_EXTENSION);
		SessionToken st = Context.getSessionToken();
		String path = st.isNamed() ? st.getFullPath() : null;
		if(file.exists()) {
			Files.move(file.toPath(), file.toPath().resolveSibling(FileConfiguration.getTargetConfDir() + FileConfiguration.getSep() + DEFAULT_BACkUP_FILE_NAME + FILE_EXTENSION), StandardCopyOption.REPLACE_EXISTING);
		}
		saveSessionImpl(file, path, persistChanges ? st.getChanges() : null);
		return file;
	}
	public static void userSaveSession(File file) throws IllegalArgumentException, IllegalAccessException, IOException {
		saveSessionImpl(file, null, null);
		Context.setSessionToken(new SessionToken(file.getAbsolutePath(), new ArrayList<>()));
	}
	private static PersistentSession saveSessionImpl(File file, String persitentSessionPath, Map<String, ValueChanges> changes) throws IllegalArgumentException, IllegalAccessException, IOException {

		List<SoundCanvasWrapper> appObjects = Context.getSoundCanvasList();
		
		List<PersistentSoundCanvasWrapper> persistentWrappers = new ArrayList<PersistentSoundCanvasWrapper>();
		
		for(SoundCanvasWrapper sw : appObjects) {
			PersistentSoundCanvasWrapper psw = new PersistentSoundCanvasWrapper(sw);
			persistentWrappers.add(psw);
		}

		PersistentSession persistentSession = new PersistentSession();
		Properties vp = Context.getVersionProperties();
		persistentSession.setVersion(vp.getProperty("version"));
		persistentSession.setBuildNumber(vp.getProperty("buildNumber"));
		persistentSession.setChanges(changes);
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
