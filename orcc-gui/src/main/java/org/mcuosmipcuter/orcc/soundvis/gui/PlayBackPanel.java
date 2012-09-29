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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.LineBorder;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.TimeAndRateHelper;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Mixin;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStopFactory;
import org.mcuosmipcuter.orcc.soundvis.Renderer;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.StopActionListener;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.FrameLabel;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.PlayPauseButton;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.TimeLabel;
import org.mcuosmipcuter.orcc.soundvis.threads.PlayThread;



/**
 * Panel that allows play back and provides the necessary GUI elements 
 * and listens to the {@link Context}
 * @author Michael Heinzelmann
 */
public class PlayBackPanel extends JPanel implements Mixin{

	private static final long serialVersionUID = 1L;

	private long sampleLength;
	private float sampleRate;
	private long sampleCount;
	private int samplesPerFrame;
	
	TimeLinePanel timeLine = new TimeLinePanel();
	
	private FrameLabel frameCountlabel = new FrameLabel();
	private TimeLabel timeLabel = new TimeLabel();
	private JLabel stateLabel = new JLabel();
	JProgressBar jProgressBar = new JProgressBar();
	PlayPauseButton playPause;
	
	/**
	 * Sets up a stop, play/pause button and a status label
	 * @param renderer the renderer to work with
	 */
	public PlayBackPanel(final Renderer renderer) {
		setBorder(new LineBorder(Color.WHITE, 5));
		jProgressBar.setMaximum(100);
		jProgressBar.setStringPainted(true);
		frameCountlabel.reset();
		timeLabel.reset();

		final JButton stop = new JButton("[]");

		PlayPauseStopFactory np = new PlayPauseStopFactory() {		
			@Override
			public PlayPauseStop newPlayPauseStop() {
				return new PlayThread(renderer);
			}
		};
		playPause = new PlayPauseButton(np);
		playPause.setEnabled(false);
		stop.addActionListener(new StopActionListener(playPause));
		stop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				playPause.reset();
				long startFrame = Context.getSongPositionPointer();
				frameCountlabel.update(startFrame);
				sampleCount = startFrame * samplesPerFrame;
				updateProgress();
			}
		});
		Context.addListener(new Listener() {
			
			@Override
			public void contextChanged(PropertyName propertyName) {
				if(PropertyName.AppState.equals(propertyName)) {
					AppState appState = Context.getAppState();
					stateLabel.setText("" + appState);
					stop.setEnabled(appState != AppState.EXPORTING);
					if(appState == AppState.READY) {
						playPause.reset();
					}
					playPause.setEnabled(appState != AppState.EXPORTING);
				}
				if(PropertyName.AudioInputInfo.equals(propertyName)) {
					playPause.setEnabled(Context.getAudioInput() != null);
				}
				if(PropertyName.AudioInputInfo.equals(propertyName) || PropertyName.VideoFrameRate.equals(propertyName) ) {
					samplesPerFrame = TimeAndRateHelper.getSamplesPerFrame(Context.getAudioInput().getAudioInputInfo(), Context.getVideoOutputInfo());
				}
			}
		});
		GridLayout gl = new GridLayout(2, 3, 5, 10);		
		
		JPanel commands = new JPanel();
		commands.setBorder(new LineBorder(Color.WHITE, 2));
		commands.setLayout(gl);
		commands.add(stop);
		commands.add(playPause);
		commands.add(stateLabel);
		
		commands.add(frameCountlabel);
		commands.add(timeLabel);
		commands.add(jProgressBar);
		commands.setPreferredSize(new Dimension(240, 70));
		
		timeLine.setPreferredSize(new Dimension(600, 150));
		setLayout(new BorderLayout());
		add(commands, BorderLayout.WEST);
		add(timeLine, BorderLayout.CENTER);

	}


	@Override
	public void newFrame(long frameCount, boolean sendPost) {
		frameCountlabel.update(frameCount);
		updateProgress();
	}
	@Override
	public boolean nextSample(int[] amplitudes, byte[] rawData, long sampleCount) {
		this.sampleCount = sampleCount;
		if(sampleCount >= sampleLength) {
			updateProgress();
			playPause.reset();
		}
		return true; // always continue
	}
	@Override
	public void start(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo) {
		sampleLength = audioInputInfo.getFrameLength();
		sampleRate = audioInputInfo.getAudioFormat().getSampleRate();
	}
	private void updateProgress() {
		double d = (double)sampleCount / (double)sampleLength;
		jProgressBar.setValue((int)(d * 100));
		timeLabel.update(sampleCount, sampleRate);
		timeLine.setSamplePosition(sampleCount);
		timeLine.repaint();
	}


	public synchronized TimeLinePanel getTimeLine() {
		return timeLine;
	}

	
}

