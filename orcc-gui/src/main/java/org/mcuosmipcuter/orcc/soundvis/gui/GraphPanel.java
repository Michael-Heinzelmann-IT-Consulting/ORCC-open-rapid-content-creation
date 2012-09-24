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
package org.mcuosmipcuter.orcc.soundvis.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Mixin;
import org.mcuosmipcuter.orcc.soundvis.Renderer;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;


/**
 * The central panel where we draw the video in play as well as export mode.
 * This is perfect for the GUI but probably needs refactoring if we want to
 * support head less rendering.
 * @author Michael Heinzelmann
 */
public class GraphPanel extends JPanel implements Renderer, CanvasBackGround {
	
	/**
	 * Type of background to use
	 * @author Michael Heinzelmann
	 */
	public enum BGType {
		/**
		 * Solid color back ground 
		 */
		COLOR, 
		/**
		 * User selected image as back ground
		 */
		IMAGE, 
		/**
		 * No background, this means all graphics add up
		 */
		NONE
	}

	private BGType bgType = BGType.COLOR;
	private static final long serialVersionUID = 1L;
	private Mixin mixin;
	private List<SoundCanvasWrapper> soundCanvasList; // canvas list to work with
	
	private BufferedImage frameImage;
	private Graphics2D graphics;
	private VideoOutputInfo videoOutputInfo;
	
	private float zoomFactor = 0.5f;
	
	private BufferedImage bgImage;
	private Color bgColor = Color.WHITE;
	private int bgX;
	private int bgY;
	private int xMoveStart;
	private int yMoveStart;
	
	long frameCount;
	long frameBgDrawn;
	
	/**
	 * Constructor, adds the mouse drag handling for the back ground image
	 */
	public GraphPanel() {
		
		setBackground(Color.LIGHT_GRAY);
		drawDefaultBackGround();
		bgImage = frameImage;
		
		addMouseMotionListener(new MouseAdapter() {
			
			@Override
			public void mouseDragged(MouseEvent e) {
				
				if(bgImage != null && bgType == BGType.IMAGE) {
					bgX += e.getX() - xMoveStart;
					bgY += e.getY() - yMoveStart;
					graphics.setColor(Color.GRAY);
					int width = Context.getVideoOutputInfo().getWidth();
					int height = Context.getVideoOutputInfo().getHeight();
					graphics.fillRect(0, 0, width, height);
					graphics.drawImage(bgImage, bgX, bgY, Color.BLACK, null);
					repaint();
					xMoveStart = e.getX();
					yMoveStart = e.getY();
				}
			}
		});
		addMouseListener(new MouseAdapter() {

			Cursor cursor;
			@Override
			public void mousePressed(MouseEvent e) {
				if(bgImage != null && bgType == BGType.IMAGE) {
				xMoveStart = e.getX();
				yMoveStart = e.getY();
				cursor = getCursor();
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {

				if( cursor != null) {
					setCursor(cursor);
				}
			}
			
		});
		videoOutputInfo = Context.getVideoOutputInfo();
	}
	
	// our 'logo'
	private void drawDefaultBackGround() {
		int width = Context.getVideoOutputInfo().getWidth();
		int height = Context.getVideoOutputInfo().getHeight();
		
		frameImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		graphics = frameImage.createGraphics();
		graphics.setColor(bgColor);
		graphics.setPaint(new RadialGradientPaint(new Point(0, 0), height, new float[] {0.0f, 0.5f}, new Color[] {Color.WHITE, Color.GRAY}, CycleMethod.REFLECT));
		graphics.fillRect(0, 0, width, height);
	}
	
	@Override
	public void drawBackGround() {
		if(frameCount == frameBgDrawn) {
			return;
		}
		if(bgType == BGType.IMAGE && bgImage != null) {
			graphics.drawImage(bgImage, bgX, bgY, Color.BLACK, null);
		}
		else if(bgType == BGType.COLOR) {
			graphics.setColor(bgColor);
			graphics.fillRect(0, 0, videoOutputInfo.getWidth(), videoOutputInfo.getHeight());
		}
		// else nothing, canvas is responsible for the background

		frameBgDrawn = frameCount;
	}
	/**
	 * Displays the preview for the canvas set in the context 
	 */
	public void preView() {
		drawDefaultBackGround();
		drawBackGround();
		VideoOutputInfo voi = Context.getVideoOutputInfo();
		for(SoundCanvasWrapper s : Context.getSoundCanvasList()) {
			s.preView(voi.getWidth(), voi.getHeight(), graphics);
		}
		repaint();
	}

	/**
	 * Sets the canvas in the context to this context and prepares it 
	 * @see SoundCanvas#prepare(AudioInputInfo, VideoOutputInfo, Graphics2D, CanvasBackGround)
	 */
	public void start(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo)  {
		this.videoOutputInfo = videoOutputInfo;
		frameImage = new BufferedImage(videoOutputInfo.getWidth(), videoOutputInfo.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		graphics = frameImage.createGraphics();
		soundCanvasList = Context.getSoundCanvasList();
		for(SoundCanvas soundCanvas : soundCanvasList) {
			soundCanvas.prepare(audioInputInfo, videoOutputInfo, graphics, this);
		}
		mixin.start(audioInputInfo, videoOutputInfo);
	}

	/**
	 * Draws the frame image, both still images preview as well as video animated
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(zoomFactor != 1) {
			((Graphics2D)g).scale(zoomFactor, zoomFactor);
		}
		g.drawImage(frameImage, 0, 0, Color.BLACK, null);
		if(zoomFactor != 1) {
			((Graphics2D)g).scale(1, 1);
		}

	}

	@Override
	public boolean nextSample(int[] amplitudes, byte[] rawData, long sampleCount) {
		for(SoundCanvas soundCanvas : soundCanvasList) {
			soundCanvas.nextSample(amplitudes);
		}
		if(mixin != null) {
			mixin.nextSample(amplitudes, rawData, sampleCount);
		}
		return true;
	}

	@Override
	public void newFrame(long frameCount) {
		for(SoundCanvas soundCanvas : soundCanvasList) {
			soundCanvas.newFrame(frameCount);
		}
		if(frameCount > Context.getSongPositionPointer()) {
			this.repaint();
		}
		
		if(mixin != null) {
			mixin.newFrame(frameCount);
		}
		
		this.frameCount = frameCount;
	}

	public BufferedImage getFrameImage() {
		return frameImage;
	}

	public void setMixin(Mixin mixin) {
		this.mixin = mixin;
	}

	public synchronized float getZoomFactor() {
		return zoomFactor;
	}

	public synchronized void setZoomFactor(float zoomFactor) {
		this.zoomFactor = zoomFactor;
	}

	public synchronized BGType getBgImageType() {
		return bgType;
	}

	public synchronized void setBgImageType(BGType bgImageType) {
		this.bgType = bgImageType;
		drawBackGround();
		repaint();
	}

	public synchronized BufferedImage getBgImage() {
		return bgImage;
	}

	public synchronized void setBgImage(BufferedImage bgImage) {
		this.bgImage = bgImage;
		drawBackGround();
		repaint();
	}

	public synchronized Color getBgColor() {
		return bgColor;
	}

	public synchronized void setBgColor(Color bgGroundColor) {
		this.bgColor = bgGroundColor;
		drawBackGround();
		repaint();
	}

}
