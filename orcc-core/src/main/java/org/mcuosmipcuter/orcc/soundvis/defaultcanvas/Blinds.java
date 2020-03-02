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
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.LimitedIntProperty;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;
import org.mcuosmipcuter.orcc.soundvis.effects.Fader;
import org.mcuosmipcuter.orcc.soundvis.effects.Positioner;
import org.mcuosmipcuter.orcc.soundvis.effects.Repeater;
import org.mcuosmipcuter.orcc.soundvis.effects.Scaler;

/**
 * Displays a solid color
 * 
 * @author Michael Heinzelmann
 */
public class Blinds implements SoundCanvas {

	public static enum BLADE_SHAPE {
		RECTANGLE, ELLIPSE,
	}
	public static enum OPEN_CLOSE {
		OPEN_IN_CLOSE_OUT, CLOSE_IN_OPEN_OUT,
	}
	
	@NestedProperty(description = "x and y position")
	Positioner positioner = new Positioner();

	@NestedProperty(description = "scale in and out")
	Scaler scalerOutline = new Scaler();
	
	@NestedProperty(description = "scale in and out")
	Scaler scalerBlinds = new Scaler();

	@NestedProperty(description = "fading in and out")
	private Fader fader = new Fader();
	
	@NestedProperty(description = "fading in and out")
	private Repeater repeater = new Repeater();
	
//	@LimitedIntProperty(minimum=1, description="number cannot be lower than 1")
//	@UserProperty(description="number of repeat")
//	int repeat = 1;
//	@LimitedIntProperty(minimum=2, description="number cannot be lower than 2")
//	@UserProperty(description="number of repeat")
//	int repeatDurationFrames = 2;

	@LimitedIntProperty(minimum=0, description="number cannot be lower than 0")
	@UserProperty(description="number of blinds horizontal")
	private int numberHorizontal = 10;
	@LimitedIntProperty(minimum=0, description="number cannot be lower than 0")
	@UserProperty(description="number of blinds vertical")
	private int numberVertical = 10;
	
	@UserProperty(description = "color of the horizontal blades")
	private Color colorHorizontal = Color.BLACK;
	@UserProperty(description = "color of the vertical blades")
	private Color colorVertical = Color.BLACK;
	
	@UserProperty(description = "shape of the horizontal blades")
	private BLADE_SHAPE bladeShapeHorizontal = BLADE_SHAPE.RECTANGLE;
	@UserProperty(description = "shape of the vertical blades")
	private BLADE_SHAPE bladeShapeVertical = BLADE_SHAPE.RECTANGLE;
	
	@UserProperty(description = "opening of the horizontal blades")
	private OPEN_CLOSE openCloseHorizontal = OPEN_CLOSE.OPEN_IN_CLOSE_OUT;
	@UserProperty(description = "opening of the horizontal blades")
	private OPEN_CLOSE openCloseVertical = OPEN_CLOSE.OPEN_IN_CLOSE_OUT;
	

	Shape screen;

	private DimensionHelper dimensionHelper;
	private long frameFrom;
	private long frameTo;


	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {
//		int relFrameCount = (int) (frameCount - frameFrom);
//		int posInSlideDuration = relFrameCount;
//		int duration = (int) (frameTo - frameFrom);
//
//		if(repeat > 1) {
//			duration = repeatDurationFrames;
//			if(relFrameCount / repeatDurationFrames < repeat) {
//				posInSlideDuration = relFrameCount % duration;
//			}
//			else {
//				posInSlideDuration = repeatDurationFrames ;
//			}
//		}
		
		int posInSlideDuration = repeater.repeat(frameFrom, frameTo, frameCount);
		int repeatDurationFrames = repeater.getRepeatDurationFrames(frameFrom, frameTo);
		
		Area fillArea = new Area(screen);

		
		AffineTransform atsc = scalerOutline.scale(posInSlideDuration, repeatDurationFrames);
		fillArea.transform(atsc);
		
		AffineTransform atp = positioner.position(dimensionHelper, fillArea.getBounds());
		fillArea.transform(atp);
		
		Rectangle outlineScPos = fillArea.getBounds();

		AffineTransform atscb = scalerBlinds.scale(posInSlideDuration, repeatDurationFrames);

		Shape bladeHorizontal;
		
		double maxBladeHeight = 0;
		if(numberHorizontal > 0) {
			maxBladeHeight = Math.ceil(outlineScPos.height / numberHorizontal);
		}
		
		Shape bladeVertical;
		
		double maxBladeWidth = 0;
		if(numberVertical > 0) {
			maxBladeWidth = Math.ceil(outlineScPos.width / numberVertical);
		}
		
		double scaleY = openCloseHorizontal == OPEN_CLOSE.OPEN_IN_CLOSE_OUT ?  1.0 - atscb.getScaleY() : atscb.getScaleY();
		
		
		if (bladeShapeHorizontal == BLADE_SHAPE.ELLIPSE) {
			bladeHorizontal = new Ellipse2D.Double(outlineScPos.x, outlineScPos.y, outlineScPos.width, maxBladeHeight * scaleY);
		} else {
			bladeHorizontal = new Rectangle2D.Double(outlineScPos.x, outlineScPos.y, outlineScPos.width, maxBladeHeight * scaleY);
		}		

		double scaleX = openCloseVertical == OPEN_CLOSE.OPEN_IN_CLOSE_OUT ?  1.0 - atscb.getScaleX() : atscb.getScaleX();
		
		if (bladeShapeVertical == BLADE_SHAPE.ELLIPSE) {
			bladeVertical = new Ellipse2D.Double(outlineScPos.x, outlineScPos.y, maxBladeWidth * scaleX, outlineScPos.height);
		} else {
			bladeVertical = new Rectangle2D.Double(outlineScPos.x, outlineScPos.y, maxBladeWidth * scaleX, outlineScPos.height);
		}
		
		Area bladeAreaHorizontal = new Area(bladeHorizontal);
		
		AffineTransform transformH = new AffineTransform();
		transformH.translate(0, ((maxBladeHeight - bladeAreaHorizontal.getBounds2D().getHeight())/2.0));
		bladeAreaHorizontal.transform(transformH);
		
		graphics2D.setColor(colorHorizontal);
		final Composite saveComposite = fader.fade(graphics2D, posInSlideDuration, repeatDurationFrames);
		
		for (int i = 0; i < numberHorizontal; i++) {
			graphics2D.fill(bladeAreaHorizontal);
			AffineTransform transform = new AffineTransform();
			transform.translate(0, maxBladeHeight );
			bladeAreaHorizontal.transform(transform);
		}
		
		Area bladeAreaVertical = new Area(bladeVertical);
		
		AffineTransform transformV = new AffineTransform();
		transformV.translate(((maxBladeWidth - bladeAreaVertical.getBounds2D().getWidth())/2.0), 0);
		bladeAreaVertical.transform(transformV);
		
		graphics2D.setColor(colorVertical);
		
		for (int i = 0; i < numberVertical; i++) {
			graphics2D.fill(bladeAreaVertical);
			AffineTransform transform = new AffineTransform();
			transform.translate(maxBladeWidth, 0);
			bladeAreaVertical.transform(transform);
		}
		
		
		//graphics2D.setColor(Color.RED);
		//graphics2D.draw(outlineScPos);

		graphics2D.setClip(null);
		graphics2D.setComposite(saveComposite);

	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo) {
		int width = videoOutputInfo.getWidth();
		int height = videoOutputInfo.getHeight();
		dimensionHelper = new DimensionHelper(videoOutputInfo);
		screen = new Rectangle2D.Double(0,0,width, height);
	}

	@Override
	public void postFrame() {
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		graphics.setColor(colorHorizontal);
		graphics.fillRect(0, 0, width, height);
		if (colorHorizontal.getRed() > 245 && colorHorizontal.getGreen() > 245 && colorHorizontal.getBlue() > 245) {
			graphics.setColor(Color.BLACK);
			graphics.drawRect(0, 0, width - 1, height - 1);
		}
	}

	@Override
	public void setFrameRange(long frameFrom, long frameTo) {
		this.frameFrom = frameFrom;
		this.frameTo = frameTo;
	}

	@Override
	public DisplayDuration<?>[] getFrameFromTos() {
		int effects = 2;
		int repeat = repeater.getRepeat();
		int repeatDurationFrames = repeater.getRepeatDurationFrames(frameFrom, frameTo);
		DisplayDuration<?>[]result = new DisplayDuration<?>[repeat * effects];
		int c = 0;
		for(int r = 0; r < repeat * effects; r += effects) {
			long end = repeat == 1 ? frameTo : frameFrom + repeatDurationFrames * (c + 1) - 1;
			result[r] = scalerBlinds.getDisplayDuration(frameFrom + repeatDurationFrames * c, end);
			result[r + 1] = fader.getDisplayDuration(frameFrom + repeatDurationFrames * c, end);
			c++;
		}
		return result;
	}

	@Override
	public int getEditorColumns() {
		return 2;
	}
	

}
