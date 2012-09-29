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
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.gui.table.CustomTableListener;
import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.CallBack;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.SuperSample;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.SuperSampleData;

/**
 * Panel that displays the timeline and the waveform and sets the song position pointer.
 * @author Michael Heinzelmann
 */
public class TimeLinePanel extends JPanel implements CustomTableListener {

	private static final long serialVersionUID = 1L;

	// fixed layout
	private final int margin = 20;
	private final int marginY = 24;
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
	
	List<SoundCanvasWrapper> currentCanvasList = new ArrayList<SoundCanvasWrapper>();
//	private Map<Long, Set<String>> frameMarksFrom = new HashMap<Long, Set<String>>();
//	private Map<Long, Set<String>> frameMarksTo = new HashMap<Long, Set<String>>();
//	
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
		
		Context.addListener(new Context.Listener() {
			@Override
			public void contextChanged(PropertyName propertyName) {
				
				if( Context.PropertyName.SoundCanvasList == propertyName ||
					Context.PropertyName.SoundCanvasAdded == propertyName || 
					Context.PropertyName.SoundCanvasRemoved == propertyName) {
					currentCanvasList.clear();
					currentCanvasList.addAll(Context.getSoundCanvasList());
					repaint();
				}
				
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
			
			int divY = (Math.max(Math.abs(superSampleData.getOverallMin()), Math.abs(superSampleData.getOverallMax())) * 2 / (h - marginY * 2)) + 1;
			int x = margin + 1;
			g.setColor(Color.BLACK);
			//g.setColor(new Color(0, 0, 0, 128));
			for(SuperSample s : superSampleData.getList()) {
				g.drawLine(x, h /2 - s.getMax() / divY, x, h / 2 - s.getMin() / divY);
				if(samplePosition > pos && samplePosition <= pos + s.getNoOfSamples()) {
					g.setColor(Color.WHITE);
					//g.setColor(new Color(255, 255, 255, 128));
					g.drawLine(x, 0, x , h);
				}
				x++;
				pos += s.getNoOfSamples();
			}
			
			
			int y = 0;
			int b = h ;
			int count = 0;
			long [][] layers = new long[currentCanvasList.size()][3];
			Rectangle selectedCanvas = null;
			
			for(SoundCanvasWrapper scw : currentCanvasList) {
				final long longFrom = scw.getFrameFrom();
				final long longTo = scw.getFrameTo();
				final int from = margin + (int)(longFrom * samplesPerFrame / noOfSamples);
				final int to = longTo == 0 ? w - margin : margin+  (int)(longTo * samplesPerFrame / noOfSamples);
				Color c = (count % 2 == 0) ? new Color(146, 166, 176, 44) : new Color(146, 176, 146, 44);
				
				g.setColor(c);
				int delta = 0;

				int free = count;
				for(int i = count - 1; i >= 0; i--) {
					if((longFrom >= layers[i][1] && layers[i][1] !=0) || (longTo != 0 && longTo <= layers[i][0])) {
						free = i;
					}
					else {		
						free = (int)layers[i][2] + 1;
						break;
					}

				}
				delta = free * 6;// TODO
				g.fillRoundRect(from, y + delta, to - from, b, 16, 16);
				layers[count][0] = longFrom;
				layers[count][1] = longTo;
				layers[count][2] = free;
				g.setColor(Color.GRAY);
				g.drawRoundRect(from, y + delta, to - from, b, 16, 16);
				if(scw.isSelected()) {
					selectedCanvas = new Rectangle(from, y + delta, to - from, b);
				}
				
//				int len = g.getFontMetrics().stringWidth(scw.getDisplayName());
//				int textStart = 4;
//				if(len < (to - from)) {
//					textStart = ((to - from) - len) / 2;
//				}
//				g.setColor(new Color(200, 200, 200, 128));
				//g.drawString(scw.getDisplayName(), from + textStart, y + (free * 12) + 16);
				count++;
			}
			if(selectedCanvas != null) {
				Color c = new Color(255, 255, 255, 200);
				g.setColor(c);
				g.fillRoundRect(selectedCanvas.x, selectedCanvas.y, selectedCanvas.width, selectedCanvas.height, 16, 16);
			}
			
			int strLength = g.getFontMetrics().stringWidth("00:00");
			int gap = strLength;
			int numMarkersPossible = (w - margin *2 ) / (strLength + gap);
			int totalSeconds = (int)(totalSampleLength / sampleRate);
			int timeMod = ((int)(totalSeconds / numMarkersPossible / displaySecondsStep) + 1) * displaySecondsStep;
			int accumSecTimeDrawn = -1;
			for(int i = 0; i < w - margin; i ++) {
				int pixel = i ;
				Long frame = new Long(pixel * noOfSamples / samplesPerFrame);

				if(margin + i  == selectPos) {
					g.setColor(Color.YELLOW);
					g.drawLine(margin + i, 0, margin + i, h);
					g.drawString(String.valueOf(frame), margin + i + 1, h/2);
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
			
//			int divY = (Math.max(Math.abs(superSampleData.getOverallMin()), Math.abs(superSampleData.getOverallMax())) * 2 / (h - margin * 2)) + 1;
			
//			int x = margin + 1;
//			g.setColor(Color.BLACK);
//			//g.setColor(new Color(0, 0, 0, 128));
//			for(SuperSample s : superSampleData.getList()) {
//				g.drawLine(x, h /2 - s.getMax() / divY, x, h / 2 - s.getMin() / divY);
//				if(samplePosition > pos && samplePosition <= pos + s.getNoOfSamples()) {
//					g.setColor(Color.WHITE);
//					//g.setColor(new Color(255, 255, 255, 128));
//					g.drawLine(x, 0, x , h);
//				}
//				x++;
//				pos += s.getNoOfSamples();
//			}
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

	@Override
	public void frameSet() {
		repaint();
	}

	@Override
	public void rowSelected(boolean selected) {
		repaint();
	}
	
	

}
