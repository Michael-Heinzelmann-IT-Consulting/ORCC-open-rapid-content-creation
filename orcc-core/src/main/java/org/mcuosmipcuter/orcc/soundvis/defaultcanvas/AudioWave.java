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
import java.lang.reflect.Field;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyListener;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.TimedChange;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;
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
public class AudioWave implements SoundCanvas, PropertyListener {
	
	public static enum AMP_VALUES{
		PEAK, AVERAGE,
	}

	private String loadedAudioName;
	private int loadedWidth;
	@TimedChange
	private SuperSampleData superSampleData;
	private SuperSampleData superSampleDataToUse;
	private int videoWidth;
	private int videoHeight;
	int noOfSamples;
	int samplesPerFrame;
	private DimensionHelper dimensionHelper;
	
	@ChangesIcon
	@UserProperty(description = "color of the already played area")
	private Color colorPlayed = Color.BLACK;
	
	@ChangesIcon
	@UserProperty(description = "color of the area yet to play")
	private Color colorToPlay = Color.GRAY;
	

	@UserProperty(description="size amplitude", unit = Unit.PERCENT_VIDEO)
	@LimitedIntProperty(minimum = 1, description = "not smaller than 1")
	private int amplitudeSize = 100;
	
	@UserProperty(description="size wave", unit = Unit.PERCENT_VIDEO)
	@LimitedIntProperty(minimum = 1, description = "not smaller than 0")
	private int scaleWidth = 100;
	
	@UserProperty(description="center Y axis in %", unit = Unit.PERCENT_VIDEO)
	private int centerY = 50;
	
	@UserProperty(description="value of amplitude")
	private AMP_VALUES ampValue = AMP_VALUES.PEAK;
	
	@UserProperty(description="size amplitude", unit = Unit.TIMES)
	@NumberMeaning(numbers = 1, meanings = "none")
	@LimitedIntProperty(minimum = 1, description = "not smaller than 1")
	private int reduction = 1;

	@Override
	public void newFrame(long frameCount, Graphics2D graphics) {
		
		if(superSampleDataToUse != null && videoHeight != 0) {
			AffineTransform save = graphics.getTransform();
			AffineTransform atSc = new AffineTransform();
			double sw = scaleWidth / 100d;
			atSc.translate((videoWidth -  videoWidth*sw)/2, 1);
			atSc.scale(sw, 1);

			int  marginVertical = (int)((videoHeight  ) -  videoHeight *((float)amplitudeSize / 100f));

			int divY = (Math.max(Math.abs(superSampleDataToUse.getOverallMin()), Math.abs(superSampleDataToUse.getOverallMax())) * 2 / (videoHeight - marginVertical / 1) + 1);
			graphics.setColor(colorPlayed);
			int center = dimensionHelper.realY(centerY);
			int x = 1;
			graphics.transform(atSc);
			for (SuperSample susa : superSampleDataToUse.getList()) {
				if(x * noOfSamples > frameCount * samplesPerFrame) {
					graphics.setColor(colorToPlay);
				}
				int up = ampValue == AMP_VALUES.PEAK ? susa.getMax() : susa.getAvgUp();
				int down = ampValue == AMP_VALUES.PEAK ? susa.getMin() : susa.getAvgDown();
				for(int r = 0; r < reduction; r++) {
					graphics.drawLine(x + r, center - up / divY, x + r, center - down / divY);
				}
//				graphics.setColor(Color.YELLOW);
//				graphics.drawLine(x, center - susa.getAvgUp()/ divY, x, center - susa.getMax() / divY);
//				graphics.drawLine(x, center - susa.getAvgDown() / divY, x, center - susa.getMin() / divY);
				x+= reduction;
			}
			
			graphics.setTransform(save);
		}
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo) {
		this.videoWidth = videoOutputInfo.getWidth();
		this.videoHeight = videoOutputInfo.getHeight();
		this.dimensionHelper = new DimensionHelper(videoOutputInfo);
		AudioInput ai = Context.getAudioInput();
		final String inputName = ai.getName();
		final int width = videoOutputInfo.getWidth();
		long totalSampleLength = ai.getAudioInputInfo().getFrameLength();
		noOfSamples = (int)(totalSampleLength / width ) + 1;
		samplesPerFrame =  (int)audioInputInfo.getAudioFormat().getSampleRate() / videoOutputInfo.getFramesPerSecond();
		if(width != loadedWidth || !inputName.equals(loadedAudioName)) {
			SubSampleThread subSampleThread = new SubSampleThread(ai, noOfSamples, new CallBack() {
				@Override
				public void finishedSampling(SuperSampleData superSampleData) {
					AudioWave.this.superSampleData = superSampleData;
					AudioWave.this.superSampleDataToUse = superSampleData.reduce(reduction);
					AudioWave.this.loadedAudioName = inputName;
					AudioWave.this.loadedWidth = width;
					Context.updateUI(AudioWave.this);
				}
			});
			subSampleThread.start();
		}
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		if(superSampleData != null) {
			int factor = videoWidth / width;
			SuperSampleData local = superSampleData.reduce(factor);
			int x = 1;
			int center = height / 2;
			int divY = (Math.max(Math.abs(local.getOverallMin()), Math.abs(local.getOverallMax())) * 2 / height);
			graphics.setColor(colorPlayed);
			for (SuperSample susa : local.getList()) {
				graphics.drawLine(x , center - susa.getMax() / divY, x, center - susa.getMin() / divY);
				if(x > width / 2) {
					graphics.setColor(colorToPlay);
				}
				x++;
			}
		}
	}

	@Override
	public void propertyWritten(Field field) {
		if("reduction".equals(field.getName())) {
			superSampleDataToUse = superSampleData.reduce(reduction);
		}
	}

}
