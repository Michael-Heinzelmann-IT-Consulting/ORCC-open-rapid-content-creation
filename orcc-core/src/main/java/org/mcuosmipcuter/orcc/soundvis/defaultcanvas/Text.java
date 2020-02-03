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
import java.lang.reflect.Field;
import java.util.Calendar;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.MappedValue;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyListener;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;
import org.mcuosmipcuter.orcc.api.util.TextHelper;
import org.mcuosmipcuter.orcc.soundvis.FontStore;

/**
 * @author Michael Heinzelmann
 *
 */
public class Text implements SoundCanvas, PropertyListener {
	
	public static enum TextAlign {
		LEFT, CENTERED //, JUSTIFY TODO
	}
	
	private int year = Calendar.getInstance().get(Calendar.YEAR);
	private String user = System.getProperty("user.name");
	
	@UserProperty(description="the font to use")
	private MappedValue<String> fontName = FontStore.getDefaultFont();
	
	@UserProperty(description="the text to display")
	private String text = "ORCC rapid content creation\nfor entertainment, education\nand media production\n" + year + " " + user + " graphics by soundvis";
	
	@UserProperty(description="font size for text in % of video height")
	@LimitedIntProperty(description="font size limitation", minimum=1)
	private int fontSize = 4;
	
	@UserProperty(description="the text alignment of lines")
	private TextAlign textAlign = TextAlign.LEFT;
	
	@UserProperty(description="text color")
	private Color textColor = Color.BLACK;
	
	@UserProperty(description="top margin of text in % of video height")
	int topMargin = 20;
	
	@UserProperty(description="")
	@LimitedIntProperty(description="", minimum=0)
	private int modTyping = 0;
	
	VideoOutputInfo videoOutputInfo;
	private DimensionHelper dimensionHelper;
	
	private long frameFrom;
	private long frameTo;
	
	private int maxTextWidth;
	private String[] lines;
	private Font font;
	
	
	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#nextSample(int[])
	 */
	@Override
	public void nextSample(int[] amplitudes) {
	}

	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#newFrame(long)
	 */
	@Override
	public void newFrame(long frameCount, Graphics2D graphics2d) {

		if(text == null || text.length() == 0 || lines == null) {
			return;
		}
		String[] linesToUse;
		if(modTyping != 0 ) {
			int posInSlideDuration = (int)(frameCount - frameFrom);
			
			int charPos = posInSlideDuration / modTyping;
			
			if(charPos >= 0 && charPos< text.length()) {
				linesToUse = text.substring(0, charPos).split("\n");
			}
			else {	
				linesToUse = lines;
			}
		}
		else {
			linesToUse = lines;
		}
		
		graphics2d.setFont(font);
		graphics2d.setColor(textColor);
		
		int top = dimensionHelper.realY(topMargin);
		final int strHeight = graphics2d.getFontMetrics().getHeight();
		int minLeftMargin = (videoOutputInfo.getWidth() - maxTextWidth) / 2 ;
		int lineIdx = 0;
		for(String line : linesToUse) {
			int leftMargin;
			if(textAlign == TextAlign.CENTERED) {
				final int strWidth = graphics2d.getFontMetrics().stringWidth(lines[lineIdx]);
				leftMargin = minLeftMargin + (maxTextWidth - strWidth) / 2;
			}
			else {
				leftMargin = minLeftMargin;
			}
			graphics2d.drawString(line, leftMargin, top);
			top += strHeight;
			lineIdx++;
		}
	}
	
	private void adjustTextDimension() {
		
		lines = text.split("\n");
		Graphics2D graphics2d = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR).createGraphics();
		
		graphics2d.setFont(font);
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

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		this.videoOutputInfo = videoOutputInfo;
		dimensionHelper = new DimensionHelper(videoOutputInfo);
		Font f = null;
		if(fontName != null) {
			f = FontStore.getFontByMappedValue(fontName);
		}
		if(f == null) {
			Graphics2D graphics2d  = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR).createGraphics();
			f = graphics2d.getFont().deriveFont(dimensionHelper.getFontSizeForPercentX(fontSize));
			graphics2d.dispose();
		}
		font = f.deriveFont(dimensionHelper.getFontSizeForPercentX(fontSize));
		adjustTextDimension();
	}

	@Override
	public void postFrame() {
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		graphics.setColor(textColor);
		TextHelper.writeText(text, graphics, height, textColor, width, height / 10);
		
	}

	@Override
	public void propertyWritten(Field field) {
		adjustTextDimension();
	}
	
	public void setFrameRange(long frameFrom, long frameTo){
		this.frameFrom = frameFrom;
		this.frameTo = frameTo;
	}


}
