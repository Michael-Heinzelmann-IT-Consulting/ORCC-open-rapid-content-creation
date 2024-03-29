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
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Calendar;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayUnit;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.MappedValue;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyGroup;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyListener;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.ColorHelper;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;
import org.mcuosmipcuter.orcc.api.util.TextHelper;
import org.mcuosmipcuter.orcc.soundvis.FontStore;
import org.mcuosmipcuter.orcc.soundvis.effects.BackGround;
import org.mcuosmipcuter.orcc.soundvis.effects.Fader;
import org.mcuosmipcuter.orcc.soundvis.effects.Mover;
import org.mcuosmipcuter.orcc.soundvis.effects.Positioner;
import org.mcuosmipcuter.orcc.soundvis.effects.Repeater;
import org.mcuosmipcuter.orcc.soundvis.effects.Rotator;
import org.mcuosmipcuter.orcc.soundvis.effects.Shearer;
import org.mcuosmipcuter.orcc.util.IOUtil;

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

	@ChangesIcon
	@UserProperty(description = "the text to display")
	private String text = "ORCC rapid content creation\nfor entertainment, education\nand media production\n" + year
			+ " " + user + " graphics by soundvis";

	//// font
	@SuppressWarnings("unused") // used by reflection
	private PropertyGroup fontProps = new PropertyGroup("fontName", "fontSize", "textAlign", "textColor");
	
	@UserProperty(description = "the font to use")
	private MappedValue<String> fontName = FontStore.getDefaultFont();
	
	@UserProperty(description = "font size for text in % of video height", unit = Unit.POINTS)
	@LimitedIntProperty(description = "font size limitation", minimum = 0)
	@NumberMeaning(numbers = 0, meanings = "auto")
	private int fontSize = 0;

	@UserProperty(description = "the text alignment of lines")
	private TextAlign textAlign = TextAlign.LEFT;

	@ChangesIcon
	@UserProperty(description = "text color")
	private Color textColor = Color.BLACK;

	//// margins
	@SuppressWarnings("unused") // used by reflection
	private PropertyGroup margins = new PropertyGroup("leftRightMargin", "topMargin", "bottomAutoMargin", "showMargins");
	
	@LimitedIntProperty(description = "50% is center", maximum = 50)
	@UserProperty(description = "left and right margin of text in % of video width", unit = Unit.PERCENT_VIDEO)
	int leftRightMargin = 10;

	@UserProperty(description = "top margin of text in % of video height", unit = Unit.PERCENT_VIDEO)
	int topMargin = 20;

	@UserProperty(description = "bottom margin of text in % of video height", unit = Unit.PERCENT_VIDEO)
	int bottomAutoMargin = 20;
	
	@UserProperty(description = "display outline")
	private boolean showMargins;
	
	///// text progress
	@SuppressWarnings("unused") // used by reflection
	private PropertyGroup progress = new PropertyGroup("typing", "textProgress", "modProgress");
	
	@UserProperty(description = "type the text")
	private boolean typing;
	
	@UserProperty(description = "top progress")
	private TextProgress textProgress = TextProgress.PAGE;

	////

	@UserProperty(description = "frames per character")
	@NumberMeaning(numbers = {-1, 0, 1}, meanings = {"slower", "off", "faster"})
	private int modProgress = 0;
	
	@NestedProperty(description = "background")
	private BackGround backGround = new BackGround();
	
	@NestedProperty(description = "x and y position")
	Positioner positioner = new Positioner();

	@NestedProperty(description = "fading in and out")
	private Fader fader = new Fader();

	@NestedProperty(description = "moving in and out")
	private Mover mover = new Mover();

	@NestedProperty(description = "shear")
	private Shearer shearer = new Shearer();
	@NestedProperty(description = "shear")
	private Shearer ishearer = new Shearer();
	
	@NestedProperty(description = "rotate text in and out")
	private Rotator rotator = new Rotator();
	@NestedProperty(description = "rotate glyph in and out")
	private Rotator iRotator = new Rotator();

	@NestedProperty(description = "repeating inside from and to")
	private Repeater repeater = new Repeater(fader, mover, shearer, ishearer, rotator, iRotator);
	
	@UserProperty(description = "use full text per repeat or repeat number of lines")
	@LimitedIntProperty(description = "not negative", minimum = 0)
	@NumberMeaning(numbers = 0, meanings = "all")
	private int linesPerRepeat;


	VideoOutputInfo videoOutputInfo;
	private DimensionHelper dimensionHelper;

	private long frameFrom;
	private long frameTo;

	private String[] lines;
	private Font font;
	private int autoAdjustedFontSize;
	private Stroke strokeX;
	private Stroke strokeY;
	
	private String[] getLinesToDisplay(final int repeatIndex) {
		if(linesPerRepeat > 0 && linesPerRepeat < lines.length) {
			String[] result = new String[linesPerRepeat]; 
			for(int i = 0; i < linesPerRepeat; i++){
				result [i] = lines[(repeatIndex * linesPerRepeat + i) % lines.length];
			}
			return result;
		}
		return lines;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#newFrame(long)
	 */
	@Override
	public void newFrame(long frameCount, Graphics2D graphics2d) {

		if (text == null || text.length() == 0 || lines == null) {
			return;
		}

		int xMargin = dimensionHelper.realX(leftRightMargin);
		int topPixels = dimensionHelper.realY(topMargin);
		int bottomPixels = dimensionHelper.realY(bottomAutoMargin);
		int yPixelsToUse = dimensionHelper.getVideoHeight() - topPixels - bottomPixels;

		if (fontSize == 0) {

			int xPixelsToUse = videoOutputInfo.getWidth() - xMargin * 2;

			if (autoAdjustedFontSize == 0) {
				int currFontSize = font.getSize();
				Font scaledFont = font;
				String prevMsg = "";
				while ((currFontSize += 1) > 0) {

					scaledFont = font.deriveFont((float) currFontSize);
					graphics2d.setFont(scaledFont);
					FontMetrics fontMetricsPrev = graphics2d.getFontMetrics();
					int linesMaxWidth = 0;
					int longestLineIdx = -1;
					int linesHeight = 0;
					for (int i = 0; i < lines.length; i++) {
						int px = fontMetricsPrev.stringWidth(lines[i]);
						if (px >= linesMaxWidth) {
							linesMaxWidth = px;
							longestLineIdx = i;
						}
						linesHeight += fontMetricsPrev.getHeight();
					}
					fontMetricsPrev = graphics2d.getFontMetrics();
					
					if (linesMaxWidth > xPixelsToUse
							|| linesHeight > yPixelsToUse) {
						font = font.deriveFont((float) (currFontSize - 1));
						IOUtil.log("exceeded   longestLineIdx: " + longestLineIdx + " linesMaxWidth/xPixelsToUse:" + linesMaxWidth + "/" + xPixelsToUse + " linesHeight/yPixelsToUse:" + linesHeight + "/" + yPixelsToUse + " font size "
								+ currFontSize);
						IOUtil.log("using now: " + prevMsg);
						autoAdjustedFontSize = font.getSize();
						break;
					}
					prevMsg = "longestLineIdx: " + longestLineIdx + " linesMaxWidth/xPixelsToUse:" + linesMaxWidth + "/" + xPixelsToUse + " linesHeight/yPixelsToUse:" + linesHeight + "/" + yPixelsToUse + " font size "
							+ currFontSize;
				}
			}
		} else {
			font = font.deriveFont((float) fontSize);
		}
		for (DisplayUnit displayUnit : repeater.repeat(frameFrom, frameTo, frameCount)) {
			graphics2d.setFont(font);
			graphics2d.setColor(textColor);

			FontMetrics fontMetrics = graphics2d.getFontMetrics();
			final int strHeight = fontMetrics.getHeight();
			final int ascent = fontMetrics.getAscent();

			int linesToUse = yPixelsToUse / strHeight;
			if (linesToUse == 0) {
				linesToUse = 1;
			}

			int progessIdx;
			if (modProgress != 0) {
				if (modProgress > 0) {
					progessIdx = displayUnit.currentPosition * modProgress; // speedup
				} else {
					progessIdx = displayUnit.currentPosition / Math.abs(modProgress); // slowdown
				}
			} else {
				progessIdx = text.length();
			}

			int caretRowIdx = 0;
			int caretColIdx = 0;
			int completedRowIdx = 0;
			int len = 0;
			String[] linesToDisplay = getLinesToDisplay(displayUnit.index);
			for (String line : linesToDisplay) {

				int lineLegth = line.length() + 1; // newline char!
				if (modProgress != 0) {
					if (progessIdx >= len && progessIdx < len + lineLegth) {
						caretColIdx = progessIdx - len;

						if (progessIdx == text.length()) {
							completedRowIdx = caretRowIdx;
						} else {
							completedRowIdx = caretRowIdx - 1;
						}
						break;
					}

				}
				caretColIdx = lineLegth - 1;

				len += lineLegth;
				if (len >= text.length()) {
					break;
				}
				caretRowIdx++;
				completedRowIdx = caretRowIdx;

			}

			int startIdx = 0;
			if (modProgress != 0) {
				if (linesToUse - caretRowIdx > 0) {
					startIdx = 0; // more rows available than text rows
				} else {
					if (textProgress == TextProgress.SCROLL) {
						startIdx = caretRowIdx - linesToUse + 1;
					}
					if (textProgress == TextProgress.PAGE) {
						startIdx = caretRowIdx - ((caretRowIdx) % linesToUse);
					}
				}

			}

			int lineTop = topPixels + ascent;
			int lineIdx = 0;

			final Composite origComposite = fader.fade(graphics2d, displayUnit);

			final AffineTransform saveTransfrom = graphics2d.getTransform();

			AffineTransform transform = graphics2d.getTransform();
			transform.concatenate(shearer.shear(displayUnit));
			transform.concatenate(mover.move(displayUnit.currentPosition, displayUnit.duration));
			transform.concatenate(positioner.position(dimensionHelper));

			transform.concatenate(rotator.rotate(displayUnit.currentPosition, displayUnit.duration, (videoOutputInfo.getWidth()) / 2, (videoOutputInfo.getHeight() ) / 2));

			if (!transform.isIdentity()) {
				graphics2d.setTransform(transform);
			}
			backGround.draw(graphics2d, videoOutputInfo.getWidth(), videoOutputInfo.getHeight(), textColor);

			for (String line : linesToDisplay) {
				if (lineIdx >= startIdx && lineIdx < startIdx + linesToUse) {
					int leftMargin;
					if (textAlign == TextAlign.LEFT) {
						leftMargin = xMargin;
					} else {
						final int strWidth = graphics2d.getFontMetrics().stringWidth(line);
						if (textAlign == TextAlign.RIGHT) {
							leftMargin = videoOutputInfo.getWidth() - xMargin - strWidth;
						} else {
							int diff = videoOutputInfo.getWidth() - strWidth;
							leftMargin = diff / 2;
						}
					}

					String lineToUse;
					if (typing) {
						if (lineIdx > caretRowIdx) {
							break;
						}
						if (lineIdx <= completedRowIdx) {
							lineToUse = line;
						} else {
							lineToUse = line.substring(0, caretColIdx);
						}
					} else {
						lineToUse = line;
					}
					FontRenderContext frx = graphics2d.getFontRenderContext();
					GlyphVector gv = font.createGlyphVector(frx, lineToUse);



					for (int i = 0; i < lineToUse.length(); i++) {
						Rectangle2D go = gv.getGlyphOutline(i).getBounds2D();
						AffineTransform glyphTx = ishearer.shear(displayUnit);
						AffineTransform glyphTxR = iRotator.rotate(displayUnit.currentPosition, 
								displayUnit.duration, (int)go.getWidth()/2, (int)
								go.getCenterY());

						glyphTx.concatenate(glyphTxR);
						gv.setGlyphTransform(i, glyphTx);

			
					}
					graphics2d.drawGlyphVector(gv, leftMargin, lineTop);
					// graphics2d.drawString(lineToUse, leftMargin, lineTop);

					lineTop += strHeight;
				}
				lineIdx++;
			}
			if (showMargins) {
				Stroke origStroke = graphics2d.getStroke();
				graphics2d.setStroke(strokeX);
				graphics2d.drawLine(0, topPixels, videoOutputInfo.getWidth(), topPixels);
				graphics2d.drawLine(0, videoOutputInfo.getHeight() - bottomPixels, videoOutputInfo.getWidth(),
						videoOutputInfo.getHeight() - bottomPixels);
				graphics2d.setStroke(strokeY);
				graphics2d.drawLine(xMargin, 0, xMargin, videoOutputInfo.getHeight());
				graphics2d.drawLine(videoOutputInfo.getWidth() - xMargin, 0, videoOutputInfo.getWidth() - xMargin,
						videoOutputInfo.getHeight());
				graphics2d.setStroke(origStroke);
			}

			graphics2d.setComposite(origComposite);
			saveTransfrom.setToIdentity();
			graphics2d.setTransform(saveTransfrom);
		}
	}

	private void adjustTextModel() {

		lines = text.split("\n");

	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo) {

		if (!videoOutputInfo.equals(this.videoOutputInfo)) {
			dimensionHelper = new DimensionHelper(videoOutputInfo);
			float xd = dimensionHelper.realX(5);
			strokeX = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { xd, xd }, 0f);
			float yd = dimensionHelper.realY(5);
			strokeY = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { yd, yd }, 0f);
		}

		this.videoOutputInfo = videoOutputInfo;
		autoAdjustedFontSize = 0;

		Font f = null;
		if (fontName != null) {
			f = FontStore.getFontByMappedValue(fontName);
		}
		if (f == null) {
			Graphics2D graphics2d = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR).createGraphics();
			f = graphics2d.getFont().deriveFont(fontSize == 0 ? 1f : (float) fontSize);
			graphics2d.dispose();
		}
		if (f != null) {
			font = f.deriveFont(fontSize != 0 ? (float) fontSize : 1);
		}

		adjustTextModel();
	}

	@Override
	public void postFrame() {
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		if(backGround.isDrawingEnabled()) {
			Color bg = backGround.getColorToUse(textColor);
			graphics.setColor(bg);
			graphics.fillRect(0, 0, width, height);
		}
		else if(ColorHelper.averageRGB(textColor) > 230) {
			graphics.setColor(Color.LIGHT_GRAY);
			graphics.fillRect(0, 0, width, height);
		}
		graphics.setColor(textColor);
		TextHelper.writeText(text, graphics, height / 3, textColor, width, height / 3);

	}

	@Override
	public void propertyWritten(Field field) {

		autoAdjustedFontSize = 0; // needs new adjustment
		if (font != null) {
			font = font.deriveFont(fontSize != 0 ? (float) fontSize : 1);
		}
		if ("text".equals(field.getName())) {
			adjustTextModel();
		}
		if ("fontName".equals(field.getName())) {
			font = FontStore.getFontByMappedValue(fontName);
		}
	}

	public void setFrameRange(long frameFrom, long frameTo) {
		this.frameFrom = frameFrom;
		this.frameTo = frameTo;
	}

	@Override
	public DisplayDuration<?>[] getFrameFromTos() {
		return repeater.getFrameFromTos(frameFrom, frameTo);
	}

}
