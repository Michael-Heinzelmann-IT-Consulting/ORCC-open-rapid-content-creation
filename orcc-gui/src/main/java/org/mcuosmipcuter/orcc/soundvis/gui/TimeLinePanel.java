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
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.CallBack;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.SuperSample;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.SuperSampleData;

/**
 * Panel that displays the timeline and the waveform and sets the song position pointer.
 * @author Michael Heinzelmann
 */
public class TimeLinePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	// fixed layout
	private final int margin = 20;
	private final int displaySecondsStep = 5;
	
	// state
	private boolean loading;
	
	// calculated data
	private SuperSampleData superSampleData;
	private int noOfSamples;
	
	// user and play positions
	private int selectPos = margin;
	private long samplePosition;
	
	// data from input / output
	private int samplesPerFrame;
	private int sampleRate;
	private long totalSampleLength;
	private int videoFrameRate;
	
	/**
	 * Sets a gray background and adds listeners
	 */
	public TimeLinePanel() {
		
		setBackground(Color.GRAY);
		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				selectPos = e.getX();
				if(selectPos < margin) {
					selectPos = margin;
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				int pixel = selectPos - margin;
				Context.setSongPositionPointer(pixel * noOfSamples / samplesPerFrame);
				
			}
			
		});
		addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				final int oldSelectPos = selectPos;
				selectPos = e.getX();
				if(selectPos < margin) {
					selectPos = margin;
				}
				if(selectPos != oldSelectPos) {
					repaint();
				}
			}
			
		});
		Context.addListener(new Listener() {
			
			@Override
			public void contextChanged(PropertyName propertyName) {
				if(PropertyName.AudioInputInfo.equals(propertyName)) {
					setInputOutputData();
					// make the asynchronous super sampling
					AudioInput audioInput = Context.getAudioInput();
					totalSampleLength = audioInput.getAudioInputInfo().getFrameLength();
					noOfSamples = (int)(totalSampleLength / (getWidth() - margin*2)) + 1; 
					SubSampleThread superSample = new SubSampleThread(audioInput, noOfSamples, new CallBack() {
						@Override
						public void finishedSampling(SuperSampleData superSampleData) {
							TimeLinePanel.this.superSampleData = superSampleData;
							selectPos = margin;
							samplePosition = 0;
							Context.setSongPositionPointer(0);
							loading = false;
							TimeLinePanel.this.repaint();
						}
					});
					loading = true;
					repaint();
					superSample.start();
				
				}
//				if(PropertyName.FrameRate.equals(propertyName)) {
//				// TODO issue #23	
//				}
			}
		});
	}
	
	// paint a message
	private void paintLoading(Graphics g) {
		g.setColor(Color.GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.WHITE);
		g.drawString("loading ...", getWidth() / 2, getHeight() / 2);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(loading) {
			paintLoading(g);
			return;
		}
		long pos = 0;
		if(superSampleData != null) {
			
			int h = getHeight();
			int w = getWidth();
			int strLength = g.getFontMetrics().stringWidth("00:00");
			int gap = strLength;
			int numMarkersPossible = (w - margin *2 ) / (strLength + gap);
			int totalSeconds = (int)(totalSampleLength / sampleRate);
			int timeMod = ((int)(totalSeconds / numMarkersPossible / displaySecondsStep) + 1) * displaySecondsStep;
			int accumSecTimeDrawn = -1;
			for(int i = 0; i < w - margin; i ++) {
				int pixel = i ;
				if(margin + i  == selectPos) {
					g.setColor(Color.YELLOW);
					g.drawLine(margin + i, 0, margin + i, h);
					int frame = pixel * noOfSamples / samplesPerFrame;
					g.drawString(String.valueOf(frame), margin + i + 1, 15);
				}
				int accumSec = pixel * noOfSamples / sampleRate ;
				
				if(accumSec % (timeMod) == 0 && accumSec > accumSecTimeDrawn) {
					accumSecTimeDrawn = accumSec;
					if(i + margin < w - margin) {
						g.setColor(new Color(136, 166, 166));
						g.drawLine(margin + i, h - 20, margin + i, h - 12);
						int minutes = accumSec / 60;
						int seconds = accumSec % 60;
						String minPref = minutes < 10 ? "0" : "";
						String secPref = seconds < 10 ? "0" : "";
						g.setColor(new Color(136, 200, 200));
						g.drawString(minPref + minutes + ":" + secPref + seconds, margin + i + 1, h - 4);
					}
				}
			}
			
			int divY = (Math.max(Math.abs(superSampleData.getOverallMin()), Math.abs(superSampleData.getOverallMax())) * 2 / (h - margin * 2)) + 1;
			
			int x = margin + 1;
			g.setColor(Color.BLACK);
			for(SuperSample s : superSampleData.getList()) {
				g.drawLine(x, h /2 - s.getMax() / divY, x, h / 2 - s.getMin() / divY);
				if(samplePosition > pos && samplePosition <= pos + s.getNoOfSamples()) {
					g.setColor(Color.WHITE);
					g.drawLine(x, 0, x , h);
				}
				x++;
				pos += s.getNoOfSamples();
			}
		}
	}
	
	private void setInputOutputData() {
		VideoOutputInfo videoOutputInfo = Context.getVideoOutputInfo();
		AudioInputInfo audioInputInfo = Context.getAudioInput().getAudioInputInfo();
		videoFrameRate = videoOutputInfo.getFramesPerSecond();
		sampleRate = (int)audioInputInfo.getAudioFormat().getSampleRate(); // non integer sample rates are rare
		samplesPerFrame = sampleRate / videoFrameRate; // e.g. 44100 / 25 = 1764
	}

	/**
	 * Set the current sample position
	 * @param samplePosition
	 */
	public void setSamplePosition(long samplePosition) {
		this.samplePosition = samplePosition;
	}
	
	

}
