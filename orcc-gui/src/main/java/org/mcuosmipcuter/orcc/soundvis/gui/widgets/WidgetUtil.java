/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2020 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.mcuosmipcuter.orcc.api.util.TextHelper;

/**
 * @author Michael Heinzelmann
 *
 */
public class WidgetUtil  {

	public static JPanel getMessagePanel(String msg, int fontSize, Graphics graphics) {
		JPanel popUpContentPanel = new JPanel();
		popUpContentPanel.setBackground(Color.YELLOW);
		popUpContentPanel.setBorder(new  LineBorder(Color.YELLOW, 8, false));
		JLabel label = new JLabel(msg);
		Font font = new Font("dialog", Font.PLAIN, fontSize);
		label.setOpaque(true);
		label.setFont(font);
		label.setForeground(Color.RED);
		label.setBackground(Color.YELLOW);
		Graphics copy = graphics.create();
		copy.setFont(font);
		label.setPreferredSize(TextHelper.getTextDimesion(new String[] {msg}, copy));
		popUpContentPanel.add(label);

		return popUpContentPanel;
	}

}
