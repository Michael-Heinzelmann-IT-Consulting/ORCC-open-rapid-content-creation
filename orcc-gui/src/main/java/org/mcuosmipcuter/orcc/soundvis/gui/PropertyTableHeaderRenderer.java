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
package org.mcuosmipcuter.orcc.soundvis.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

/**
 * @author Michael Heinzelmann
 *
 */
public class PropertyTableHeaderRenderer  implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(final JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, final int column) {

		JLabel label = new JLabel(String.valueOf(value));
		label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
		label.setForeground(table.getTableHeader().getReorderingAllowed() ? Color.BLACK : Color.DARK_GRAY);
		JPanel p = new JPanel();
		p.setBorder(new LineBorder(Color.WHITE, 5));
		p.add(label);
		return p;
	}

}
