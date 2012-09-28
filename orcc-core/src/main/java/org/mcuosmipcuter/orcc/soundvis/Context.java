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

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.soundvis.model.AudioFileInputImpl;
import org.mcuosmipcuter.orcc.soundvis.model.SoundCanvasWrapperImpl;
import org.mcuosmipcuter.orcc.soundvis.model.VideoOutputInfoImpl;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.SuperSampleData;

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
		CanvasClassNames, AppState, SongPositionPointer, VideoFrameRate, FrameMark
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
			listener.contextChanged(propertyName);
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
	
	public static AudioInput getAudioInput() {
		return audioInput;
	}

	public static synchronized VideoOutputInfo getVideoOutputInfo() {
		return videoOutputInfo;
	}
	
	// we aggregate the different output info parameters into the video output object:
	
	
	/**
	 * Sets the target video dimension
	 * @param width width in pixels
	 * @param height height in pixels
	 */
	public static synchronized void setOutputDimension(int width, int height) {
		videoOutputInfo.setWidth(width);
		videoOutputInfo.setHeight(height);
		notifyListeners(PropertyName.VideoDimension);
	}

	/**
	 * Adds a canvas to work with from the given class name string.
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
	public static synchronized void removeCanvas(SoundCanvasWrapper soundCanvas) {
		soundCanvasList.remove(soundCanvas);
		notifyListeners(PropertyName.SoundCanvasRemoved);
	}
	public static synchronized void reorderCanvasList(List<SoundCanvasWrapper> newList) {

		soundCanvasList.clear();
		soundCanvasList.addAll(newList); // TODO when wrapper is implemented do a reorder
		notifyListeners(PropertyName.SoundCanvasList);

	}
	/**
	 * Sets the audio from a file
	 * @param audioFileName full path to the file
	 */
	public static synchronized void setAudioFromFile(String audioFileName) {
		audioInput = new AudioFileInputImpl(audioFileName);
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
	 * Sets the full path to the video export file
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
	 * Adds a canvas class name to the internal set of names
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
	 * Sets the application state
	 * @param appState the state to set
	 */
	public static synchronized void setAppState(AppState appState) {
		Context.appState = appState;
		notifyListeners(PropertyName.AppState);
	}
	public static long getSongPositionPointer() {
		return songPositionPointer; // not synchronized
	}
	public static synchronized void setSongPositionPointer(long songPositionPointer) {
		Context.songPositionPointer = songPositionPointer;
		notifyListeners(PropertyName.SongPositionPointer);
	}
	
	/**
	 * This is just to notify listeners, frame positions can be obtained from {@link #getSoundCanvasList()}
	 * @param framePos
	 * @param source
	 */
	public static synchronized void setFrameMark(long framePos, SoundCanvasWrapper source) {
		notifyListeners(PropertyName.FrameMark);
	}
}
