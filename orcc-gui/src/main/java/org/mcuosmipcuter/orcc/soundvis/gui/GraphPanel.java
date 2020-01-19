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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.EnumSet;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.TextHelper;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.Mixin;
import org.mcuosmipcuter.orcc.soundvis.Renderer;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.Zoomable;
import org.mcuosmipcuter.orcc.soundvis.threads.ProgressPainterThread;


/**
 * The central panel where we draw the video in play as well as export mode.
 * This is perfect for the GUI but probably needs refactoring if we want to
 * support head less rendering.
 * @author Michael Heinzelmann
 */
public class GraphPanel extends JPanel implements Renderer, Zoomable {

	private static final long serialVersionUID = 1L;
	private Mixin mixin;
	private SoundCanvasWrapper[] soundCanvasArray; // canvas list as array to work with
	
	private BufferedImage frameImage;
	private Graphics2D graphics;
	
	private float zoomFactor = 0.5f;
	private boolean autoZoom;

	private long frameCount;
	private boolean paintProgressOverLay;
	ProgressPainterThread progressPainterThread = new ProgressPainterThread();
	JPanel pr = new JPanel();
	private boolean updating;
	
	/**
	 * Constructor, adds the mouse drag handling for the back ground image
	 */
	public GraphPanel() {
		JLabel l = new JLabel("PROGRESS ...");
		Font f = getFont().deriveFont((float)80);
		l.setFont(f);
		pr.add(BorderLayout.CENTER, l);
		setBackground(Color.LIGHT_GRAY);
		//add(BorderLayout.CENTER, pr);
		add( pr);
		pr.setVisible(false);
		drawDefaultBackGround();
		
		Context.addListener(new Listener() {
			
			
			@Override
			public void contextChanged(PropertyName propertyName) {
				//System.err.println("::::" + propertyName);
				if(PropertyName.BeforeSoundCanvasProperty.equals(propertyName)) {
					System.err.println(getGraphics());
					//getGraphics().drawString("UPDATE", 100, 100);
					
					//getGraphics().setXORMode(Color.ORANGE);
					//graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f)); 
					//graphics.setColor(Color.WHITE);
					//System.err.println(getGraphics());
					
					
					//graphics.fillRect(0, 0, Context.getVideoOutputInfo().getWidth(), Context.getVideoOutputInfo().getHeight());
					//
//					pr.setSize(Context.getVideoOutputInfo().getWidth(), Context.getVideoOutputInfo().getHeight());
//					pr.setVisible(true);
					//repaint();
//					paintProgressOverLay = true;
				}
				else {
					//pr.setVisible(false);
//					paintProgressOverLay = false;
				}

				if(PropertyName.VideoDimension.equals(propertyName)) {
					if(autoZoom) {
						setZoomFactor(0.0f); // adapt to new size
					}
					displayUpdate(true);

				}
				EnumSet<PropertyName> match = EnumSet.of(PropertyName.SoundCanvasProperty,
						PropertyName.SoundCanvasAdded, PropertyName.SoundCanvasList, PropertyName.SongPositionPointer);
				if (Context.getAppState() != AppState.PLAYING && match.contains(propertyName)) {
					displayUpdate(true);
				}
				
			}
		});
		addComponentListener(new ComponentAdapter(){
			@Override
			public void componentResized(ComponentEvent arg0) {
				if(autoZoom) {
					setZoomFactor(0.0f); // adapt to new size
				}
			}
			
		});
	}
	
	// our 'logo'
	private void drawDefaultBackGround() {
		int width = Context.getVideoOutputInfo().getWidth();
		int height = Context.getVideoOutputInfo().getHeight();
		
		frameImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		graphics = frameImage.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.setPaint(new RadialGradientPaint(new Point(0, 0), height, new float[] {0.0f, 0.5f}, new Color[] {Color.WHITE, Color.GRAY}, CycleMethod.REFLECT));
		graphics.fillRect(0, 0, width, height);
	}
	
	/**
	 * Update the display
	 */
	public void displayUpdate(boolean prepare) {

		soundCanvasArray = Context.getSoundCanvasList().toArray(new SoundCanvasWrapper[0]);

		drawDefaultBackGround();

		for(SoundCanvas soundCanvas : soundCanvasArray) {
			if(prepare) {
				soundCanvas.prepare(Context.getAudioInput().getAudioInputInfo(), Context.getVideoOutputInfo());
			}
			soundCanvas.newFrame(Context.getAppState() == AppState.PLAYING ? frameCount : Context.getSongPositionPointer(), graphics);
		}

		repaint();
	}

	/**
	 * Sets the canvas in the context to this context and prepares it 
	 * @see SoundCanvas#prepare(AudioInputInfo, VideoOutputInfo, Graphics2D, CanvasBackGround)
	 */
	public void start(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo)  {
		frameImage = new BufferedImage(videoOutputInfo.getWidth(), videoOutputInfo.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		graphics = frameImage.createGraphics();
		soundCanvasArray = Context.getSoundCanvasList().toArray(new SoundCanvasWrapper[0]);
		for(SoundCanvas soundCanvas : soundCanvasArray) {
			soundCanvas.prepare(audioInputInfo, videoOutputInfo);
		}
		progressPainterThread.prepare(audioInputInfo, videoOutputInfo);
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

		{
			g.drawImage(frameImage, 0, 0, Color.BLACK, null);
		}
		if(updating) {
			Composite origComposite = ((Graphics2D)g).getComposite();
			g.setColor(Color.WHITE);
			((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f)); 
			g.fillRect(0, 0, Context.getVideoOutputInfo().getWidth(), Context.getVideoOutputInfo().getHeight());
			((Graphics2D)g).setComposite(origComposite);
			////progressPainterThread.newFrame(frameCount, (Graphics2D) g);
			TextHelper.writeText("Recalculating ...", (Graphics2D) g, 80, Color.BLACK, Context.getVideoOutputInfo().getWidth(), Context.getVideoOutputInfo().getHeight() / 2);
		}
		if(zoomFactor != 1) {
			((Graphics2D)g).scale(1, 1);
		}

	}

	@Override
	public boolean nextSample(int[] amplitudes, byte[] rawData, long sampleCount) {
		for(SoundCanvas soundCanvas : soundCanvasArray) {
			soundCanvas.nextSample(amplitudes);
		}
		if(mixin != null) {
			mixin.nextSample(amplitudes, rawData, sampleCount);
		}
		return true;
	}

	@Override
	public void newFrame(long frameCount, boolean sendPost) {
		for(SoundCanvas soundCanvas : soundCanvasArray) {
			soundCanvas.newFrame(frameCount, graphics);
			if(sendPost) {
				soundCanvas.postFrame();
			}
		}
		if(updating) {
			//progressPainterThread.newFrame(frameCount, graphics);
		}
		if(frameCount > Context.getSongPositionPointer()) {
			this.repaint();
		}
		if(mixin != null) {
			mixin.newFrame(frameCount, sendPost);
		}
		this.frameCount = frameCount;
	}

	@Override
	public BufferedImage getFrameImage() {
		return frameImage;
	}

	/**
	 * Sets the mixin that will receive events
	 * @param mixin
	 */
	public void setMixin(Mixin mixin) {
		this.mixin = mixin;
	}

	@Override
	public synchronized void setZoomFactor(final float zoomFactor) {
		if(zoomFactor == 0.0f) {
			int panelW = getWidth();
			int panelH = getHeight();
			int width = Context.getVideoOutputInfo().getWidth();
			int height = Context.getVideoOutputInfo().getHeight();
			
			float wFactor = (float)panelW / (float)width;
			float hFactor = (float)panelH /  (float)height;
			
			this.zoomFactor = Math.min(wFactor, hFactor);
			autoZoom = true;
		}
		else {
			this.zoomFactor = zoomFactor;
			autoZoom = false;
		}
	}

	public void setUpdating(boolean updating) {
		this.updating = updating;
	}

}
