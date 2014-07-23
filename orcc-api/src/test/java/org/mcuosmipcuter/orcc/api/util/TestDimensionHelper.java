/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2014 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.api.util;

import junit.framework.TestCase;

import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;

/**
 * @author Michael Heinzelmann
 *
 */
public class TestDimensionHelper extends TestCase {
	public  void testGetRealX() {
		
		VideoOutputInfo videoOutputInfo = new VideoOutputInfo() {
			
			@Override
			public int getWidth() {
				return 1920;
			}		
			@Override
			public String getTitle() {
				return null;
			}
			@Override
			public int getHeight() {
				return 0;
			}
			@Override
			public int getFramesPerSecond() {
				return 0;
			}
		};
		DimensionHelper d = new DimensionHelper(videoOutputInfo);
		assertEquals(videoOutputInfo.getWidth(), d.realX(100));
		assertEquals(videoOutputInfo.getWidth() / 2, d.realX(50));
		assertEquals(videoOutputInfo.getWidth() / 10, d.realX(10));
		assertEquals(0, d.realX(0));

	}
	public  void testGetRealY() {
		
		VideoOutputInfo videoOutputInfo = new VideoOutputInfo() {
			
			@Override
			public int getWidth() {
				return 1920;
			}		
			@Override
			public String getTitle() {
				return null;
			}
			@Override
			public int getHeight() {
				return 0;
			}
			@Override
			public int getFramesPerSecond() {
				return 0;
			}
		};
		DimensionHelper d = new DimensionHelper(videoOutputInfo);
		assertEquals(videoOutputInfo.getHeight(), d.realY(100));
		assertEquals(videoOutputInfo.getHeight() / 2, d.realY(50));
		assertEquals(videoOutputInfo.getHeight() / 10, d.realY(10));
		assertEquals(0, d.realY(0));

	}
}
