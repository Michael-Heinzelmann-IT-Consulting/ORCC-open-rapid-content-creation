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
package org.mcuosmipcuter.orcc.soundvis;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;

/**
 * Intermediate interface for controller components
 * @author Michael Heinzelmann
 */
public interface Mixin extends DecodingCallback {
	/**
	 * Start the processing with the given parameters
	 * @param audioInputInfo audio input
	 * @param videoOutputInfo the output to produce
	 */
	public void start(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo);
	/**
	 * Process a new frame
	 * @param frameCount current the frame number
	 */
	void newFrame(long frameCount);
}
