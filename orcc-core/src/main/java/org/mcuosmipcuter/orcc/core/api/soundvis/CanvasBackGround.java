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
 * Facade for a canvas background that can draw the background in different ways,
 * like image, solid color etc. If the canvas wants the background be drawn externally
 * it should call this facade {@link #drawBackGround()} method.
 * Canvas that take full responsibility for the background can just ignore
 * this interface.
 * @author Michael Heinzelmann
 */
public interface CanvasBackGround {
	/**
	 * Draws the background in a specific way that is determined by the user
	 */
	public void drawBackGround();
}