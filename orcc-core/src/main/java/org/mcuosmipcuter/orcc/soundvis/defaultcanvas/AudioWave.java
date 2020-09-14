/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2020 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.model.SuperSample;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread;
import org.mcuosmipcuter.orcc.soundvis.threads.SubSampleThread.CallBack;
import org.mcuosmipcuter.orcc.soundvis.threads.SuperSampleData;

/**
 * @author Michael Heinzelmann
 *
 */
public class AudioWave implements SoundCanvas {
	
	public static enum AMP_VALUES{
		PEAK, AVERAGE,
	}

	private String loadedAudioName;
	private int loadedWidth;
	private SuperSampleData superSampleData;
	private int videoWidth;
	private int videoHeight;
	int noOfSamples;
	int samplesPerFrame;
	
	@UserProperty(description = "color of the already played area")
	private Color colorPlayed = Color.BLACK;
	
	@UserProperty(description = "color of the area yet to play")
	private Color colorToPlay = Color.GRAY;
	

	@UserProperty(description="size amplitude")
	@LimitedIntProperty(minimum = 1, description = "not smaller than 0")
	private int amplitudeSize = 100;
	
	@UserProperty(description="size amplitude")
	@LimitedIntProperty(minimum = 1, description = "not smaller than 0")
	private int scaleWidth = 100;
	
	@UserProperty(description="value of amplitude")
	private AMP_VALUES ampValue = AMP_VALUES.PEAK;

	@Override
	public void newFrame(long frameCount, Graphics2D graphics) {
		
		if(superSampleData != null && videoHeight != 0) {
			AffineTransform save = graphics.getTransform();
			AffineTransform atSc = new AffineTransform();
			double sw = scaleWidth / 100d;
			atSc.translate((videoWidth -  videoWidth*sw)/2, 1);
			atSc.scale(sw, 1);

			int  marginVertical = (int)((videoHeight  ) -  videoHeight *((float)amplitudeSize / 100f));

			int divY = (Math.max(Math.abs(superSampleData.getOverallMin()), Math.abs(superSampleData.getOverallMax())) * 2 / (videoHeight - marginVertical / 1) + 1);
			graphics.setColor(colorPlayed);
			int center = videoHeight / 2;
			int x = 1;
			graphics.transform(atSc);
			for (SuperSample susa : superSampleData.getList()) {
				if(x * noOfSamples > frameCount * samplesPerFrame) {
					graphics.setColor(colorToPlay);
				}
				int up = ampValue == AMP_VALUES.PEAK ? susa.getMax() : susa.getAvgUp();
				int down = ampValue == AMP_VALUES.PEAK ? susa.getMin() : susa.getAvgDown();
				graphics.drawLine(x, center - up / divY, x, center - down / divY);
//				graphics.setColor(Color.YELLOW);
//				graphics.drawLine(x, center - susa.getAvgUp()/ divY, x, center - susa.getMax() / divY);
//				graphics.drawLine(x, center - susa.getAvgDown() / divY, x, center - susa.getMin() / divY);
				x++;
			}
			
			graphics.setTransform(save);
		}
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo) {
		this.videoWidth = videoOutputInfo.getWidth();
		this.videoHeight = videoOutputInfo.getHeight();
		AudioInput ai = Context.getAudioInput();
		final String inputName = ai.getName();
		final int width = videoOutputInfo.getWidth();
		long totalSampleLength = ai.getAudioInputInfo().getFrameLength();
		noOfSamples = (int)(totalSampleLength / width ) + 1;
		samplesPerFrame =  (int)audioInputInfo.getAudioFormat().getSampleRate() / videoOutputInfo.getFramesPerSecond();
		if(width != loadedWidth || inputName.equals(loadedAudioName)) {
			SubSampleThread subSampleThread = new SubSampleThread(ai, noOfSamples, new CallBack() {
				@Override
				public void finishedSampling(SuperSampleData superSampleData) {
					AudioWave.this.superSampleData = superSampleData;
					AudioWave.this.loadedAudioName = loadedAudioName;
					AudioWave.this.loadedWidth = loadedWidth;
				}
			});
			subSampleThread.start();
		}
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		// TODO Auto-generated method stub

	}

}
