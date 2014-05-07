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
package org.mcuosmipcuter.orcc.gui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Set;

import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.CanvasPropertyPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties.PropertyPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties.PropertyPanelFactory;

/**
 * Row for the custom table {@link CustomTable}
 * @author Michael Heinzelmann
 */
public class Row extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private final SoundCanvasWrapper soundCanvasWrapper;
	private final CanvasPropertyPanel canvasPropertyPanel;
	JPanel panel;
	boolean panelVisible;
	int headerHeight;
	public boolean isPanelVisible() {
		return panelVisible;
	}

	/**
	 * New row containing the given wrapped canvas
	 * @param soundCanvasWrapper the wrapped canvas that belongs to this layer row
	 */
	public Row(SoundCanvasWrapper soundCanvasWrapper) {
		this.soundCanvasWrapper = soundCanvasWrapper;
		this.canvasPropertyPanel = new CanvasPropertyPanel(soundCanvasWrapper);
		
	}

	/**
	 * Gets the panel (for showing it to the user)
	 * @return
	 */
	public CanvasPropertyPanel getCanvasPropertyPanel() {
		return canvasPropertyPanel;
	}

	/**
	 * Gets the wrapped canvas (for updating the model)
	 * @return
	 */
	public SoundCanvasWrapper getSoundCanvasWrapper() {
		return soundCanvasWrapper;
	}
	public void toggleProperties() {
		if(headerHeight == 0) {
			headerHeight = getPreferredSize().height;
		}
		if(panelVisible) {
			remove(panel);
			setPreferredSize(new Dimension(getPreferredSize().width, headerHeight));
			setMaximumSize(getPreferredSize());
		}
		else {
			if(panel == null) {
				panel = new JPanel();
				//GridLayout gl = new GridLayout(0, 1, 5, 4);
				GridBagConstraints gc = new GridBagConstraints();
				gc.gridwidth = GridBagConstraints.REMAINDER;	
				gc.fill = GridBagConstraints.HORIZONTAL;
				gc.gridx = GridBagConstraints.EAST;
				gc.insets = new Insets(3, 6, 3, 6);
				panel.setLayout(new GridBagLayout());
				Set<PropertyPanel<?>> props = PropertyPanelFactory.getCanvasPanels(soundCanvasWrapper);

				for(final PropertyPanel<?> p : props) {
					panel.add(p, gc);
				}
			}
			int h = headerHeight + panel.getPreferredSize().height;
			add(panel, BorderLayout.SOUTH);
			setPreferredSize(new Dimension(getPreferredSize().width, h));
			setMaximumSize(getPreferredSize());
		}
		panelVisible = !panelVisible;
	}
	public void changeSize(PropertyPanel p) {
		int index = 0;
		for(Component c : panel.getComponents()) {
			if(c == p) {
				panel.revalidate();
				int h = headerHeight + panel.getPreferredSize().height;
				setPreferredSize(new Dimension(getPreferredSize().width, h));
				revalidate();
				break;
			}
			index++;
		}
		
	}
}
