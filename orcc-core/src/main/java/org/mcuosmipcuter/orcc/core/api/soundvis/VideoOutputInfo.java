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


/**
 * Provides information about the video output
 * @author Michael Heinzelmann
 */
public interface VideoOutputInfo {
	
	/**
	 * The frame rate of the video
	 * @return the number of frames per second
	 */
	public int getFramesPerSecond() ;
	/**
	 * The video dimension width
	 * @return the video width
	 */
	public int getWidth() ;
	/**
	 * The video dimension height
	 * @return the video height
	 */
	public int getHeight();
	/**
	 * Videos title as provided by the user
	 * @return the title
	 */
	public String getTitle();
	/**
	 * Videos water mark text
	 * @return the water mark string
	 */
	public String getWaterMarkText();

}
