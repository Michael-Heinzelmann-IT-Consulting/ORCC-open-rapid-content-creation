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
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.sound.sampled.AudioFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.TimeLabel;


/**
 * Panel to display input and output information
 * @author Michael Heinzelmann
 */
public class InfoPanel extends JPanel implements Listener {

	private static final long serialVersionUID = 1L;
	
	private JLabel audioFileLabel = new JLabel();
	private JLabel exportFileLabel = new JLabel();
	
	private JLabel sampleRateLabel = new JLabel();
	private JLabel frameSizeLabel = new JLabel();
	private JLabel frameLengthLabel = new JLabel();
	private JLabel channelsLabel = new JLabel();
	private JLabel resolutionLabel = new JLabel();
	private TimeLabel timeLengthLabel = new TimeLabel();

	/**
	 * Sets up components for audio in and export file name
	 */
	public  InfoPanel() {

		setBorder(new LineBorder(Color.WHITE, 5));	
		
		JPanel labelPanel = new JPanel();
		labelPanel.setBackground(Color.GRAY);
		labelPanel.setBorder(new LineBorder(Color.GRAY, 5));
		labelPanel.setLayout(new GridLayout(8, 1, 1, 1));
		JPanel valuePanel = new JPanel();
		valuePanel.setBackground(Color.LIGHT_GRAY);
		valuePanel.setBorder(new LineBorder(Color.LIGHT_GRAY, 5));
		valuePanel.setLayout(new GridLayout(8, 1, 1, 1));
		
		labelPanel.add(new JLabel("audio in:"));
		valuePanel.add(audioFileLabel);
		labelPanel.add(new JLabel("export:"));
		valuePanel.add(exportFileLabel);
		labelPanel.add(new JLabel("sample rate:"));
		valuePanel.add(sampleRateLabel);
		labelPanel.add(new JLabel("frame size:"));
		valuePanel.add(frameSizeLabel);
		labelPanel.add(new JLabel("total samples:"));
		valuePanel.add(frameLengthLabel);
		labelPanel.add(new JLabel("channels:"));
		valuePanel.add(channelsLabel);
		labelPanel.add(new JLabel("resolution:"));
		valuePanel.add(resolutionLabel);
		labelPanel.add(new JLabel("time:"));
		valuePanel.add(timeLengthLabel);
		
		setLayout(new BorderLayout(0, 0));
		add(labelPanel, BorderLayout.WEST);
		add(valuePanel);
		
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

	}

	@Override
	public void contextChanged(PropertyName propertyName) {
		if(PropertyName.AudioInputInfo.equals(propertyName)) {
			AudioInput audioInput = Context.getAudioInput();
			final AudioFormat audioFormat = audioInput.getAudioInputInfo().getAudioFormat();
			sampleRateLabel.setText(String.valueOf(audioFormat.getSampleRate()));
			frameSizeLabel.setText(String.valueOf(audioFormat.getFrameSize()));
			frameLengthLabel.setText(String.valueOf(audioInput.getAudioInputInfo().getFrameLength()));
			channelsLabel.setText(String.valueOf(audioFormat.getChannels()));
			resolutionLabel.setText(String.valueOf((Math.pow(2, audioFormat.getSampleSizeInBits()))));
			timeLengthLabel.update(audioInput.getAudioInputInfo().getFrameLength(), audioFormat.getSampleRate());
		}
		if(PropertyName.AudioInputInfo.equals(propertyName)) {
			audioFileLabel.setText(Context.getAudioInput().getName());
		}
		if(PropertyName.ExportFileName.equals(propertyName)) {
			exportFileLabel.setText(Context.getExportFileName());
		}

	}
	
}
