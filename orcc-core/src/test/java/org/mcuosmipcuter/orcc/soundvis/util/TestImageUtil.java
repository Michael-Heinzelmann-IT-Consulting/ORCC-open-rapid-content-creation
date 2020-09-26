/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2020 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.soundvis.util;

import junit.framework.TestCase;

/**
 * @author Michael Heinzelmann
 *
 */
public class TestImageUtil extends TestCase{

	public void test_qudrant() {
		assertEquals(0, ImageUtil.adjustQuadrant(0));
		assertEquals(1, ImageUtil.adjustQuadrant(1));
		assertEquals(2, ImageUtil.adjustQuadrant(2));
		assertEquals(3, ImageUtil.adjustQuadrant(3));
		assertEquals(0, ImageUtil.adjustQuadrant(4));
		assertEquals(1, ImageUtil.adjustQuadrant(5));
		assertEquals(2, ImageUtil.adjustQuadrant(6));
		assertEquals(3, ImageUtil.adjustQuadrant(7));
		assertEquals(0, ImageUtil.adjustQuadrant(8));
		
		assertEquals(3, ImageUtil.adjustQuadrant(-1));
		assertEquals(2, ImageUtil.adjustQuadrant(-2));
		assertEquals(1, ImageUtil.adjustQuadrant(-3));
		assertEquals(0, ImageUtil.adjustQuadrant(-4));
		assertEquals(3, ImageUtil.adjustQuadrant(-5));
		assertEquals(2, ImageUtil.adjustQuadrant(-6));
		assertEquals(1, ImageUtil.adjustQuadrant(-7));
		assertEquals(0, ImageUtil.adjustQuadrant(-8));
		
	}

}
