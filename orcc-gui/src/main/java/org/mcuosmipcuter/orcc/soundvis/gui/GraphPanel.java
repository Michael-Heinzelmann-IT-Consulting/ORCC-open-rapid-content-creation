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
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.swing.JPanel;
import javax.swing.Popup;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.TextHelper;
import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.DecodingCallback;
import org.mcuosmipcuter.orcc.soundvis.Mixin;
import org.mcuosmipcuter.orcc.soundvis.RealtimeSettings;
import org.mcuosmipcuter.orcc.soundvis.Renderer;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.threads.ProgressPainterThread;
import org.mcuosmipcuter.orcc.soundvis.util.ByteArrayLinearDecoder;
import org.mcuosmipcuter.orcc.util.IOUtil;


/**
 * The central panel where we draw the video in play as well as export mode.
 * This is perfect for the GUI but probably needs refactoring if we want to
 * support head less rendering.
 * @author Michael Heinzelmann
 */
public class GraphPanel extends JPanel implements Renderer, RealtimeSettings, Listener {

	private static final long serialVersionUID = 1L;
	private Mixin mixin;
	private SoundCanvasWrapper[] soundCanvasArray; // canvas list as array to work with
	private List<SettingsListener> settingsListeners = new ArrayList<>();
	
	private BufferedImage frameImage;
	private Graphics2D graphics;
	
	private float zoomFactor = 0.5f;
	private boolean autoZoom;
	int reductionModulus = 1;
	int backupReductionModulus = 1;
	

	ProgressPainterThread progressPainterThread = new ProgressPainterThread();
	private boolean updating;
	private String updateString;
	JPanel popUpContentPanel;
	Popup popup;
	
	/**
	 * Constructor, adds the mouse drag handling for the back ground image
	 */
	public GraphPanel() {
		
		setBackground(Color.LIGHT_GRAY);
		drawDefaultBackGround();
		
		addComponentListener(new ComponentAdapter(){
			@Override
			public void componentResized(ComponentEvent arg0) {
				if(autoZoom) {
					setZoomFactor(0.0f); // adapt to new size
				}
			}
			
		});
	}
	@Override
	public void progress(String msg) {
		updateString = msg;
		paintComponent(getGraphics());
	}

	@Override
	public void contextChanged(PropertyName propertyName) {
		//System.err.println("::::" + propertyName);
		if(PropertyName.BeforeSoundCanvasProperty.equals(propertyName)) {
			updateString = "Updating ...";
			updating = true;
		}
		else {
			updating = false;
			updateString = null;
		}

		if(PropertyName.VideoDimension.equals(propertyName)) {
			if(autoZoom) {
				setZoomFactor(0.0f); // adapt to new size
			}
			displayUpdate(true);
		}
		if(PropertyName.SessionChanged.equals(propertyName)) {
			frameImage = new BufferedImage(Context.getVideoOutputInfo().getWidth(), Context.getVideoOutputInfo().getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			graphics = frameImage.createGraphics();
			displayUpdate(true);
		}
		EnumSet<PropertyName> match = EnumSet.of(PropertyName.SoundCanvasProperty,
				PropertyName.SoundCanvasAdded, PropertyName.SoundCanvasList, PropertyName.SongPositionPointer);
		if (Context.getAppState() != AppState.PLAYING && match.contains(propertyName)) {
			displayUpdate(true);
		}
		
	}
	
	private void drawDefaultBackGround() {
		int width = Context.getVideoOutputInfo().getWidth();
		int height = Context.getVideoOutputInfo().getHeight();
		
		frameImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		graphics = frameImage.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
	}
	
	/**
	 * Update the display
	 */
	public void displayUpdate(boolean prepare) {

		if(Context.getAppState() == AppState.PLAYING) {
			return; // while playing updates are via newFrame()
		}
		soundCanvasArray = Context.getSoundCanvasList().toArray(new SoundCanvasWrapper[0]);
		if (prepare) {
			for (SoundCanvas soundCanvas : soundCanvasArray) {
				soundCanvas.prepare(Context.getAudioInput().getAudioInputInfo(), Context.getVideoOutputInfo());
			}
		}
		if (Context.getAppState() != AppState.INIT && Context.getAppState() != AppState.LOADING && Context.getAppState() != AppState.EXPORTING) {
			
			AudioInput audioInput = Context.getAudioInput();
			AudioFormat format = audioInput.getAudioInputInfo().getAudioFormat();
			final long samplesPerFrame = (int) format.getSampleRate()
					/ Context.getVideoOutputInfo().getFramesPerSecond();

			try (AudioInputStream ais = audioInput.getAudioStream();) {
				final long startCount = Context.getPreRun(ais, format);
				ByteArrayLinearDecoder.decodeLinear(ais, new DecodingCallback() {
					long frameCount = 0;

					@Override
					public boolean nextSample(int[] amplitudes, byte[] rawData, long sampleCount) {
						boolean cont = GraphPanel.this.nextSample(amplitudes, rawData, startCount + frameCount);
						if (sampleCount % samplesPerFrame == 0) {
							long fn = startCount + frameCount;
							for (SoundCanvas soundCanvas : soundCanvasArray) {
								soundCanvas.newFrame(fn, graphics);
								if(cont) {
									soundCanvas.postFrame();
								}
							}
							if(fn >= Context.getSongPositionPointer()) {
								//mixin.newFrame(Context.getSongPositionPointer(), false);
								return false;
							}
							frameCount++;
						}
						return cont;
					}
				});
			} catch (Exception ex) {
				IOUtil.log("" + ex);
			}
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
		if(Context.getAppState() == AppState.EXPORTING) {
			reductionModulus = 1; // always use original frame rate
		}
		else {
			reductionModulus = backupReductionModulus; // reset
		}
		//graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		//graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		//graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		//graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		
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
		try {
			super.paintComponent(g);
		}
		catch(Exception ex) {
			IOUtil.log(ex.getMessage());
		}
		if(zoomFactor != 1) {
			((Graphics2D)g).scale(zoomFactor, zoomFactor);
		}

		{
			g.drawImage(frameImage, 0, 0, Color.BLACK, null);
		}
		if(updating) {
			Composite origComposite = ((Graphics2D)g).getComposite();
			g.setColor(Color.WHITE);
			((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f)); 
			g.fillRect(0, 0, Context.getVideoOutputInfo().getWidth(), Context.getVideoOutputInfo().getHeight());
			((Graphics2D)g).setComposite(origComposite);
			TextHelper.writeText(updateString, (Graphics2D) g, 80, Color.BLACK, Context.getVideoOutputInfo().getWidth(), Context.getVideoOutputInfo().getHeight() / 2);
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
			if(frameCount < 2 || frameCount % reductionModulus == 0) {
			if(frameCount > Context.getSongPositionPointer()) {
				this.repaint(); // standard asynchronous painting
				//paintComponent(getGraphics());// synchronous on some systems better graphics but can slow down audio
				//paintImmediately(0, 0, getWidth(), getHeight());
			}
			if(mixin != null) {
				mixin.newFrame(frameCount, sendPost);
			}
		}
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
			//setBackground(Color.GREEN);
			//setBorder(new EtchedBorder());
			
			int w = (int)((float)Context.getVideoOutputInfo().getWidth() * zoomFactor);
			int h = (int)((float)Context.getVideoOutputInfo().getHeight() * zoomFactor);
			setSize(w, h);
			setPreferredSize(new Dimension(w, h));
			
			IOUtil.log(zoomFactor + " " + w + " " + h);
			//invalidate();
		}
		updateSettingListeners();
	}

	@Override
	public void setVideoRefresh(int reductionModulus) {
		this.reductionModulus = reductionModulus;
		this.backupReductionModulus = reductionModulus;
		updateSettingListeners();
	}

	private void updateSettingListeners() {
		String settingsString = "frame reduction: " + (reductionModulus == 1 ? "none" : "" + reductionModulus)
		+ " zoom: " + zoomFactor;
		for(SettingsListener sl : settingsListeners) {
			sl.update(settingsString);
		}
	}

	@Override
	public void addSettingsListener(SettingsListener settingsListener) {
		settingsListeners.add(settingsListener);
	}
}
