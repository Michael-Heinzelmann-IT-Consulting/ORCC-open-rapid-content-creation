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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public class MouseGrabAndMove extends MouseAdapter {
	
	private final JComponent container;
	private final  JComponent owner;
	private final Cursor moveCursor;
	 
	private  Component source;
	private  Component target;
	private Cursor cursor;
	private final Color originalBackground;
	
	private Color selectColor = Color.GRAY;

	
	public MouseGrabAndMove(JComponent container, JComponent owner, Cursor moveCursor) {
		this.container = container;
		this.owner = owner;
		this.moveCursor = moveCursor;
		this.originalBackground = owner.getBackground();
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		final Component oldTarget = target;
		target = container.getComponentAt(owner.getX() + e.getX(), owner.getY() + e.getY());
		
		if(oldTarget != null && oldTarget != target) {
			oldTarget.setBackground(originalBackground);
			if(target != null) {
			target.setBackground(Color.ORANGE);
			move();
			}
		}

		if(source != null) {
			source.setBackground(selectColor);
		}

	}
	private void move() {
		final Component oldTarget = target;
		if(target != null && source != target) {
			oldTarget.setBackground(originalBackground);
			int sourceIndex = -1;
			int targetIndex = -1;
			Component[] components = container.getComponents();
			for(int i = 0; i < components.length; i++) {
				
				if(components[i] == source) {
					sourceIndex = i;
				}
				if(components[i] == target) {
					targetIndex = i;
				}
			}
			if(sourceIndex!= -1 && targetIndex != -1 && sourceIndex != targetIndex) {
				List<Component> list = new ArrayList<Component>();
				for(Component c : components) {
					list.add(c);
				}
				list.remove(sourceIndex);
				list.add(targetIndex, source);
				container.removeAll();
				for(Component c : list) {
					container.add(c);
				}
				container.revalidate();
			}

		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		source = owner;
		cursor = container.getCursor();
		container.setCursor(moveCursor);
		owner.setBackground(selectColor);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {

		if( cursor != null) {
			container.setCursor(cursor);
		}
		if( cursor != null) {
			container.setCursor(cursor);
		}
		if(target != null) {
			target.setBackground(originalBackground);
		}
		if(source != null) {
			source.setBackground(originalBackground);
		}
		target = null;
	}

}
