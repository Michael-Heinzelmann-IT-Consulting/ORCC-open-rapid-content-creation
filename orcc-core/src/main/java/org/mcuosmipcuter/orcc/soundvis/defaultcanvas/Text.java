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
package org.mcuosmipcuter.orcc.soundvis.defaultcanvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Calendar;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyListener;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.TextHelper;

/**
 * @author Michael Heinzelmann
 *
 */
public class Text implements SoundCanvas, PropertyListener {
	
	private int year = Calendar.getInstance().get(Calendar.YEAR);
	private String user = System.getProperty("user.name");
	
	@UserProperty(description="the text to display")
	private String text = "ORCC rapid content creation for entertainment, education and media production\n" + year + " " + user + " graphics by soundvis";
	@UserProperty(description="font size for text")
	@LimitedIntProperty(description="font size limitation", minimum=4)
	private int fontSize = 36;
	@UserProperty(description="text color")
	private Color textColor = Color.BLACK;
	@UserProperty(description="top margin of test")
	int topMargin = 200;
	
	@UserProperty(description="scrolling in pixel per frame")
	private int scrollIncrement;
	
	VideoOutputInfo videoOutputInfo;
	
	private int topPos;
	private int maxTextWidth;
	
	
	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#nextSample(int[])
	 */
	@Override
	public void nextSample(int[] amplitudes) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#newFrame(long)
	 */
	@Override
	public void newFrame(long frameCount, Graphics2D graphics2d) {

		if(text == null || text.length() == 0) {
			return;
		}
		Font f = graphics2d.getFont().deriveFont((float)fontSize);
		graphics2d.setFont(f);
		graphics2d.setColor(textColor);
		
		
		String[] lines = text.split("\n");
		
		int top = topPos;
		final int strHeight = graphics2d.getFontMetrics().getHeight();
		int leftMargin = (videoOutputInfo.getWidth() - maxTextWidth) / 2 ;
		for(String line : lines) {
			graphics2d.drawString(line, leftMargin, top);
			top += strHeight;
		}
		topPos += scrollIncrement;
	}
	
	private void adjustTextDimension() {
		
		String[] lines = text.split("\n");
		Graphics2D graphics2d = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR).createGraphics();
		
		Font f = graphics2d.getFont().deriveFont((float)fontSize);
		graphics2d.setFont(f);
		int maxWidth = 0;
		int height = 0;
		final int strHeight = graphics2d.getFontMetrics().getHeight();
		for(String line : lines) {
			final int strWidth = graphics2d.getFontMetrics().stringWidth(line);
			if(strWidth > maxWidth) {
				maxWidth = strWidth;
			}
			height += strHeight;
		}
		Dimension d = new Dimension(maxWidth, height);
		maxTextWidth = d.width;
	}

	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#prepare(org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo, org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo, java.awt.Graphics2D, org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround)
	 */
	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {

		this.videoOutputInfo = videoOutputInfo;
		adjustTextDimension();
		
		topPos = topMargin;

	}

	@Override
	public void postFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawCurrentIcon(int width, int height, Graphics2D graphics) {
		graphics.setColor(textColor);
		TextHelper.writeText(text, graphics, height, textColor, width, height / 10);
		
	}

	@Override
	public void propertyWritten(String name) {
		if("text".equals(name)) {
			adjustTextDimension();
		}
		
	}


}
