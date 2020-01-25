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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sound.sampled.FloatControl;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.soundvis.model.AudioClasspathInputImpl;
import org.mcuosmipcuter.orcc.soundvis.model.AudioFileInputImpl;
import org.mcuosmipcuter.orcc.soundvis.model.SoundCanvasWrapperImpl;
import org.mcuosmipcuter.orcc.soundvis.model.VideoOutputInfoImpl;

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
		AudioInputInfo, VideoDimension, SoundCanvasAdded, SoundCanvasRemoved, SoundCanvasList, ExportFileName, 
		CanvasClassNames, AppState, SongPositionPointer, VideoFrameRate, VolumeControl, FullPreRun, SoundCanvasProperty, BeforeSoundCanvasProperty, SoundCanvasPropertyCancelled
	}
	/**
	 * Enumeration of application states
	 * @author Michael Heinzelmann
	 */
	public enum AppState {
		READY, PLAYING, PAUSED, EXPORTING
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
		for(Listener listener : listeners) {
			//System.err.println("listener: " + listener + " " + propertyName);
			listener.contextChanged(propertyName);
		}
	}
	private static void notifyListeners(String msg) {
		for(Listener listener : listeners) {
			//System.err.println("listener: " + listener + " " + propertyName);
			listener.progress(msg);
		}
	}
	
	// static fields
	private static AppState appState = AppState.READY;
	private static List<SoundCanvasWrapper> soundCanvasList = new ArrayList<SoundCanvasWrapper>();
	private static SortedSet<String> canvasClassNames = new TreeSet<String>();
	private static String exportFileName;
	private static AudioInput audioInput;
	private static VideoOutputInfoImpl videoOutputInfo = new VideoOutputInfoImpl(25, 1920, 1080);
	private static long songPositionPointer;
	private static FloatControl volumeControl;
	private static boolean fullPreRun;
	
	/**
	 * Gets the audio input info object
	 * @return the input or null
	 */
	public static AudioInput getAudioInput() {
		return audioInput;
	}

	/**
	 *  Gets the video output info object
	 * @return the video output info, never null, there is a default of 25fps 1920x1080
	 */
	public static synchronized VideoOutputInfo getVideoOutputInfo() {
		return videoOutputInfo;
	}
	
	// we aggregate the different output info parameters into the video output object:
	
	
	/**
	 * Sets the target video dimension and notifies listeners
	 * @param width width in pixels
	 * @param height height in pixels
	 */
	public static synchronized void setOutputDimension(int width, int height) {
		videoOutputInfo.setWidth(width);
		videoOutputInfo.setHeight(height);
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
		videoOutputInfo.setFramesPerSecond(frameRate);
		notifyListeners(PropertyName.VideoFrameRate);
	}

	/**
	 * Adds a canvas to work with from the given class name string and notifies listeners.
	 * @param canvasClassName fully qualified name of the {@link SoundCanvas} instance to use
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static synchronized  void addCanvas(String canvasClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		SoundCanvas soundCanvas = (SoundCanvas) Class.forName(canvasClassName).newInstance();
		SoundCanvasWrapper soundCanvasWrapper = new SoundCanvasWrapperImpl(soundCanvas);
		if(audioInput != null) {
			soundCanvasWrapper.prepare(audioInput.getAudioInputInfo(), videoOutputInfo);
		}
		soundCanvasList.add(soundCanvasWrapper);
		notifyListeners(PropertyName.SoundCanvasAdded);
	}
	/**
	 * Removes the given canvas from the list and notifies listeners
	 * @param soundCanvas the canvas to remove
	 */
	public static synchronized void removeCanvas(SoundCanvasWrapper soundCanvas) {
		soundCanvasList.remove(soundCanvas);
		notifyListeners(PropertyName.SoundCanvasRemoved);
	}
	
	/**
	 * Replaces the current list with the new one and notifies listeners
	 * @param newList the new list
	 */
	public static synchronized void replaceCanvasList(List<SoundCanvasWrapper> newList) {
		soundCanvasList.clear();
		soundCanvasList.addAll(newList);
		notifyListeners(PropertyName.SoundCanvasList);
	}
	/**
	 * Sets the audio from a file and notifies listeners
	 * @param audioFileName full path to the file
	 */
	public static synchronized void setAudioFromFile(String audioFileName) throws AppLogicException {
		AudioInput a = new AudioFileInputImpl(audioFileName);
		setAudio(a);
	}
	/**
	 * Sets the audio from a classpath resource and notifies listeners
	 * @param audioFileName full path to the file
	 */
	public static synchronized void setAudioFromClasspath(String audioResourcePath) throws AppLogicException {
		AudioInput a = new AudioClasspathInputImpl(audioResourcePath);
		setAudio(a);
	}
	private static void setAudio(AudioInput a) throws AppLogicException {
		final float sampleRate = a.getAudioInputInfo().getAudioFormat().getSampleRate();
		final int frameRate = videoOutputInfo.getFramesPerSecond();
		if(sampleRate % frameRate != 0) {
			throw new AppLogicException("sample rate " + sampleRate + " % frame rate " + frameRate + " is not 0");
		}
		audioInput = a;
		notifyListeners(PropertyName.AudioInputInfo);
	}
	/**
	 * Full path to the video export file
	 * @return the file full path or null if no file is set
	 */
	public static synchronized String getExportFileName() {
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
	public static synchronized List<SoundCanvasWrapper> getSoundCanvasList() {
		return soundCanvasList;	
	}
	/**
	 * Returns the application state
	 * @return the state
	 */
	public static synchronized AppState getAppState() {
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
	/**
	 * Whether a full pre run should precede the playing from song position pointer
	 * @return the boolean value
	 */
	public static boolean isFullPreRun() {
		return fullPreRun;
	}
	public static synchronized void setFullPreRun(boolean fullPreRun) {
		Context.fullPreRun = fullPreRun;
		notifyListeners(PropertyName.FullPreRun);
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
	public static void cancelPropertyUpdate(String name) {
		notifyListeners(PropertyName.SoundCanvasPropertyCancelled);
	}
}
