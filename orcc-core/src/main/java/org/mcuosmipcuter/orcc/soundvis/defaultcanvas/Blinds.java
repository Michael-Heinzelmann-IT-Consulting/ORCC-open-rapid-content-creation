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
import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayDuration;
import org.mcuosmipcuter.orcc.api.soundvis.DisplayUnit;
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
 * Displays vertical and horizontal opening/closing blinds
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
	
	@NestedProperty(description = "repeating inside from and to")
	private Repeater repeater = new Repeater(scalerOutline, scalerBlinds, fader);

	@ChangesIcon
	@LimitedIntProperty(minimum=0, description="number cannot be lower than 0")
	@UserProperty(description="number of blinds horizontal")
	private int numberHorizontal = 10;
	@ChangesIcon
	@LimitedIntProperty(minimum=0, description="number cannot be lower than 0")
	@UserProperty(description="number of blinds vertical")
	private int numberVertical = 10;
	
	@ChangesIcon
	@UserProperty(description = "color of the horizontal blades")
	private Color colorHorizontal = Color.BLACK;
	@ChangesIcon
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
		
//		int posInSlideDuration = repeater.repeat(frameFrom, frameTo, frameCount);
//		int repeatDurationFrames = repeater.getRepeatDurationFrames(frameFrom, frameTo);
		for(DisplayUnit displayUnit : repeater.repeat(frameFrom, frameTo, frameCount)) {
		
		Area fillArea = new Area(screen);

		
		AffineTransform atsc = scalerOutline.scale(displayUnit, fillArea.getBounds().width, fillArea.getBounds().height);
		fillArea.transform(atsc);
		
		AffineTransform atp = positioner.position(dimensionHelper, fillArea.getBounds());
		fillArea.transform(atp);
		
		Rectangle outlineScPos = fillArea.getBounds();

		AffineTransform atscb = scalerBlinds.scale(displayUnit, fillArea.getBounds().width, fillArea.getBounds().height);

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
		final Composite saveComposite = fader.fade(graphics2D, displayUnit);
		
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
		if(numberHorizontal != 0) {
			int h = height / numberHorizontal / 2;
			h = h < 2 ? 2 : h;
			graphics.setColor(colorHorizontal);
			for(int i = 0; h * i * 2 < height; i++) {
				graphics.fillRect(0, h * i * 2, width, h);
			}
		}
		if(numberVertical != 0) {
			int w = width / numberVertical / 2;
			w = w < 1 ? 1 : w;
			graphics.setColor(colorVertical);
			for(int i = 0; w * i * 2 < width; i++) {
				graphics.fillRect(w * i * 2, 0 , w, height);
			}
		}
	}

	@Override
	public void setFrameRange(long frameFrom, long frameTo) {
		this.frameFrom = frameFrom;
		this.frameTo = frameTo;
	}

	@Override
	public DisplayDuration<?>[] getFrameFromTos() {
		return repeater.getFrameFromTos(frameFrom, frameTo);
	}

	@Override
	public int getEditorColumns() {
		return 2;
	}
	

}
