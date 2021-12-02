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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.AudioLayout;
import org.mcuosmipcuter.orcc.api.soundvis.AudioOutputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.ExtendedFrameHistory;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.soundvis.AudioInput.Type;
import org.mcuosmipcuter.orcc.soundvis.model.AudioOutputInfoImpl;
import org.mcuosmipcuter.orcc.soundvis.model.AudioURLInputImpl;
import org.mcuosmipcuter.orcc.soundvis.model.SoundCanvasWrapperImpl;
import org.mcuosmipcuter.orcc.soundvis.model.VideoOutputInfoImpl;
import org.mcuosmipcuter.orcc.soundvis.util.AudioUtil;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Static context object for the soundvis application = playing audio and generating saving video
 * @author Michael Heinzelmann
 */
public abstract class Context {
	/**
	 * Enumeration about property names that will send a notification enables the caller to filter for properties of interest.
	 * @author Michael Heinzelmann
	 */
	public enum PropertyName {
		AudioInputInfo, VideoDimension, SoundCanvasAdded, SoundCanvasRemoved, SoundCanvasList, SoundCanvasListCleared,
		ExportFileName, CanvasClassNames, AppState, SongPositionPointer, VideoFrameRate, VolumeControl, FullPreRun,
		SoundCanvasProperty, BeforeSoundCanvasProperty, SoundCanvasPropertyCancelled, SessionChanged, NewSession, AudioOutputInfo;
	}
	/**
	 * Enumeration of application states
	 * @author Michael Heinzelmann
	 */
	public enum AppState {
		INIT, READY, PLAYING, PAUSED, EXPORTING, LOADING
	}
	/**
	 * Listener to this context for asynchronous communication
	 * @author Michael Heinzelmann
	 */
	public interface Listener {
		/**
		 * Notification of a change on this context, the given name is for filtering purposes.
		 * @param propertyName the enumerated name of the changed property
		 */
		public void contextChanged(PropertyName propertyName);
		public default void progress(String msg) {
		}
		public default void updateUI(SoundCanvas soundCanvas) {
		}
	}
	
	private static long touchCounter;
	
	/**
	 * user is doing something
	 * @return
	 */
	public static long touch() {
		return ++touchCounter;
	}
	
	public static long getTouchCounter() {
		return touchCounter;
	}
	
	private static SessionToken sessionToken = new SessionToken();

	public static SessionToken getSessionToken() {
		return sessionToken;
	}

	public static void setSessionToken(SessionToken sessionToken) {
		if(sessionToken == null) {
			throw new IllegalArgumentException("sessionToken null not allowed!");
		}
		Context.sessionToken = sessionToken;
		IOUtil.log("setSessionToken " + sessionToken);
		notifyListeners(PropertyName.NewSession);
	}
	
	public static void changeSession(String propertyKey, Object oldValue, Object newValue) {
		sessionToken.changeOccurred(propertyKey, oldValue, newValue);
		notifyListeners(PropertyName.SessionChanged);
	}
	public static void changeSoundCanvasList(SoundCanvasWrapper[] listBefore, SoundCanvasWrapper[] listAfter) {
		String[] keysBefore = new String[listBefore.length];
		int i = 0;
		for(SoundCanvasWrapper scw : listBefore) {
			keysBefore[i++] = scw.getSessionId();
		}
		String[] keysAfter = new String[listAfter.length];
		i = 0;
		for(SoundCanvasWrapper scw : listAfter) {
			keysAfter[i++] = scw.getSessionId();
		}
		changeSession("SoundCanvasList", keysBefore, keysAfter);
	}

	// listener list
	private static List<Listener> listeners = new ArrayList<Listener>();
	
	/**
	 * Adds a listener to this context
	 * @param listener the listener to add
	 */
	public static void addListener(Listener listener) {
		listeners.add(listener);
	}
	/**
	 * Removes the given a listener from this context
	 * @param listener the listener to remove
	 */
	public static void removeListener(Listener listener) {
		listeners.remove(listener);
	}
	
	// private helper method
	private static void notifyListeners(PropertyName propertyName) {
		//System.err.println("#73 notifyListeners " + System.currentTimeMillis());
		for(Listener listener : listeners) {
			//System.err.println("#73 " + System.currentTimeMillis() + " listener: " + listener + " " + propertyName);
			listener.contextChanged(propertyName);
			//System.err.println("#73 " + System.currentTimeMillis());
		}
		//System.err.println("#73 notifyListeners " + System.currentTimeMillis());
		touch();
	}
	private static void notifyListeners(String msg) {
		for(Listener listener : listeners) {
			//System.err.println("listener: " + listener + " " + propertyName);
			listener.progress(msg);
		}
		touch();
	}
	private static void notifyUIListeners(SoundCanvas soundCanvas) {
		for(Listener listener : listeners) {
			listener.updateUI(soundCanvas);
		}
	}
	
	// static fields
	private static AppState appState = AppState.INIT;
	private static List<SoundCanvasWrapper> soundCanvasList = new ArrayList<SoundCanvasWrapper>();
	private static SortedSet<String> canvasClassNames = new TreeSet<String>();
	private static String exportFileName;
	private static AudioInput audioInput;
	private static VideoOutputInfoImpl videoOutputInfo = new VideoOutputInfoImpl(25, 1920, 1080);
	private static AudioOutputInfo audioOutputInfo = new AudioOutputInfoImpl(AudioLayout.COMPRESSED);
	private static long songPositionPointer;
	private static FloatControl volumeControl;
	
	/**
	 * Gets the audio input info object
	 * @return the input or null
	 */
	public static AudioInput getAudioInput() {
		return audioInput;
	}

	public static AudioOutputInfo getAudioOutputInfo() {
		return audioOutputInfo;
	}
	
	public static void setAudioOutputLayout(AudioLayout audioLayout) {
		audioOutputInfo = new AudioOutputInfoImpl(audioLayout);
		notifyListeners(PropertyName.AudioOutputInfo);
	}

	/**
	 *  Gets the video output info object
	 * @return the video output info, never null, there is a default of 25fps 1920x1080
	 */
	public static VideoOutputInfo getVideoOutputInfo() {
		return videoOutputInfo;
	}
	
	// we aggregate the different output info parameters into the video output object:
	
	
	/**
	 * Sets the target video dimension and notifies listeners
	 * @param width width in pixels
	 * @param height height in pixels
	 */
	public static synchronized void setOutputDimension(int width, int height) {
		VideoOutputInfoImpl newVideoOutputInfo = new VideoOutputInfoImpl(videoOutputInfo.getFramesPerSecond(), width, height);
		changeSession("VideoOutputInfo", videoOutputInfo, newVideoOutputInfo);
		videoOutputInfo = newVideoOutputInfo;
		notifyListeners(PropertyName.VideoDimension);
	}
	public static synchronized void loadFrameRateAndOutputDimension(int framesPerSecond, int width, int height) {
		VideoOutputInfoImpl newVideoOutputInfo = new VideoOutputInfoImpl(framesPerSecond, width, height);
		changeSession("VideoOutputInfo", videoOutputInfo, newVideoOutputInfo);
		videoOutputInfo = newVideoOutputInfo;
		notifyListeners(PropertyName.VideoDimension);
	}
	/**
	 * Sets the target video frame rate and notifies listeners
	 * @param frameRate the frame rate to use
	 */
	public static synchronized void setOutputFrameRate(int frameRate) throws AppLogicException {
		if(audioInput != null) {
			final float sampleRate = audioInput.getAudioInputInfo().getAudioFormat().getSampleRate();
			if(sampleRate % frameRate != 0) {
				throw new AppLogicException("sample rate " + sampleRate + " % frame rate " + frameRate + " is not 0");
			}
		}
		VideoOutputInfoImpl newVideoOutputInfo = new VideoOutputInfoImpl(frameRate, videoOutputInfo.getWidth(), videoOutputInfo.getHeight());
		changeSession("VideoOutputInfo", videoOutputInfo, newVideoOutputInfo);
		videoOutputInfo = newVideoOutputInfo;
		notifyListeners(PropertyName.VideoFrameRate);
	}

	/**
	 * Adds a canvas to work with from the given class name string and notifies listeners.
	 * @param canvasClassName fully qualified name of the {@link SoundCanvas} instance to use
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public static synchronized  void addCanvas(String canvasClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		SoundCanvas soundCanvas = (SoundCanvas) Class.forName(canvasClassName).getDeclaredConstructor((Class<?>[])null).newInstance((Object[])null);
		SoundCanvasWrapper soundCanvasWrapper = new SoundCanvasWrapperImpl(soundCanvas, null);
		if(audioInput != null) {
			soundCanvasWrapper.prepare(audioInput.getAudioInputInfo(), videoOutputInfo);
		}
		soundCanvasWrapper.setFrameFrom(0);
		soundCanvasWrapper.setFrameTo(0); // TODO frames without audio
		SoundCanvasWrapper[] listBefore = soundCanvasList.toArray(new SoundCanvasWrapper[] {});
		soundCanvasList.add(soundCanvasWrapper);
		notifyListeners(PropertyName.SoundCanvasAdded);
		changeSession(soundCanvasWrapper.getSessionId(), null, soundCanvas);
		changeSoundCanvasList(listBefore, soundCanvasList.toArray(new SoundCanvasWrapper[] {}));
	}
	public static synchronized  void addCanvasWrapper(SoundCanvasWrapper soundCanvasWrapper) {
		soundCanvasList.add(soundCanvasWrapper);
		notifyListeners(PropertyName.SoundCanvasAdded);
		notifyListeners("added " + soundCanvasWrapper.getDisplayName());
	}

	/**
	 * Removes the all canvas from the list and notifies listeners
	 * @param soundCanvas the canvas to remove
	 */
	public static synchronized void clearCanvasList() {
		Iterator<SoundCanvasWrapper> iter = soundCanvasList.iterator();
		while(iter.hasNext()) {
			iter.next();
			iter.remove();	
		}
		notifyListeners(PropertyName.SoundCanvasListCleared);
	}
		
	
	/**
	 * Replaces the current list with the new one and notifies listeners
	 * @param newList the new list
	 */
	public static synchronized void replaceCanvasList(List<SoundCanvasWrapper> newList) {
		if (newList.size() > soundCanvasList.size()) {
			for (SoundCanvasWrapper scw : newList) {
				if (!soundCanvasList.contains(scw)) {
					changeSession(scw.getSessionId(), null, scw.getSoundCanvas());
				}
			}
		}
		if (soundCanvasList.size() > newList.size()) {
			for (SoundCanvasWrapper scw : soundCanvasList) {
				if (!newList.contains(scw)) {
					changeSession(scw.getSessionId(), scw.getSoundCanvas(), null);
				}
			}
		}
		changeSoundCanvasList(soundCanvasList.toArray(new SoundCanvasWrapper[] {}), newList.toArray(new SoundCanvasWrapper[] {}));
		soundCanvasList.clear();
		soundCanvasList.addAll(newList);
		notifyListeners(PropertyName.SoundCanvasList);
	}
	/**
	 * Sets the audio from a file and notifies listeners
	 * @param audioFileName full path to the file
	 */
	public static synchronized void setAudioFromFile(String audioFileName) throws AppLogicException {
			String urlString = audioFileName.startsWith("file:") ? audioFileName : "file:" + audioFileName;
			URL url;
			try {
				url = new URL(urlString);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			loadAudio(url, null);

	}
	private static void loadAudio(URL url, String classpath) throws AppLogicException {
		final AppState before  = appState;
		IOUtil.log(before + " loading " + url);
		progressUpdate("loading " + url);

			if(before != AppState.LOADING) {
				setAppState(AppState.LOADING);
			}

			try {
				long start = System.currentTimeMillis();
				AudioInput a = new AudioURLInputImpl(url, classpath);
				setAudio(a);
				IOUtil.log("loaded in " + (System.currentTimeMillis() - start) + "ms");
			}
			catch(AppLogicException ale) {
				throw ale;
			} catch (Exception ex) {
				IOUtil.log("exception loading audio: " + ex);
				throw new RuntimeException(ex);
			}
			finally {
				if(before != AppState.LOADING) {
					setAppState(before);
				}
			}
	}
	/**
	 * Sets the audio from a classpath resource and notifies listeners
	 * @param audioFileName full path to the file
	 */
	public static synchronized void setAudioFromClasspath(String audioResourcePath) throws AppLogicException {
		URL url = AudioURLInputImpl.getClasspathUrl(audioResourcePath);
		String classpath = null;
		if(url != null) {
			classpath = audioResourcePath;
		}
		else {
			try {
				url = new URL(audioResourcePath);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		loadAudio(url, classpath);
	}
	public static synchronized void setAudio(Type inputType, String audioInputName, int outPutFrameRate) throws AppLogicException {
		switch(inputType) {
		case FILE:
			setAudioFromFile(audioInputName);
				break;
			case STREAM:
				setAudioFromClasspath(audioInputName);
				break;
			default:
				throw new IllegalArgumentException();
		}
		setOutputFrameRate(outPutFrameRate);
	}
	
	private static void setAudio(AudioInput a) throws AppLogicException {
		AudioFormat format = a.getAudioInputInfo().getAudioFormat();
		final float sampleRate = format.getSampleRate();
		final int frameRate = videoOutputInfo.getFramesPerSecond();
		if(sampleRate % frameRate != 0) {
			throw new AppLogicException("sample rate " + sampleRate + " % frame rate " + frameRate + " is not 0");
		}
		changeSession(AudioInput.class.getName(), audioInput != null ? audioInput.getName() : null, a != null ? a.getName() : null);
		audioInput = a;
		try(SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(format)){
		int chunkSize =  a.getAudioInputInfo().getAudioFormat().getFrameSize();
			sourceDataLine.open(format, chunkSize);
			setVolumeControl(AudioUtil.getVolumeControl(sourceDataLine));
		}
		catch(Exception ex) {
			IOUtil.log(ex.getMessage());
		}
		notifyListeners(PropertyName.AudioInputInfo);
	}
	/**
	 * Full path to the video export file
	 * @return the file full path or null if no file is set
	 */
	public static String getExportFileName() {
		return exportFileName;
	}
	/**
	 * Sets the full path to the video export file and notifies listeners
	 * @param exportFileName the name to use
	 */
	public static synchronized void setExportFileName(String exportFileName) {
		Context.exportFileName = exportFileName;
		notifyListeners(PropertyName.ExportFileName);
	}
	/**
	 * Get all available canvas class names
	 * @return the names as sorted set
	 */
	public static SortedSet<String> getCanvasClassNames() {
		return canvasClassNames;
	}
	/**
	 * Adds a canvas class name to the internal set of names and notifies listeners
	 * @param canvasClassName
	 */
	public static synchronized void addCanvasClassName(String canvasClassName) {
		canvasClassNames.add(canvasClassName);
		notifyListeners(PropertyName.CanvasClassNames);
	}
	/**
	 * Gets the current {@link SoundCanvas} instance
	 * @return the instance or null if none is set
	 */
	public static List<SoundCanvasWrapper> getSoundCanvasList() {
		return soundCanvasList;	
	}
	/**
	 * Returns the application state
	 * @return the state
	 */
	public static AppState getAppState() {
		return appState;
	}
	/**
	 * Sets the application state and notifies listeners
	 * @param appState the state to set
	 */
	public static synchronized void setAppState(AppState appState) {
		Context.appState = appState;
		notifyListeners(PropertyName.AppState);
	}
	/**
	 * Get the current song position pointer
	 * @return the song position pointer
	 */
	public static long getSongPositionPointer() {
		return songPositionPointer; // not synchronized
	}
	/**
	 * Sets the current song position pointer and notifies listeners
	 * @param songPositionPointer the position to set
	 */
	public static synchronized void setSongPositionPointer(long songPositionPointer) {
		Context.songPositionPointer = songPositionPointer;
		notifyListeners(PropertyName.SongPositionPointer);
	}
	/**
	 * Sets the currently used volume control and notifies listeners
	 * @param volumeControl
	 */
	public static synchronized void setVolumeControl(FloatControl volumeControl) {
		Context.volumeControl = volumeControl;
		notifyListeners(PropertyName.VolumeControl);
	}
	/**
	 * Gets the currently used volume control 
	 * @return the volume control
	 */
	public static FloatControl getVolumeControl() {
		return volumeControl;
	}

	public static void canvasPropertyWritten(String name,
			SoundCanvas soundCanvas) {
		notifyListeners(PropertyName.SoundCanvasProperty);
		
	}
	public static void beforePropertyUpdate(String name) {
		notifyListeners(PropertyName.BeforeSoundCanvasProperty);
	}
	public static void progressUpdate(String msg) {
		notifyListeners(msg);
	}
	public static void updateUI(SoundCanvas soundCanvas) {
		notifyUIListeners(soundCanvas);
	}
	public static void cancelPropertyUpdate(String name) {
		notifyListeners(PropertyName.SoundCanvasPropertyCancelled);
	}
	public static long getMaxFrame() {
		long frameToConcrete = 0;
		if (audioInput != null) {
			AudioInputInfo audioInputInfo = audioInput.getAudioInputInfo();
			double audioLength = (double) audioInputInfo.getFrameLength();
			double sampleRate = audioInputInfo.getAudioFormat().getSampleRate();
			double numberOfSeconds = audioLength / sampleRate;

			double frameRate = videoOutputInfo.getFramesPerSecond();
			frameToConcrete = (long) Math.floor(numberOfSeconds * frameRate);
		}
		return frameToConcrete;
	}
	
	public static long getPreRun(AudioInputStream ais, AudioFormat format, boolean forAllCanvas) throws IOException {
		
		int chunkSize =  format.getFrameSize();
		int samplesPerFrame = (int)format.getSampleRate() / Context.getVideoOutputInfo().getFramesPerSecond();
		final long songPos = Context.getSongPositionPointer();
		long preRun = 1;
		for(SoundCanvasWrapper s : soundCanvasList) {
			if((forAllCanvas || s.getFrameFrom() <= songPos && s.getFrameTo() > songPos) && s.getSoundCanvas() instanceof ExtendedFrameHistory) {
				int f = ((ExtendedFrameHistory)s.getSoundCanvas()).getCurrentHistoryFrameSize();
				IOUtil.log(s + " getPreRunFrames() = " + f);
				if(f > preRun) {
					preRun = f;
				}
			}
		}
		if(forAllCanvas ) {
			IOUtil.log("forAllCanvas: " +  forAllCanvas + " preRun: " + preRun);
		}
		final long frameStart = songPos - preRun >= 0 ? Context.getSongPositionPointer() - preRun : 0;
		
		if( frameStart >= 0) {	
			long byteStart = frameStart * samplesPerFrame * chunkSize;
			long count = 0;
			while(count < byteStart  && ais.available() > 0) {
				int step = count < byteStart - samplesPerFrame * chunkSize ? samplesPerFrame * chunkSize : chunkSize;
				long skipped = ais.skip( step);
				count += skipped;
			}
			if(count != byteStart) {
				IOUtil.log("WARNING did not reach correct start pos in stream: count " + count + "  vs. " + byteStart + " ");
			}

		}
		return frameStart;
	}
		
	public static Properties getVersionProperties() {
		Properties vp = new Properties();
		try(InputStream is = Context.class.getResourceAsStream("/version.properties")){
			vp.load(is);
		} catch (IOException e) {
			IOUtil.log(e.getMessage());
		}
		return vp;
	}
	public static void memoryReport(StringBuilder stringBuilder) {
		Runtime rt = Runtime.getRuntime();
		long maxMb = rt.maxMemory() / 1024 / 1024;
		long totalMb = rt.totalMemory() / 1024 / 1024;
		long freeMb = rt.freeMemory()  / 1024 / 1024;
		
		stringBuilder.append("max memory Mb: " + maxMb);
		stringBuilder.append("\n");
		stringBuilder.append("total memory Mb: " + totalMb);
		stringBuilder.append("\n");
		stringBuilder.append(" free memory Mb: " + freeMb);
	}
}
