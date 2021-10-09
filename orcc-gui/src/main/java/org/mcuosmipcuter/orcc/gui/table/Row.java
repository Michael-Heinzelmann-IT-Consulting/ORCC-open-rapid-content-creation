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
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties.EditorLifeCycle;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties.PropertyPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties.PropertyPanelFactory;

/**
 * Row for the custom table {@link CustomTable}
 * @author Michael Heinzelmann
 */
public class Row extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private final SoundCanvasWrapper soundCanvasWrapper;
	private JPanel panel;
	private final Set<JPanel> props ;
	private boolean panelVisible;
	private int headerHeight;
	public boolean isPanelVisible() {
		return panelVisible;
	}

	/**
	 * New row containing the given wrapped canvas
	 * @param soundCanvasWrapper the wrapped canvas that belongs to this layer row
	 */
	public Row(SoundCanvasWrapper soundCanvasWrapper) {
		this.soundCanvasWrapper = soundCanvasWrapper;
		props = PropertyPanelFactory.getCanvasPanels(soundCanvasWrapper);
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
			passivatePropertyEditors();
			remove(panel);
			setPreferredSize(new Dimension(getPreferredSize().width, headerHeight));
			setMaximumSize(getPreferredSize());
		}
		else {
			if(panel == null) {
				panel = new JPanel();
				panel.setBorder(new LineBorder(panel.getBackground(), 8, true));
				GridBagConstraints gc = new GridBagConstraints();
				GridBagLayout gl = new GridBagLayout();
				panel.setLayout(gl);
				gc.fill = GridBagConstraints.BOTH;
				gc.anchor = GridBagConstraints.LINE_START;
				gc.insets = new Insets(3, 0, 10, 0);
		        gc.weightx = 1;
		        
				final JSpinner scale = new JSpinner();
				scale.setToolTipText("scale in % of video");
				scale.setValue(soundCanvasWrapper.getScale());		
				scale.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent arg0) {
						soundCanvasWrapper.setScale(((((Number)scale.getValue()).intValue())));
						Context.touch();
					}
				});
				
				final JSpinner posX = new JSpinner();
				posX.setToolTipText("position X");
				posX.setValue(soundCanvasWrapper.getPosX());		
				posX.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent arg0) {
						soundCanvasWrapper.setPosX(((((Number)posX.getValue()).intValue())));
						Context.touch();
					}
				});
				final JSpinner posY = new JSpinner();
				posY.setToolTipText("position Y");
				posY.setValue(soundCanvasWrapper.getPosY());		
				posY.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent arg0) {
						soundCanvasWrapper.setPosY(((((Number)posY.getValue()).intValue())));
						Context.touch();
					}
				});
		
				JPanel scaleP = commonPanel("scale", scale);
				gl.setConstraints(scaleP, gc);
				panel.add(scaleP);
				JPanel posxP = commonPanel("position X", posX);
				gl.setConstraints(posxP, gc);
				panel.add(posxP);
				JPanel posyP = commonPanel("position Y", posY);
				gc.gridwidth = GridBagConstraints.REMAINDER;
				gl.setConstraints(posyP, gc);
				panel.add(posyP);
				
				gc.insets = new Insets(3, 0, 0, 0);

				int maxCols = soundCanvasWrapper.getSoundCanvas().getEditorColumns();
				int c = 0;
				for(final JPanel p : props) {
					c++;
					if(c == maxCols) {
						gc.gridwidth = GridBagConstraints.REMAINDER;
						c = 0;
					}
					else {
						gc.gridwidth = 1;
					}
					gl.setConstraints(p, gc);
					panel.add(p);
				}
			}
			int h = headerHeight + panel.getPreferredSize().height;
			add(panel, BorderLayout.SOUTH);
			setPreferredSize(new Dimension(getPreferredSize().width, h));
			setMaximumSize(getPreferredSize());
		}
		panelVisible = !panelVisible;
	}
	private JPanel commonPanel(String name, Component c) {
		JPanel p = new JPanel();
		p.setBackground(getBackground());
		//p.setBorder(new LineBorder(getBackground(), 2));
		p.setLayout(new GridLayout(1 , 2, 12, 12));
		JLabel nameLabel = new JLabel(name);
		nameLabel.setPreferredSize(new Dimension(200, 10));
		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		p.add(nameLabel);
		p.add(c);
	
		return p;
	}
	public void changeSize(PropertyPanel<?> p) {

		for(Component c : panel.getComponents()) {
			if(c == p) {
				panel.revalidate();
				int h = headerHeight + panel.getPreferredSize().height;
				setPreferredSize(new Dimension(getPreferredSize().width, h));
				revalidate();
				break;
			}
		}
		
	}
	public void passivatePropertyEditors() {
		if(panel != null) {
			for(Component c : panel.getComponents()) {
				if(c instanceof EditorLifeCycle) {
					((EditorLifeCycle)c).passivate();
				}
			}
		}
	}
}
