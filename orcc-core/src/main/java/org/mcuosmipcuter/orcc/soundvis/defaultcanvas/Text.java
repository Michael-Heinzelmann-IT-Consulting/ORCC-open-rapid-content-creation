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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
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
	public static enum TextProgress {
		SCROLL, PAGE
	}
	
	private int year = Calendar.getInstance().get(Calendar.YEAR);
	private String user = System.getProperty("user.name");
	
	@UserProperty(description="the font to use")
	private MappedValue<String> fontName = FontStore.getDefaultFont();
	
	@UserProperty(description="the text to display")
	private String text = "ORCC rapid content creation\nfor entertainment, education\nand media production\n" + year + " " + user + " graphics by soundvis";
	
	@UserProperty(description="")
	@LimitedIntProperty(description="", minimum=0)
	private boolean showMargins;
	
	@UserProperty(description="font size for text in % of video height")
	@LimitedIntProperty(description="font size limitation", minimum=0)
	private int fontSize = 0;
	
	@UserProperty(description="the text alignment of lines")
	private TextAlign textAlign = TextAlign.LEFT;
	
	@UserProperty(description="text color")
	private Color textColor = Color.BLACK;
	
	@UserProperty(description="left and right margin of text in % of video width")
	int leftRightMargin = 10;
	
	@UserProperty(description="top margin of text in % of video height")
	int topMargin = 20;
	
	@UserProperty(description="bottom margin of text in % of video height")
	int bottomAutoMargin = 20;
	
	@UserProperty(description="top progress")
	private TextProgress textProgress = TextProgress.PAGE;
	
	@UserProperty(description="type the text")
	@LimitedIntProperty(description="", minimum=0)
	private boolean typing;
	
	@UserProperty(description="frames per character")
	@LimitedIntProperty(description="0 = no progress", minimum=0)
	private int modProgress = 1;
	
	VideoOutputInfo videoOutputInfo;
	private DimensionHelper dimensionHelper;
	
	private long frameFrom;
	
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
		int posInSlideDuration = (int)(frameCount - frameFrom);
		int xMargin = dimensionHelper.realX(leftRightMargin);

		if(fontSize == 0 ) {
			
			graphics2d.setFont(font);
			FontMetrics fontMetrics = graphics2d.getFontMetrics();
			int pixelsToUse = videoOutputInfo.getWidth() - xMargin * 2;
			int diff = pixelsToUse - fontMetrics.stringWidth(lines[longestLineIdx]);
			if(diff != 0) {
				int currFontSize = font.getSize();
				Font scaledFont = font;
				while((currFontSize += diff > 0 ? 1 : -1) > 0) {
					
					scaledFont = font.deriveFont((float)currFontSize);
					graphics2d.setFont(scaledFont);
					fontMetrics = graphics2d.getFontMetrics();
					if(diff > 0) {
						if(fontMetrics.stringWidth(lines[longestLineIdx]) > pixelsToUse) {
							font = font.deriveFont((float)(currFontSize - 1));
							//System.err.println("diff " + diff + " font size " + font.getSize());
							break;
						}
					}
					else {
						if(fontMetrics.stringWidth(lines[longestLineIdx]) < pixelsToUse) {
							font = scaledFont;
							//System.err.println("diff " + diff + " font size " + font.getSize());
							break;
						}
					}
				}
			}
		}
		
		graphics2d.setFont(font);
		graphics2d.setColor(textColor);
		
		int topPixels = dimensionHelper.realY(topMargin);
		int bottomPixels = dimensionHelper.realY(bottomAutoMargin);

		FontMetrics fontMetrics = graphics2d.getFontMetrics();
		final int maxWidth = fontMetrics.stringWidth(lines[longestLineIdx]);
		final int strHeight = fontMetrics.getHeight();
		final int ascent = fontMetrics.getAscent();
		
		int pixelsToUse = dimensionHelper.getVideoHeight() - topPixels - bottomPixels;
		int linesToUse = pixelsToUse / strHeight;
		if(linesToUse == 0) {
			linesToUse = 1;
		}

		int progessIdx = modProgress != 0 ? posInSlideDuration / modProgress : text.length();
		
		int caretRowIdx = 0;
		int caretColIdx = 0;
		int completedRowIdx = 0;
		int len = 0;
		for(String line : lines) {

			int lineLegth = line.length() + 1; // newline char!
			if(modProgress != 0) {
				if(progessIdx >= len && progessIdx < len + lineLegth) {
					caretColIdx = progessIdx - len ;

					if(progessIdx == text.length()) {
						completedRowIdx = caretRowIdx;
					}
					else {
						completedRowIdx = caretRowIdx - 1;
					}
					break;
				}

			}
			caretColIdx = lineLegth - 1;

			len += lineLegth;
			if(len >= text.length()) {
				break;
			}
			caretRowIdx++;
			completedRowIdx = caretRowIdx;
			
			
		}
		//System.err.println(completedRowIdx + " caretRowIdx: " + caretRowIdx + " caretColIdx: " + caretColIdx + " progessIdx: " + progessIdx + " text.length: " + text.length());
		
		
		int startIdx = 0;
		if(modProgress != 0) { 
			if(linesToUse - caretRowIdx > 0) {
				startIdx = 0; // more rows available than text rows
			}
			else {
				if(textProgress == TextProgress.SCROLL) {
					startIdx = caretRowIdx - linesToUse + 1;
				}
				if(textProgress == TextProgress.PAGE) {
					startIdx = caretRowIdx -((caretRowIdx ) % linesToUse);
				}
			}
			
		}
		//System.err.println("linesToUse: " + linesToUse + " startIdx: " + startIdx);

		int minLeftMargin = (videoOutputInfo.getWidth()  - maxWidth) / 2 ;
		int lineTop = topPixels + ascent;// strHeight;
		int lineIdx = 0;

		for(String line : lines) {
			if(lineIdx >= startIdx && lineIdx < startIdx + linesToUse) {
				int leftMargin;
				if(textAlign == TextAlign.LEFT) {
					leftMargin = minLeftMargin;
				}
				else {
					final int strWidth = graphics2d.getFontMetrics().stringWidth(line);
					int diff = maxWidth - strWidth;
					leftMargin = minLeftMargin + (textAlign == TextAlign.CENTERED ? diff / 2 : diff);
				}
				String lineToUse;
				if(typing) {
					if(lineIdx > caretRowIdx) {
						break;
					}
					if(lineIdx <= completedRowIdx) {
						lineToUse = line;
					}
					else {
						lineToUse = line.substring(0, caretColIdx);
					}
				}
				else {
					lineToUse = line;
				}
				graphics2d.drawString(lineToUse, leftMargin, lineTop);	
				
				lineTop += strHeight;
			}
			lineIdx++;
		}
		if(showMargins) {
			Stroke origStroke = graphics2d.getStroke();
			float xd = dimensionHelper.realX(5);
			graphics2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float [] {xd, xd}, 0f));
			graphics2d.drawLine(0, topPixels, videoOutputInfo.getWidth(), topPixels);
			graphics2d.drawLine(0, videoOutputInfo.getHeight() - bottomPixels, videoOutputInfo.getWidth(),
					videoOutputInfo.getHeight() - bottomPixels);
			float yd = dimensionHelper.realY(5);
			graphics2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float [] {yd, yd}, 0f));
			graphics2d.drawLine(xMargin, 0, xMargin, videoOutputInfo.getHeight());
			graphics2d.drawLine(videoOutputInfo.getWidth() - xMargin, 0, videoOutputInfo.getWidth() - xMargin,
					videoOutputInfo.getHeight());
			graphics2d.setStroke(origStroke);
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
			f = graphics2d.getFont().deriveFont(fontSize == 0 ? 1 : fontSize);
			graphics2d.dispose();
		}
		font = f.deriveFont(fontSize == 0 ? 1 : fontSize);
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
				font = font.deriveFont(fontSize);
			}
		}
	}
	
	public void setFrameRange(long frameFrom, long frameTo){
		this.frameFrom = frameFrom;
	}


}
