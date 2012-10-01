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
package org.mcuosmipcuter.orcc.api.soundvis;

import java.awt.Graphics2D;

/**
 * Interface to be implemented by canvas programmers to draw the video.
 * Implementations need to provide an empty no args constructor, everything
 * else is entirely up to the implementation, except the constructor all is
 * optional, an empty implementation will just create an empty video channel.
 * 
 * there are 3 frequencies of method calls:
 * 
 * <ol>
 * <li>
 * {@link #prepare(AudioInputInfo, VideoOutputInfo, Graphics2D, CanvasBackGround)}
 * is only called once before the first frame
 * </li>
 * <li>
 * {@link #newFrame(long)} is called per video frame ( e.g 24, 25, 30 times per second)
 * </li>
 * <li>
 * {@link #nextSample(int[])} is called per audio sample (typically 44100 times per second)
 * </li>
 * </ol>
 * The code that is executed inside each of the methods will therefore have more or less
 * performance impact.
 * 
 * @author Michael Heinzelmann
 */
public interface SoundCanvas {
	/**
	 * Audio callback: we have received a new sample that contained the given amplitudes
	 * @param amplitudes amplitudes per audio channel - the values come as unsigned integers
	 * with 0 as minimum and the maximum equal to the audio sample size (e.g. 2^16)
	 */
	public void nextSample(int[] amplitudes);
	/**
	 * Video callback: we about to display the new frame, last chance for the canvas to 
	 * change the graphics 
	 * @param frameCount the current frame number
	 * @param graphics graphics to draw on, avoid storing the reference in your instance
	 */
	public void newFrame(long frameCount, Graphics2D graphics);
	/**
	 * Prepare method called before any audio and video callback methods are called.
	 * 
	 * @param audioInputInfo info about the given audio input
	 * @param videoOutputInfo info about the configured video output
	 */
	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo);
	
	/**
	 * This method is called when the engine is done with the frame, all cleanup and re-set work should be done here
	 */
	public void postFrame();
	
	/**
	 * Get the number of frames this canvas needs to run before a frame image can be produced.
	 * This will generate some delay on play back if the number is too high.
	 * @return the number, any number starting from 0 - the lower the better
	 */
	public int getPreRunFrames();
	
	/**
	 * Get a simplified image icon of this canvas current state for showing this in a compacted UI.
	 * @param width width of this icon
	 * @param height height of this icon
	 * @param graphics graphics to draw on
	 */
	public void drawCurrentIcon(int width, int height, Graphics2D graphics);
}

