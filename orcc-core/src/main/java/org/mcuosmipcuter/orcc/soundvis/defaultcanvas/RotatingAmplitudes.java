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
package org.mcuosmipcuter.orcc.soundvis.defaultcanvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.ExtendedFrameHistory;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
import org.mcuosmipcuter.orcc.soundvis.effects.MovingAverage;

/**
 * @author Michael Heinzelmann
 *
 */
public class RotatingAmplitudes implements SoundCanvas, ExtendedFrameHistory {
	
	public static enum DRAW_MODE {
		LINE, DOT, DOT_LINE, POLY_LINE
	}
	
	public static enum AMP_MODE {
		SIGNED, UNSIGNED
	}
	@ChangesIcon
	@UserProperty(description="degrees per frame speed", unit = Unit.DEGREES_PER_FRAME)
	private int degreesPerFrame = 30;
	@ChangesIcon
	@UserProperty(description="foreground color")
	private Color foreGround = Color.BLACK;
	@LimitedIntProperty(description="size must be greater zero", minimum=1)
	@UserProperty(description="frames to keep")
	int size = 360;
	@UserProperty(description="x distance from center", unit = Unit.PIXEL)
	int shiftX = 0;
	@UserProperty(description="y distance from center", unit = Unit.PIXEL)
	int shiftY = 0;
	
	@LimitedIntProperty(description="limits for zoom", minimum=1)
	@UserProperty(description="amp zoom in %", unit = Unit.PERCENT_OBJECT)
	int ampZoom = 100;
	
	@ChangesIcon
	@UserProperty(description="first polygon from center")
	boolean startFromCenter = false;
	
	@ChangesIcon
	@UserProperty(description="mode for drawing")
	private DRAW_MODE drawMode = DRAW_MODE.LINE;
	
	@UserProperty(description="mode for amplitude calculation")
	private AMP_MODE ampMode = AMP_MODE.UNSIGNED;
	
	@ChangesIcon
	@LimitedIntProperty(description="limits for dot size", minimum=2, stepSize= 2)
	@UserProperty(description="dot size for dot mode for drawing", unit = Unit.PIXEL)
	private int dotSize = 2;
	
	@NestedProperty(description = "smoothening using moving average")
	MovingAverage movingAverage = new MovingAverage(1000);
	
	private int centerX;
	private int centerY;

	private float amplitudeDivisor;
	private float amplitudeMultiplicator;
	private AmplitudeHelper amplitude;	
	long degrees;
	Deque<Point> deque = new ArrayDeque<Point>();
	int max;
	int samplesPerFrame;
	long sampleCount;
	
	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#nextSample(int[])
	 */
	@Override
	public void nextSample(int[] amplitudes) {

		int mono = amplitude.getSignedMono(amplitudes);
		int amp = amplitudeDivisor > 1 ? (int)(mono / amplitudeDivisor) : (int)(mono * amplitudeMultiplicator);
		amp = movingAverage.average(amp);
		if(Math.abs(amp) > Math.abs(max)) {
			max = ampMode == AMP_MODE.UNSIGNED ?  Math.abs(amp) : amp;
		}
		if(degreesPerFrame == 0) {
			return; // do nothing
		}
		int modul =  samplesPerFrame / degreesPerFrame;
		
		if(modul == 0 || sampleCount % modul == 0) {
			int drawAmp = max;
			if(ampZoom != 100) {
				double scale = (double)ampZoom / 100;
				drawAmp = (int)((double)max * scale);
			}
			int x = centerX  + (int)(drawAmp * Math.cos(degrees * (Math.PI / 180)));
			int y = centerY + (int)(drawAmp * Math.sin(degrees * (Math.PI / 180)));
			Point latestRemoved = null;
			if(deque.size() == size) {
				latestRemoved = deque.removeFirst();
			}
			if(deque.size() > size) {
				while(deque.size() >= size) {
					latestRemoved = deque.removeFirst();
				}
			}
			Point p;
			if(latestRemoved != null) {
				latestRemoved.x = x;
				latestRemoved.y = y;
				p = latestRemoved;
			}
			else {
				p = new Point(x, y);
			}
			deque.addLast(p);
			max = 0;
			if(degreesPerFrame > 0) {
				this.degrees++;
			}
			else {
				this.degrees--;
			}
			
		}
		sampleCount++;
		
	}

	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#newFrame(long)
	 */
	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {
		
		graphics2D.setColor(foreGround);
		int prevX = centerX + shiftX;
		int prevY = centerY + shiftY;
		boolean begin = true;
		
		for(Point p : deque) {
			if(drawMode == DRAW_MODE.LINE || drawMode == DRAW_MODE.DOT_LINE) {
				graphics2D.drawLine(centerX + shiftX, centerY + shiftY, p.x + shiftX, p.y + shiftY);
			}
			if(drawMode == DRAW_MODE.DOT || drawMode == DRAW_MODE.DOT_LINE) {
				graphics2D.fillOval(p.x + shiftX - dotSize / 2, p.y + shiftY - dotSize / 2, dotSize, dotSize);
			}
			if(drawMode == DRAW_MODE.POLY_LINE) {
				if(! begin ||startFromCenter) {
					graphics2D.drawLine(prevX + shiftX, prevY + shiftY, p.x + shiftX, p.y + shiftY);
				}
				prevX = p.x + shiftX;
				prevY = p.y + shiftY;
			}
			begin = false;
		}
		
	}

	/* (non-Javadoc)
	 * @see org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas#prepare(org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo, org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo, java.awt.Graphics2D, org.mcuosmipcuter.orcc.api.soundvis.CanvasBackGround)
	 */
	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		int frameRate = videoOutputInfo.getFramesPerSecond();
		int sampleRate = (int)audioInputInfo.getAudioFormat().getSampleRate(); // non integer sample rates are rare
		samplesPerFrame = sampleRate / frameRate; // e.g. 44100 / 25 = 1764
		centerX = videoOutputInfo.getWidth() / 2;
		centerY = videoOutputInfo.getHeight() / 2;

		amplitude = new AmplitudeHelper(audioInputInfo);
		amplitudeDivisor = (amplitude.getAmplitudeRange() / videoOutputInfo.getHeight());
		if(amplitudeDivisor < 1){
			amplitudeMultiplicator = videoOutputInfo.getHeight() / amplitude.getAmplitudeRange();
		}
		degrees = 0;
		deque.clear();
		max = 0;
	}

	@Override
	public void postFrame() {
		
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics2D) {
		graphics2D.setColor(foreGround);

		boolean begin = true;
		int localCenterX = width / 2;
		int localCenterY = height / 2;
		int[] drawAmps = new int[] { height /2, height / 4, height / 2, height / 3, 0}; 
		Point[] localQueue = new Point[drawAmps.length];
		long localDegrees = 0;
		for(int i = 0; i < localQueue.length; i++) {
			int x = localCenterX + (int)(drawAmps[i] * Math.cos(localDegrees * (Math.PI / 180)));
			int y = localCenterY + (int)(drawAmps[i] * Math.sin(localDegrees * (Math.PI / 180)));
			localQueue[i] = new Point(x, y);
			localDegrees += degreesPerFrame;
		}
		int prevX = localCenterX;
		int prevY = localCenterY;
		for(Point p : localQueue) {
			if(drawMode == DRAW_MODE.LINE || drawMode == DRAW_MODE.DOT_LINE) {
				graphics2D.drawLine(localCenterX, localCenterY, p.x, p.y);
			}
			if(drawMode == DRAW_MODE.DOT || drawMode == DRAW_MODE.DOT_LINE) {
				int localDotSize = dotSize < height / 2 ? dotSize : height / 2;
				graphics2D.fillOval(p.x  - localDotSize / 2, p.y  - localDotSize / 2, localDotSize, localDotSize);
			}
			if(drawMode == DRAW_MODE.POLY_LINE) {
				if(! begin ||startFromCenter) {
					graphics2D.drawLine(prevX, prevY, p.x, p.y);
				}
				prevX = p.x;
				prevY = p.y;
			}
			begin = false;
		}
	}

	@Override
	public int getCurrentHistoryFrameSize() {
		// depends on the amount of history we are keeping, a big size and a slow degree speed need a big pre-run
		return degreesPerFrame != 0 ? size / degreesPerFrame : 0;
	}

}
