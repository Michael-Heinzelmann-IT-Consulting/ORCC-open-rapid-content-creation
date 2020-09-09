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
package org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.mcuosmipcuter.orcc.gui.util.GraphicsUtil;


/**
 * Panel to display nested canvas properties editors
 * @author Michael Heinzelmann
 */
public class NestedPropertyPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private JButton ocButton = new JButton("...");
	private JLabel nameLabel = new JLabel();
	boolean expanded;
	JPanel popUpContentPanel = new JPanel();
	Popup popup = null;
	/**
	 * Sets up a grid layout
	 */
	public NestedPropertyPanel(Set<PropertyPanel<?>> props, String ownerName, String name) {

		setOpaque(true);
		setPreferredSize(new Dimension(100, 25));
		setLayout(new GridLayout(1 , 2, 12, 12));
		nameLabel.setText(name);
		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(nameLabel);
		updateSettingsLabel(props);
		add(ocButton);	
		JButton close = new JButton("close");
		close.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSettingsLabel(props);
				popup.hide();
				ocButton.setEnabled(true);
				NestedPropertyPanel.this.setBackground(Color.LIGHT_GRAY);
			}
		});
		
		ocButton.addActionListener(new ActionListener() {
		
			@Override
			public void actionPerformed(ActionEvent arg0) {

				Rectangle screen = GraphicsUtil.getRootComponentOutline(NestedPropertyPanel.this);
				int lowLimit = screen.y + screen.height;
				Point loc = NestedPropertyPanel.this.ocButton.getLocationOnScreen();
				popUpContentPanel.doLayout();
				int extentY = loc.y + popUpContentPanel.getPreferredSize().height;
				// debug: System.err.println("extentY " + extentY  + " lowLimit " + lowLimit);
				int yToUse = extentY < lowLimit ? loc.y : loc.y - (extentY - lowLimit) - 10;
				
				popup = PopupFactory.getSharedInstance().getPopup(NestedPropertyPanel.this, popUpContentPanel, loc.x, yToUse);

				NestedPropertyPanel.this.setBackground(Color.YELLOW);
				
				ocButton.setEnabled(false);
				popup.show();
				// TODO undo option
				updateSettingsLabel(props);
			}
		});

		setBackground(Color.YELLOW);
		popUpContentPanel.setBorder(new  LineBorder(Color.BLUE, 4, true));
		GridLayout gl = new GridLayout(props.size() + 2, 1, 0, 0);		
		popUpContentPanel.setLayout(gl);
		popUpContentPanel.setPreferredSize(new Dimension(220, (props.size() + 2) * 30));
		popUpContentPanel.add(close);
		JLabel parents = new JLabel(ownerName + "::" + name, SwingConstants.CENTER);
		popUpContentPanel.add(parents);

		for(final JPanel p : props) {
			popUpContentPanel.add(p);
		}

		popUpContentPanel.revalidate();
		
	}

	private void updateSettingsLabel(Set<PropertyPanel<?>> props) {
		StringBuilder sb = new StringBuilder();
		for(final PropertyPanel<?> p : props) {
			sb.append( "|" + p.getCurrentValue() );
		}
		ocButton.setToolTipText(sb.toString());
	}
}
