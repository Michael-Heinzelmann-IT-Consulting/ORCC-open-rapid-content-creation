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
import java.util.Calendar;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.TextHelper;

/**
 * @author Michael Heinzelmann
 *
 */
public class Text implements SoundCanvas {
	
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
	@UserProperty(description="number of spaces used for tabs")
	@LimitedIntProperty(description="tab size limitation, 0 means no replacement", minimum=0)
	private int tabReplacement = 2;
	
	private CanvasBackGround canvasBackGround;
	private Graphics2D graphics2d;
	VideoOutputInfo videoOutputInfo;
	
	private double topPos;
	private int maxTextWidth;
	private double scrollIncrement;
	
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
	public void newFrame(long frameCount) {
		canvasBackGround.drawBackGround();
		if(text == null || text.length() == 0) {
			return;
		}
		Font f = graphics2d.getFont().deriveFont((float)fontSize);
		graphics2d.setFont(f);
		graphics2d.setColor(textColor);
		
		
		String[] lines = text.split("\n");
		
		int top = (int)topPos; // TODO
		final int strHeight = graphics2d.getFontMetrics().getHeight();
		int leftMargin = (videoOutputInfo.getWidth() - maxTextWidth) / 2 ;
		for(String line : lines) {
			graphics2d.drawString(line, leftMargin, top);
			top += strHeight;
		}
		topPos -= scrollIncrement;
	}
	
	private Dimension getTextDimesion(String[] lines) {
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
		return new Dimension(maxWidth, height);
	}

	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#prepare(org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo, org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo, java.awt.Graphics2D, org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround)
	 */
	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo, Graphics2D graphics,
			CanvasBackGround canvasBackGround) {
		this.canvasBackGround = canvasBackGround;
		this.graphics2d = graphics;
		this.videoOutputInfo = videoOutputInfo;
		topPos = topMargin;
		
		double framesInVideo = (audioInputInfo.getFrameLength() / audioInputInfo.getAudioFormat().getSampleRate() * videoOutputInfo.getFramesPerSecond());
		
		if(tabReplacement > 0) {
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < tabReplacement; i++) {
				b.append(" ");
			}
			String str = b.toString();
			text = text.replaceAll("\t", str);
		}
		String[] lines = text.split("\n");
		Dimension d = getTextDimesion(lines);
		maxTextWidth = d.width;
		System.err.println(d);
		if(d.height < videoOutputInfo.getHeight()) {
			scrollIncrement = 0;
		}
		else {
			int distanceToScroll = d.height - videoOutputInfo.getHeight() + topMargin;
			scrollIncrement = distanceToScroll / framesInVideo;
		}

	}

	@Override
	public void preView(int width, int height, Graphics2D graphics) {
		String text = "draws text onto the background\n" +
				"long text is autoscrolled";
		graphics.setXORMode(Color.BLACK);
		TextHelper.writeText(text, graphics, 24f, Color.WHITE, width, height / 2);
		graphics.setPaintMode();
	}

}
