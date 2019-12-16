/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2014 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.soundvis.threads;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;

import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;

public class ProgressPainterThread implements SoundCanvas {
	
	int segments = 12;
	int dotSize;
	int width;
	int height;
	int [] xValues;
	int [] yValues;
	Color [] shades;
	int lead = 0;

	@Override
	public void nextSample(int[] amplitudes) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void newFrame(long frameCount, Graphics2D graphics2D) {
//		graphics2D.setColor(Color.LIGHT_GRAY);
//		Composite origComposite = graphics2D.getComposite();
//		graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); 
//		graphics2D.fillRect(0, 0, width, height);
//		graphics2D.setComposite(origComposite);
		
		
			int offset = lead % segments;
			
			for(int s = 0; s < segments; s++) {
				int idx = Math.abs((s - offset)) % segments;
				graphics2D.setColor(shades[idx]);
				graphics2D.fillOval(xValues[s]  - dotSize / 2, yValues[s] - dotSize / 2, dotSize, dotSize);
			}
			lead++;
	}


	@Override
	public void prepare(AudioInputInfo audioInputInfo,
			VideoOutputInfo videoOutputInfo) {
		this.width = videoOutputInfo.getWidth();
		this.height = videoOutputInfo.getHeight();
		int centerX = width / 2;
		int centerY = height / 2;
		int radius = Math.min(width, height) / 4;
		dotSize = radius / 5;
		xValues = new int[segments];
		yValues = new int[segments];
		shades = new Color[segments];
		int degrees = 360 / segments;
		for(int s = 0; s < segments; s++) {
			int x = centerX  + (int)(radius * Math.cos(degrees * s * (Math.PI / 180)));
			int y = centerY + (int)(radius * Math.sin(degrees * s * (Math.PI / 180)));
			xValues[s] = x;
			yValues[s] = y;
			int g = s * (255 / segments);
			shades[s] = new Color(g, g, g);
		}	
	}


	@Override
	public void postFrame() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void updateUI(int width, int height, Graphics2D graphics) {
		// TODO Auto-generated method stub
		
	}

}
