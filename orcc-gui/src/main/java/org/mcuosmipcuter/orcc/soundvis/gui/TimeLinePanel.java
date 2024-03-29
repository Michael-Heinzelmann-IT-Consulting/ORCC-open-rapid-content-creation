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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.gui.table.CustomTableListener;
import org.mcuosmipcuter.orcc.gui.util.GUIDesignUtil;
import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.model.SuperSample;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.CallBack;
import org.mcuosmipcuter.orcc.soundvis.threads.SuperSampleData;

/**
 * Panel that displays the timeline and the waveform and sets the song position pointer.
 * @author Michael Heinzelmann
 */
public class TimeLinePanel extends JPanel implements CustomTableListener {

	private static final long serialVersionUID = 1L;

	// fixed layout
	final static int MINIMUM_WIDTH = 600;
	private final int margin = 20;
	private final int marginY = 24;
	private final int displaySecondsStep = 5;
	private final static Color selectFrameWidthColor =  new Color(Color.YELLOW.getRed(), Color.YELLOW.getGreen(), Color.YELLOW.getBlue(), 44);
	private final static Color timeMarkerColor = new Color(136, 166, 166);
	private final static Color timeNumbersColor = new Color(136, 200, 200);
	private final static Color selectedCanvasColor = new Color(255, 255, 255, 200);
	private final static Color canvasColor1 = new Color(146, 166, 176, 44);
	private final static Color canvasColor2 = new Color(146, 176, 146, 44);
	
	// flexible layout screen dependent
	int widthToUse;
	int expandedWidth;
	
	int heightToUse = 150;
	//int guiWidth;
	
	// user configuration
	boolean autoZoom = true;
	int samplesToZoom = 100;
	
	// state
	private boolean loading;
	private boolean sampling;
	private int noOfSamplesLoaded;
	
	// calculated data
	private SuperSampleData superSampleData;
	private SuperSampleData superSampleDataAutoZoomed;
	private SuperSampleData superSampleDataFrameZoomed;
	private int noOfSamples;
	private int divY = 1;
	
	// user and play positions
	private int selectPos = margin;
	private long selectFrame;
	private long samplePosition;
	
	// data from input / output
	private int samplesPerFrame = 1;
	private int sampleRate;
	private long totalSampleLength;
	private int videoFrameRate;
	
	private List<SoundCanvasWrapper> currentCanvasList = new ArrayList<SoundCanvasWrapper>();
	
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
				int pixel = selectPos - margin;
				selectFrame = pixel * noOfSamples / samplesPerFrame;
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				int pixel = selectPos - margin;
				final long frame = pixel * noOfSamples / samplesPerFrame;
				if(Context.getAppState() == AppState.READY) {
					Context.setSongPositionPointer(frame);
				}
				selectFrame = frame;
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
				int pixel = selectPos - margin;
				selectFrame = pixel * noOfSamples / samplesPerFrame;
				if(selectPos != oldSelectPos) {
					repaint();
				}
			}
			
		});
		Context.addListener(new Listener() {
			
			@Override
			public void contextChanged(PropertyName propertyName) {
				if(PropertyName.AudioInputInfo.equals(propertyName) 
						|| PropertyName.VideoFrameRate.equals(propertyName)) {
					superSampleDataFrameZoomed = null;
					superSampleDataAutoZoomed = null; // forces reload
					setInputOutputData();
					selectPos = margin;
					samplePosition = 0;
					selectFrame = 0;
					Context.setSongPositionPointer(0);
				}
				if( Context.PropertyName.SoundCanvasList == propertyName ||
					Context.PropertyName.SoundCanvasAdded == propertyName || 
					Context.PropertyName.SoundCanvasRemoved == propertyName) {
						currentCanvasList.clear();
						currentCanvasList.addAll(Context.getSoundCanvasList());
						repaint();
				}
				if(PropertyName.AppState.equals(propertyName)) {
					if(Context.getAppState() == AppState.READY) {
						Context.setSongPositionPointer(selectFrame);
					}
					if(Context.getAppState() == AppState.EXPORTING) {
						Context.setSongPositionPointer(0);
					}
					loading = Context.getAppState() == AppState.LOADING;
				}		
				repaint();
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
		if(loading || sampling) {
			paintLoading(g);
			return;
		}
		long pos = 0;
		if(superSampleData != null) {
			
			
			
			final int h = heightToUse;
			final int w = widthToUse;
			int closed = (int)currentCanvasList.stream().filter(sc -> !sc.isEditorOpen()).count();
			int cB = 3;
			int rem = h - closed * cB;
			int open = currentCanvasList.size() - closed;
			int oB = open > 0 ? rem / open : 0;
			//int b = h / currentCanvasList.size();
			int x = margin + 1;
			g.setColor(Color.WHITE);
			//g.setXORMode(Color.GRAY);

			for(SuperSample s : superSampleData.getList()) {
				g.drawLine(x, h /2 - s.getMax() / divY, x, h / 2 - s.getMin() / divY);
				if(samplePosition >= pos && samplePosition <= pos + s.getNoOfSamples()) {
					g.setColor(Color.LIGHT_GRAY);
				}
				x++;
				pos += s.getNoOfSamples();
			}
			int currentPos = (int)(samplePosition / noOfSamples);
			g.setColor(Color.ORANGE);
			g.drawLine(margin + currentPos, 0, margin + currentPos, heightToUse);
			g.setColor(Color.BLACK);
			g.drawLine(selectPos, 0, selectPos, h);
			//g.drawString(String.valueOf(selectFrame), selectPos + 1, 16);
			//g.setXORMode(Color.GRAY);
			g.setPaintMode();
			
			int y = 0;

			int count = 0;
			long [][] layers = new long[currentCanvasList.size()][3];
			Rectangle selectedCanvas = null;
			
			for(SoundCanvasWrapper scw : currentCanvasList) {
				int b = scw.isEditorOpen() ? oB : cB;
				final long longFrom = scw.getFrameFrom();
				final long longTo = scw.getFrameTo();
				final int from = margin + (int)(longFrom * samplesPerFrame / noOfSamples);
				final int to = longTo == 0 ? w - margin : margin+  (int)(longTo * samplesPerFrame / noOfSamples);
				Color c = (count % 2 == 0) ? canvasColor1  : canvasColor2;
				
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
				delta = 2;//free * 4;// TODO
				g.fillRoundRect(from, y + delta, to - from, b, 16, 16);
				layers[count][0] = longFrom;
				layers[count][1] = longTo;
				layers[count][2] = free;
				g.setColor(Color.YELLOW.brighter());
				g.drawRoundRect(from, y + delta, to - from, b, 16, 16);
				if(scw.isEditorOpen()) {
					DisplayDuration<?>[] fromTos = scw.getFrameFromTos();
					if(fromTos.length > 0) {
						//g.setColor(new Color(delta*100, false));
						long oldFtf = Long.MIN_VALUE;
						int userIdx = 1;
						for(int i = 0; i < fromTos.length; i++) {
							long ftf = fromTos[i].getFrom();

							int subFrom = margin +  (int)fromTos[i].getFrom()* samplesPerFrame / noOfSamples;
							int subTo = margin + (int)(fromTos[i].getTo() +1)* samplesPerFrame / noOfSamples;
							//g.drawRoundRect(subFrom, y + delta, subTo - subFrom, b  + delta, 16, 16);
							if(ftf != oldFtf) {
								String idxStr = "" + (userIdx);
								Font oldFont = g.getFont();
								g.setFont(oldFont.deriveFont((float)Math.min((subTo - subFrom) * 0.4, b * 0.8)));
								g.setColor(Color.GREEN.darker());
								g.setXORMode(Color.GRAY);
								FontMetrics fm = g.getFontMetrics();
								int fontHeight = fm.getAscent() + fm.getDescent();
								g.drawString(idxStr, subFrom + (subTo - subFrom) / 2 - (fm.stringWidth(idxStr)/ 2),y + b/2 - fontHeight / 2 + fm.getAscent());
								g.setFont(oldFont);
								g.setPaintMode();
								userIdx++;
							}
							oldFtf = ftf;
							if(fromTos[i].getOverlapBefore() == 0 && fromTos[i].getOverlapAfter() == 0 && fromTos[i].getDegreesBefore() == 0 && fromTos[i].getDegreesAfter() == 0) {
								g.drawRoundRect(subFrom, y + delta, subTo - subFrom, b  + delta, 16, 16);
							}
							else if(fromTos[i].getDegreesBefore() != 0 || fromTos[i].getDegreesAfter() != 0) {
								int[] shapeDegrees = new int[] {fromTos[i].getDegreesBefore(), fromTos[i].getDegreesAfter()};
						        int ampl =  b  + delta;
								int marginYpx = 2;
						        Color pre = g.getColor();
						        g.setColor(GUIDesignUtil.getEffectBgColor(fromTos[i].getDisplayObject().getClass(), Color.WHITE));
								for(int shape = 0; shape <= 1; shape++) {
									int totalDegrees = Math.abs(shapeDegrees[shape]);
									if(totalDegrees == 0) {
										continue;
									}
							        float pxPerDeg;
	
							        GeneralPath polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
							        float begin;
							        float px;
							        if(shape == 0) {
										px = Math.abs(fromTos[i].getOverlapBefore())* samplesPerFrame / noOfSamples;
								        begin = subFrom + (int)px;
							        }
							        else {
								        px = Math.abs(fromTos[i].getOverlapAfter())* samplesPerFrame / noOfSamples;
								        begin = subTo - (int)px;
							        }
							        pxPerDeg = px / totalDegrees;
							        
							        polygon.moveTo(begin, y + ampl/2 + marginYpx);
							        for ( int id = 1; id <= totalDegrees; id++ ) {
							        	double  rad =  (double)id/ 180d * Math.PI; 
							            double d = Math.sin(rad);
							            int yd =(int) (d * ampl / 2d);
							            float xPx = (shape == 0) ? begin - id * pxPerDeg : begin + id * pxPerDeg;
							        	polygon.lineTo(xPx, y + ampl/2 - yd + marginYpx);
							        };
	
							        ((Graphics2D) g).draw(polygon);
								}
						        
						        g.setColor(pre);
							}
							else {
								int x1 = subFrom + (int)Math.abs(fromTos[i].getOverlapBefore())* samplesPerFrame / noOfSamples;
								int x2 = subTo - (int)Math.abs(fromTos[i].getOverlapAfter())* samplesPerFrame / noOfSamples;

								int[]xPoints;
								int[]yPoints;
								int nPoints;
								if(fromTos[i].getEffectX() != null && fromTos[i].getEffectY() != null) {
									xPoints = fromTos[i].getEffectX();
									yPoints = fromTos[i].getEffectY();
									nPoints = fromTos[i].getEffectX().length;
									for(int j = 0; j < xPoints.length; j++) {
										xPoints[j] =  margin + xPoints[j] *  samplesPerFrame / noOfSamples;
										yPoints[j] =  delta + y + (int)(b * .05) + (int)(yPoints[j] / 100f * (b * .90));
									}
								}
								else if(x1 != 0 && x2 == 0) {
									xPoints = new int[] {subFrom, x1, subTo, subTo, x1};
									yPoints = new int[] {y+b, y , y , y+ b + b/2 + delta, y+b + b/2 + delta};
									nPoints = 5;
								}
								else {
									xPoints = new int[] {subFrom, x1, x2, subTo, x2, x1};
									yPoints = new int[] {y+b/2, y+delta +1, y+delta+1 , y+b/2, y+ b, y+b };
									nPoints = 6;
								}
								g.setColor(GUIDesignUtil.getEffectBgColor(fromTos[i].getDisplayObject().getClass(), Color.WHITE));
								g.drawPolygon(xPoints, yPoints, nPoints);
								//g.drawString(fromTos[i].getDisplayObject().getDisplayKey(), x1 + 6, y + delta + 12);
							}
						}
					}


				}
				if(scw.isSelected()) {
					selectedCanvas = new Rectangle(from, y + delta, to - from, b);
					g.setColor(selectedCanvasColor);
					g.fillRoundRect(selectedCanvas.x, selectedCanvas.y, selectedCanvas.width, selectedCanvas.height, 16, 16);
				}
				
				count++;
				y += b;
			}

			int strLength = g.getFontMetrics().stringWidth("00:00");
			int gap = strLength;
			int numMarkersPossible = (w - margin *2 ) / (strLength + gap);
			int totalSeconds = (int)(totalSampleLength / sampleRate);
			int timeMod = ((int)(totalSeconds / numMarkersPossible / displaySecondsStep) + 1) * displaySecondsStep;
			int accumSecTimeDrawn = -1;

			for(int i = 0; i < w - margin; i ++) {
				
				if(i * noOfSamples / samplesPerFrame == selectFrame) {
					g.setColor(selectFrameWidthColor);
					g.drawLine(margin + i, 0, margin + i, h);
				}
				
				int accumSec = i * noOfSamples / sampleRate ;
				
				if(accumSec % (timeMod) == 0 && accumSec > accumSecTimeDrawn) {
					accumSecTimeDrawn = accumSec;
					if(i + margin < w - margin) {
						g.setColor(timeMarkerColor);
						g.drawLine(margin + i, h - 20, margin + i, h - 12);
						int minutes = accumSec / 60;
						int seconds = accumSec % 60;
						String minPref = minutes < 10 ? "0" : "";
						String secPref = seconds < 10 ? "0" : "";
						g.setColor(timeNumbersColor);
						g.drawString(minPref + minutes + ":" + secPref + seconds, margin + i + 1, h - 4);
					}
				}
			}
			String str = String.valueOf(selectFrame);
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(str, 0, str.length(), g);
			g.setColor(Color.YELLOW);
			g.fillRect(selectPos, 4, (int)bounds.getWidth() +1, (int)bounds.getHeight());
			g.setColor(Color.BLACK);
			g.drawString(str, selectPos + 1, 16);
		
		}
	}
	
	/**
	 * Sets the data for the time line (takes it internally from the {@link Context}) and start an asynchronous sampling
	 */
	public void setInputOutputData() {
		if(Context.getAudioInput() == null) {
			return;
		}
		VideoOutputInfo videoOutputInfo = Context.getVideoOutputInfo();
		AudioInputInfo audioInputInfo = Context.getAudioInput().getAudioInputInfo();
		videoFrameRate = videoOutputInfo.getFramesPerSecond();
		sampleRate = (int)audioInputInfo.getAudioFormat().getSampleRate(); // non integer sample rates are rare
		samplesPerFrame = sampleRate / videoFrameRate; // e.g. 44100 / 25 = 1764
		AudioInput audioInput = Context.getAudioInput();
		totalSampleLength = audioInput.getAudioInputInfo().getFrameLength();
		if(autoZoom) {
			if(expandedWidth == 0) {
				expandedWidth = getWidth();
			}
			widthToUse = expandedWidth;
			noOfSamples = (int)(totalSampleLength / (widthToUse - margin*2)) + 1;
		}
		else {
			noOfSamples = samplesToZoom;
		}
		if(autoZoom && superSampleDataAutoZoomed != null) {
			superSampleData = superSampleDataAutoZoomed;
		}
		else if(!autoZoom && superSampleDataFrameZoomed != null && noOfSamples == noOfSamplesLoaded) {
			superSampleData = superSampleDataFrameZoomed;
		}
		else {
			// make the asynchronous sub sampling
			SubSampleThread superSample = new SubSampleThread(audioInput, noOfSamples, new CallBack() {
				@Override
				public void finishedSampling(SuperSampleData superSampleData) {
					TimeLinePanel.this.superSampleData = superSampleData;
					divY = (Math.max(Math.abs(superSampleData.getOverallMin()), Math.abs(superSampleData.getOverallMax())) * 2 / (heightToUse - marginY * 2)) + 1;
					if(autoZoom) {
						superSampleDataAutoZoomed = superSampleData;
					}
					else {
						superSampleDataFrameZoomed = superSampleData;
					}

					sampling = false;
					TimeLinePanel.this.revalidate();
					TimeLinePanel.this.repaint();
				}
			});
			sampling = true;
			noOfSamplesLoaded = noOfSamples;
			
			superSample.start();
		}
		if(autoZoom) {
			setPreferredSize(new Dimension(MINIMUM_WIDTH, heightToUse));
			setSize(MINIMUM_WIDTH, heightToUse);
		}
		else {
			int widthRequired = (int)(totalSampleLength / (noOfSamples)) + margin * 2;
			setPreferredSize(new Dimension(widthRequired, heightToUse));
			setSize(widthRequired, heightToUse);
			widthToUse = widthRequired;

		}
		selectPos = (int)(selectFrame * samplesPerFrame /  noOfSamples) + 1 + margin;
		repaint();
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

	/**
	 * Sets whether to zoom the wave automatically to the full track length, or not, for this case see {@link #setSamplesToZoom(int)}
	 * @param autoZoom
	 */
	public void setAutoZoom(boolean autoZoom) {
		this.autoZoom = autoZoom;
	}

	/**
	 * Sets the fixed amount samples to zoom if auto zoom is off
	 * @param samplesToZoom the number of samples to use for a sub sample
	 */
	public void setSamplesToZoom(int samplesToZoom) {
		this.samplesToZoom = samplesToZoom;
	}

	@Override
	public long getFrameSelected() {
		return selectFrame;
	}

	/**
	 * Get the selected pixel position (for scrolling)
	 * @return the position
	 */
	public int getSelectPos() {
		return selectPos;
	}
	
	public int getCurrentPosition() {
		return noOfSamples != 0 ? (int)(samplePosition / noOfSamples) : 0;
	}

}
