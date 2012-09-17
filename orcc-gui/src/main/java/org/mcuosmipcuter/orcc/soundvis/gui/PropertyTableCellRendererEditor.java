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

import java.awt.Component;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;



public class PropertyTableCellRendererEditor extends DefaultTableCellRenderer implements TableCellEditor {
	
	private static final long serialVersionUID = 1L;

	Map<SoundCanvas, CanvasPropertyPanel> map = new HashMap<SoundCanvas, CanvasPropertyPanel>();
	
	private CellEditorListener cellEditorListener;
	
	public PropertyTableCellRendererEditor() {
		final Context.Listener closeEnabledListener = new Listener() {
			@Override
			public void contextChanged(PropertyName propertyName) {
				if(PropertyName.AppState.equals(propertyName)) {
					for(CanvasPropertyPanel p : map.values()) {
						p.setCloseEnabled(Context.getAppState() == AppState.READY);
					}
				}
			}
		};
		Context.addListener(closeEnabledListener);
	}
	
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		//System.err.println("renede " + value);
		if(value == null){
			return new JPanel();
			//return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		SoundCanvasWrapper soundCanvas = (SoundCanvasWrapper) value;
		
		CanvasPropertyPanel p = map.get(value);
		if(p == null) {
			System.err.println("p was null");
			 p = new CanvasPropertyPanel(soundCanvas);
			 map.put((SoundCanvas)value, p);
		}
		return p;
	}

	public boolean stopCellEditing() {
		System.err.println("stopCellEditing ");
			cellEditorListener.editingCanceled(new ChangeEvent(this));
		
		return true;
	}

	public boolean shouldSelectCell(EventObject arg0) {
		//System.err.println("shouldSelectCell");
		return true;
	}

	public void removeCellEditorListener(CellEditorListener arg0) {
		System.err.println("removeCellEditorListener " + arg0);
		//cellEditorListeners.remove(arg0);
	}

	public boolean isCellEditable(EventObject arg0) {
		//System.err.println("isCellEditable");arg0.
		return true;
	}

	public Object getCellEditorValue() {
		System.err.println("getCellEditorValue " );
		return  null;
	}

	public void cancelCellEditing() {
		System.err.println("cancelCellEditing");
		cellEditorListener.editingCanceled(new ChangeEvent(this));
	}

	public void addCellEditorListener(CellEditorListener arg0) {
		System.err.println("addCellEditorListener: " + arg0);
		//new Exception().printStackTrace();
		this.cellEditorListener = arg0;
	}


	@Override
	public Component getTableCellEditorComponent(JTable arg0, Object value,
			boolean arg2, int arg3, int column) {
		System.err.print("get e " + value + " col " + column);
		CanvasPropertyPanel p = map.get(value);
		System.err.println(" p e was " + p);
		return p;
	}
}
