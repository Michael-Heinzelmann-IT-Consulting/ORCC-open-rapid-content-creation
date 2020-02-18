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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.NestedProperty;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.UserProperty;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.api.util.DimensionHelper;
import org.mcuosmipcuter.orcc.soundvis.effects.Fader;
import org.mcuosmipcuter.orcc.soundvis.effects.Positioner;
import org.mcuosmipcuter.orcc.soundvis.effects.Scaler;

/**
 * Displays a solid color
 * @author Michael Heinzelmann
 */
public class Shutter implements SoundCanvas {
	
	public static enum CLIP_SHAPE {
		RECTANGLE, ELLIPSE, DIAMOND,
	}
	
	@UserProperty(description="color of the area")
	private Color color = Color.BLACK;
	@UserProperty(description="color of the area")
	private CLIP_SHAPE clipShape = CLIP_SHAPE.RECTANGLE;
	
	private int width;
	private int height;
	Shape screen = new Rectangle(width, height);
	
	private DimensionHelper dimensionHelper;
	private long frameFrom;
	private long frameTo;
	
	Polygon diamond;
	
	
	@NestedProperty(description = "x and y position")
	Positioner positioner = new Positioner();
	
	@NestedProperty(description = "scale in and out")
	Scaler scaler = new Scaler();
	
	@NestedProperty(description = "fading in and out")
	private Fader fader = new Fader();

	@Override
	public void nextSample(int[] amplitudes) {
	}

	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {	
		int posInSlideDuration = (int)(frameCount - frameFrom);

		  Shape clip;
		  
		  if(clipShape == CLIP_SHAPE.ELLIPSE) {
			  clip = new Ellipse2D.Float(0, 0, width ,height);
		  }
		  else if(clipShape == CLIP_SHAPE.DIAMOND) {
			  clip = diamond;
			  
		  }
		  else {
			  clip = new Rectangle(width, height);
		  }
				  
			
			AffineTransform ats = scaler.scale(posInSlideDuration, (int)(frameTo - frameFrom));
			//ats.concatenate(atp);
			Area seeTrough = new Area(clip);

			//seeTrough.transform(ats);
			
			seeTrough.transform(ats);
			AffineTransform atp = positioner.position(dimensionHelper, seeTrough.getBounds());
			seeTrough.transform(atp);
			//fillArea.exclusiveOr(seeTrough);

			Area fillArea = new Area(screen);
			fillArea.subtract(seeTrough);
			graphics2D.setColor(color);

			//final AffineTransform saveAT = graphics2D.getTransform();
			graphics2D.setClip(fillArea);
			final Composite saveComposite = fader.fade(graphics2D, posInSlideDuration, (int)(frameTo - frameFrom));
			//graphics2D.transform(at);

			graphics2D.fillRect(0, 0, width, height);
			
			//graphics2D.setTransform(new AffineTransform());
			graphics2D.setClip(null);
			graphics2D.setComposite(saveComposite);
		
	}

	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		width = videoOutputInfo.getWidth();
		height = videoOutputInfo.getHeight();
		dimensionHelper =  new DimensionHelper(videoOutputInfo);
		screen = new Rectangle(width, height);
		diamond = new Polygon(new int[] {0, width / 2, width, width / 2}, new int[] {height / 2, height, height / 2, 0,}, 4);
		//fillArea = new Area(screen);
	}

	@Override
	public void postFrame() {
	}

	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		graphics.setColor(color);
		graphics.fillRect(0, 0, width, height);
		if(color.getRed() > 245 && color.getGreen() > 245 && color.getBlue() > 245) {
			graphics.setColor(Color.BLACK);
			graphics.drawRect(0, 0, width -1, height - 1);
		}
	}
	public void setFrameRange(long frameFrom, long frameTo){
		this.frameFrom = frameFrom;
		this.frameTo = frameTo;
	}

}
