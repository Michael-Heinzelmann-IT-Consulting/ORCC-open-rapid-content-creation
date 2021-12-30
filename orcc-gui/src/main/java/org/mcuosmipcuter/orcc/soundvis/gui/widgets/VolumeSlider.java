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
package org.mcuosmipcuter.orcc.soundvis.gui.widgets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 * Extension of {@link JSlider} to support logarithmic adjustment
 * @author Michael Heinzelmann
 */
public class VolumeSlider extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private float value;
	private float minimum;
	private float maximum;
	private int selectPoint;
	
	private int margin = 10;

	/**
	 * Callback interface for asynchronous communication
	 * @author Michael Heinzelmann
	 */
	public interface VolumeListener {
		/**
		 * Method to notify of a volume change
		 * @param value the current volume value
		 */
		public void adjustVolume(float value);
	}
	
	/**
	 * A new slider with a listener that will be notified of volume changes
	 * @param volumeListener
	 */
	public VolumeSlider(final VolumeListener volumeListener) {
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(isEnabled()){
					volumeListener.adjustVolume(value);
				}
			};
		});
		addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if(isEnabled()){
					view2model(e.getX());
					volumeListener.adjustVolume(value);
				}
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if(isEnabled()){
					view2model(e.getX());
					volumeListener.adjustVolume(value);
				}
				repaint();
			}
			
		});
	}
	
	// works with percentage and log 10, max. 100 % = log10(100) = 2 min. 1% = log10(1) = 0
	private void view2model(int x) {
		
		if(x <= margin) {
			selectPoint = margin + 1;
			value = minimum;
		}
		else if(x > (getWidth() - margin)) {
			selectPoint = getWidth() - margin - 1;
			value = maximum;
		}
		else {
			float viewRange = getWidth() - margin * 2;
			float viewValue = x - margin; // subtract the left margin
			
			float viewPercentage = viewValue * 100 / viewRange;
			float viewLogValue = (float)Math.log10(viewPercentage);
			
			float ratio = viewLogValue / 2;
			float modelRange = maximum - minimum;

			float v =  modelRange * ratio;
			value = minimum + v;
			selectPoint = x;
		}
	}
	
	// reverse of view2model 10 ^ 2 = 100 %
	private void model2view(float val) {
		
		if(val == minimum) {
			selectPoint = margin + 1;
		}
		else if(val == maximum) {
			selectPoint = getWidth() - margin - 1;
		}
		else if(val > minimum && val < maximum) {
			
			float v = val - minimum ;
			float modelRange = maximum - minimum;
			float ratio = v / modelRange;
			float viewLogValue = ratio * 2;
			float viewPercentage = (float)Math.pow(10, viewLogValue);		
			float viewRange = getWidth() - margin * 2;
			float viewValue = viewPercentage * viewRange / 100;
			int x = margin + (int)viewValue; // adding the left margin
			
			selectPoint = x;
		}
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(isEnabled() ? Color.GRAY : Color.LIGHT_GRAY);
		int w = getWidth();
		int h = getHeight();
		g.fillPolygon(new int[] {margin, w - margin, w - margin,  w - margin - 7,  margin}, new int[] { h/2, h/2, 3, 3, h / 2}, 5);
		g.setColor(Color.BLACK);
		g.drawLine(selectPoint, 2, selectPoint, h / 2);
		g.drawLine(selectPoint + 1, 2, selectPoint + 1, h / 2);
		String str = value > 0 ? "+" + (int)value : String.valueOf((int)value);
		g.drawString(str, selectPoint - g.getFontMetrics().stringWidth(str) / 2, h - 1);
	}

	/**
	 * Sets the model value of this slider and updates the view
	 * @param value
	 */
	public void setValue(float value) {
		this.value = value;
		model2view(value);
		repaint();
	}

	/**
	 *  Get the model value
	 * @return the value
	 */
	public float getValue() {
		return value;
	}

	/**
	 * Sets the model minimum value of this slider
	 * @param minimum the minimum value
	 */
	public void setMinimum(float minimum) {
		this.minimum = minimum;
	}

	/**
	 * Sets the model maximum value of this slider
	 * @param maximum the maximum value
	 */
	public void setMaximum(float maximum) {
		this.maximum = maximum;
	}

	/**
	 * Get the model minimum value
	 * @return the minimum value
	 */
	public float getMinimum() {
		return minimum;
	}

	/**
	 * Get the model maximum value
	 * @return the maximum value
	 */
	public float getMaximum() {
		return maximum;
	}
	
	
}
