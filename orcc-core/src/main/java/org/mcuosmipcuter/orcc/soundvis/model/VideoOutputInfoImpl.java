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
package org.mcuosmipcuter.orcc.soundvis.model;

import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;

/**
 * Bean implementation of {@link VideoOutputInfo}
 * @author Michael Heinzelmann
 */
public class VideoOutputInfoImpl implements VideoOutputInfo {
	private int framesPerSecond;
	private int width;
	private int height;
	private String title;
	private String waterMarkText;
	
	public VideoOutputInfoImpl(int framesPerSecond, int width, int height) {
		this.framesPerSecond = framesPerSecond;
		this.width = width;
		this.height = height;
	}

	@Override
	public int getFramesPerSecond() {
		return framesPerSecond;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getWaterMarkText() {
		return waterMarkText;
	}

	public void setFramesPerSecond(int framesPerSecond) {
		this.framesPerSecond = framesPerSecond;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setWaterMarkText(String waterMarkText) {
		this.waterMarkText = waterMarkText;
	}

}
