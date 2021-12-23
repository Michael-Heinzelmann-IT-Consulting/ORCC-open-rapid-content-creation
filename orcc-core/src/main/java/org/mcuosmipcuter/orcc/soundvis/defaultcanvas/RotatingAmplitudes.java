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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.ExtendedFrameHistory;
import org.mcuosmipcuter.orcc.api.soundvis.InputEnabling;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyGroup;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.AmplitudeHelper;
import org.mcuosmipcuter.orcc.soundvis.InputController;
import org.mcuosmipcuter.orcc.soundvis.effects.MovingAverage;

/**
 * @author Michael Heinzelmann
 *
 */
public class RotatingAmplitudes extends InputController implements SoundCanvas, ExtendedFrameHistory {
	
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
	@UserProperty(description="degrees initial", unit = Unit.DEGREES)
	private int initialDegrees = 0;
	
	///// shift
	@SuppressWarnings("unused") // used by reflection
	private PropertyGroup shift = new PropertyGroup("shiftX", "shiftY");
	@UserProperty(description="x distance from center", unit = Unit.PIXEL)
	int shiftX = 0;
	@UserProperty(description="y distance from center", unit = Unit.PIXEL)
	int shiftY = 0;
	
	@LimitedIntProperty(description="limits for zoom", minimum=1)
	@UserProperty(description="amp zoom in %", unit = Unit.PERCENT_OBJECT)
	int ampZoom = 100;
	
	@UserProperty(description="mode for amplitude calculation")
	private AMP_MODE ampMode = AMP_MODE.UNSIGNED;
		
	@ChangesIcon
	@UserProperty(description="mode for drawing")
	private DRAW_MODE drawMode = DRAW_MODE.LINE;
	
	///// min / max
	@SuppressWarnings("unused") // used by reflection
	private PropertyGroup minMax = new PropertyGroup("minRadius", "drawMin", "maxRadius", "drawMax");
	@UserProperty(description="minimum radius in pixel", unit = Unit.PIXEL)
	@LimitedIntProperty(minimum = 0, description = "minimum 0")
	private int minRadius = 0;
	@UserProperty(description="draw if below minimum")
	private boolean drawMin = true;
	@UserProperty(description="maximum radius in pixel", unit = Unit.PIXEL)
	@NumberMeaning(numbers = 0, meanings = "off")
	@LimitedIntProperty(minimum = 0, description = "minimum 0")
	private int maxRadius = 0;
	@UserProperty(description="draw if above maximum")
	private boolean drawMax = true;
	
	
	@ChangesIcon
	@UserProperty(description="size of line", unit = Unit.PIXEL)
	@LimitedIntProperty(minimum = 1, description = "minimum 1")
	private int lineSize = 1;
	private int prevLineSize = 0; // reduce object creation
	
	@ChangesIcon
	@LimitedIntProperty(description="limits for dot size", minimum=2, stepSize= 2)
	@UserProperty(description="dot size for dot mode for drawing", unit = Unit.PIXEL)
	private int dotSize = 2;
	
	@ChangesIcon
	@UserProperty(description="first polygon from center")
	private boolean startFromCenter = false;
	
	@NestedProperty(description = "smoothening using moving average")
	MovingAverage movingAverage = new MovingAverage(1000);
	
	private int centerX;
	private int centerY;

	private float amplitudeDivisor;
	private float amplitudeMultiplicator;
	private AmplitudeHelper amplitude;	
	int degrees;
	Deque<int[]> deque = new ArrayDeque<int[]>();
	int max;
	int samplesPerFrame;
	long sampleCount;
	BasicStroke stroke = new BasicStroke(1);
	
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
		int modul;
		if(degreesPerFrame != 0) {
			modul =  samplesPerFrame / degreesPerFrame;
		}
		else {
			modul = 0;
		}
		
		if(modul == 0 || sampleCount % modul == 0) {
			int drawAmp = max;

			int[] latestRemoved = null;
			if(deque.size() == size) {
				latestRemoved = deque.removeFirst();
			}
			if(deque.size() > size) {
				while(deque.size() >= size) {
					latestRemoved = deque.removeFirst();
				}
			}
			int[] p;
			if(latestRemoved != null) {
				latestRemoved[0] = drawAmp;
				latestRemoved[1] = degrees;
				p = latestRemoved;
			}
			else {
				p = new int[] {drawAmp, degrees};
			}
			deque.addLast(p);
			max = 0;
			if(degreesPerFrame > 0) {
				this.degrees++;
			}
			else if(degreesPerFrame < 0){
				this.degrees--;
			}
			else {
				this.degrees = initialDegrees;
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
		Stroke origStroke = graphics2D.getStroke();
		if (prevLineSize != lineSize) {
			stroke = new BasicStroke(lineSize);
		}
		prevLineSize = lineSize;
		if (lineSize > 0) {		
			graphics2D.setStroke(stroke);
		}
		
		for(int[] arr : deque) {
			int drawAmp = arr[0];
			if(ampZoom != 100) {
				double scale = (double)ampZoom / 100;
				drawAmp = (int)((double)arr[0] * scale);
			}
			boolean draw = true;
			if(drawAmp < minRadius) {
				drawAmp = minRadius;
				draw = drawMin;
			}
			if(maxRadius != 0 && drawAmp > maxRadius) {
				drawAmp = maxRadius;
				draw = drawMax;
			}
			
			int x = centerX  + (int)(drawAmp * Math.cos(arr[1] * (Math.PI / 180)));
			int y = centerY + (int)(drawAmp * Math.sin(arr[1] * (Math.PI / 180)));

			if(draw && (drawMode == DRAW_MODE.LINE || drawMode == DRAW_MODE.DOT_LINE)) {
				graphics2D.drawLine(centerX + shiftX, centerY + shiftY, x + shiftX, y + shiftY);
			}
			if(draw && (drawMode == DRAW_MODE.DOT || drawMode == DRAW_MODE.DOT_LINE)) {
				graphics2D.fillOval(x + shiftX - dotSize / 2, y + shiftY - dotSize / 2, dotSize, dotSize);
			}
			if(drawMode == DRAW_MODE.POLY_LINE) {
				if(draw &&(! begin ||startFromCenter)) {
					graphics2D.drawLine(prevX + shiftX, prevY + shiftY, x + shiftX, y + shiftY);
				}
				prevX = x + shiftX;
				prevY = y + shiftY;
			}
			begin = false;
		}
		graphics2D.setStroke(origStroke);
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
		degrees = initialDegrees;
		deque.clear();
		max = 0;
	}

	@Override
	public void postFrame() {
		
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics2D) {
		graphics2D.setColor(foreGround);
		if (lineSize > 1) {
			graphics2D.setStroke(new BasicStroke(Math.max(1, lineSize / 5)));
		}

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
		return degreesPerFrame != 0 ? Math.abs(size / degreesPerFrame) : 0;
	}

	@Override
	protected void doFieldEnablings(Map<String, InputEnabling> fieldEnablings) {
		fieldEnablings.get("lineSize").enableInput(drawMode != DRAW_MODE.DOT);
		fieldEnablings.get("dotSize").enableInput(drawMode == DRAW_MODE.DOT || drawMode == DRAW_MODE.DOT_LINE);
		fieldEnablings.get("startFromCenter").enableInput(drawMode == DRAW_MODE.POLY_LINE);
		fieldEnablings.get("drawMin").enableInput(minRadius > 0);
		fieldEnablings.get("drawMax").enableInput(maxRadius > 0);
	}

}
