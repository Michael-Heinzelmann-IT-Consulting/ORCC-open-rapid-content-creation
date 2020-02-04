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
import java.awt.Font;
import java.awt.FontMetrics;
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
		LEFT, CENTERED, RIGHT
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
	
	private int longestLineIdx;
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
		FontMetrics fontMetrics = graphics2d.getFontMetrics();
		int maxWidth = fontMetrics.stringWidth(lines[longestLineIdx]);
		final int strHeight = fontMetrics.getHeight();
		int minLeftMargin = (videoOutputInfo.getWidth() - maxWidth) / 2 ;
		int lineIdx = 0;
		for(String line : linesToUse) {
			int leftMargin;
			if(textAlign == TextAlign.LEFT) {
				leftMargin = minLeftMargin;
			}
			else {
				final int strWidth = graphics2d.getFontMetrics().stringWidth(lines[lineIdx]);
				int diff = maxWidth - strWidth;
				leftMargin = minLeftMargin + (textAlign == TextAlign.CENTERED ? diff / 2 : diff);
			}
			graphics2d.drawString(line, leftMargin, top);
			top += strHeight;
			lineIdx++;
		}
	}
	
	private void adjustTextModel() {
		
		lines = text.split("\n");
		int maxLen = 0;
		int idx = 0;
		for(String line : lines) {
			if(line.length() > maxLen) {
				maxLen = line.length();
				longestLineIdx = idx;
			}
			idx++;
		}
		
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
		adjustTextModel();
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
		if("text".equals(field.getName())) {
			adjustTextModel();
		}
		if("fontName".equals(field.getName())) {
			font = FontStore.getFontByMappedValue(fontName);
		}
		if("fontSize".equals(field.getName()) || "fontName".equals(field.getName())) {
			if(font != null) {
				font = font.deriveFont(dimensionHelper.getFontSizeForPercentX(fontSize));
			}
		}
	}
	
	public void setFrameRange(long frameFrom, long frameTo){
		this.frameFrom = frameFrom;
		this.frameTo = frameTo;
	}


}
