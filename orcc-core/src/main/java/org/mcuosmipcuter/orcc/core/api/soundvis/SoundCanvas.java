/**
* Copyright 2012 Michael Heinzelmann IT-Consulting
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package org.mcuosmipcuter.orcc.core.api.soundvis;

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
 * {@link #newFrame(long)} is called per video frame ( e.g 24, 25, 30 times per second)}
 * </li>
 * <li>
 * {@link #nextSample(int[])} is called per audio sample (typically 44100 times per second)}
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
	 * @param amplitudes amplitudes per audio channel - the values come as decoded, usually
	 * signed with o as center, maximum - minimum equal to the audio sample size (e.g. 2^16)
	 */
	public void nextSample(int[] amplitudes);
	/**
	 * Video callback: we about to display the new frame, last chance for the canvas to 
	 * change the graphics 
	 * @param frameCount
	 */
	public void newFrame(long frameCount);
	/**
	 * Prepare method called before any audio and video callback methods are called.
	 * 
	 * @param audioInputInfo info about the given audio input
	 * @param videoOutputInfo info about the configured video output
	 * @param graphics graphics to draw on, in most cases you want to store the reference in your instance
	 * @param canvasBackGround call back for external background drawing
	 * @throws PrepareException to be thrown if this canvas refuses working with the given parameters
	 */
	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo, Graphics2D graphics, CanvasBackGround canvasBackGround) throws PrepareException;
}

