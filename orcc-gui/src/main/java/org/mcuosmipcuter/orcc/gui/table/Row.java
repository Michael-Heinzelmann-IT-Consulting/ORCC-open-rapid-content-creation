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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.WidgetUtil;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties.EditorLifeCycle;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties.PropertyPanelFactory;

/**
 * Row for the custom table {@link CustomTable}
 * @author Michael Heinzelmann
 */
public class Row extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private final SoundCanvasWrapper soundCanvasWrapper;
	private final TitledBorder titledBorder;
	private final LineBorder unselectedBorder;
	private final int borderSize;
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
	public Row(SoundCanvasWrapper soundCanvasWrapper, Color defaultColor, int borderSize, final JFrame parentFrame) {
		this.soundCanvasWrapper = soundCanvasWrapper;
		this.unselectedBorder = new LineBorder(defaultColor, borderSize);
		this.borderSize = borderSize;
		titledBorder = new TitledBorder(unselectedBorder);
		titledBorder.setTitle(soundCanvasWrapper.getDisplayName());
		titledBorder.setTitlePosition(TitledBorder.TOP);
		setBorder(titledBorder);
		props = PropertyPanelFactory.getCanvasPanels(soundCanvasWrapper, parentFrame);
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
		        
				final JSpinner scale = WidgetUtil.getIntegerSpinner(soundCanvasWrapper.getScale(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1, Unit.PERCENT_VIDEO, null);
				scale.setPreferredSize(new Dimension(80, 26));
				scale.setToolTipText("scale in % of video");		
				scale.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent arg0) {
						soundCanvasWrapper.setScale(((((Number)scale.getValue()).intValue())));
						Context.touch();
					}
				});
				
				final JSpinner posX = WidgetUtil.getIntegerSpinner(soundCanvasWrapper.getPosX(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1, Unit.PERCENT_VIDEO, null);
				posX.setPreferredSize(new Dimension(80, 26));
				posX.setToolTipText("position X");	
				posX.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent arg0) {
						soundCanvasWrapper.setPosX(((((Number)posX.getValue()).intValue())));
						Context.touch();
					}
				});
				final JSpinner posY = WidgetUtil.getIntegerSpinner(soundCanvasWrapper.getPosY(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1, Unit.PERCENT_VIDEO, null);
				posY.setPreferredSize(new Dimension(80, 26));
				posY.setToolTipText("position Y");	
				posY.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent arg0) {
						soundCanvasWrapper.setPosY(((((Number)posY.getValue()).intValue())));
						Context.touch();
					}
				});
		
				JPanel commonP = new JPanel();
				commonP.setLayout(new GridLayout(1, 3));
				
				JPanel scaleP = commonPanel("scale X/Y", scale);
				commonP.add(scaleP);
				JPanel posxP = commonPanel("position X", posX);
				commonP.add(posxP);
				JPanel posyP = commonPanel("position Y", posY);
				commonP.add(posyP);
				
				gc.gridwidth = GridBagConstraints.REMAINDER;
				gl.setConstraints(commonP, gc);
				panel.add(commonP);

				gc.gridwidth = 1;
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
	
	@Override
	public void setBackground(Color bg) {
		if(titledBorder != null) {
			titledBorder.setBorder(new LineBorder(bg, borderSize));
		}
		super.setBackground(bg);
	}

	private JPanel commonPanel(String name, Component c) {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1 , 2, 12, 12));
		JLabel nameLabel = new JLabel(name);
		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		p.add(nameLabel);
		p.add(c);
	
		return p;
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
