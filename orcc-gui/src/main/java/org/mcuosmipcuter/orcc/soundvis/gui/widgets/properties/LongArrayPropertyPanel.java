/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2021 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;

/**
 * @author Michael Heinzelmann
 * @param <T>
 *
 */
public class LongArrayPropertyPanel extends PropertyPanel<long[]> {

	private static final long serialVersionUID = 1L;

	private JSpinner index = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
	private JSpinner valueAt = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
	ChangeListener valueChanged;
	ChangeListener indexChanged;

	public LongArrayPropertyPanel(SoundCanvasWrapper soundCanvasWrapper, Object valueOwner) {
		super(soundCanvasWrapper, valueOwner);
		setLayout(new GridLayout(1 , 2, 2, 2));
		index.setPreferredSize(new Dimension(30, 10));
		add(index);
		valueAt.setPreferredSize(new Dimension(200, 10));
		add(valueAt);
		((DefaultEditor)valueAt.getEditor()).getTextField().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() >= 2) {
					long sp = Context.getSongPositionPointer() - soundCanvasWrapper.getFrameFrom();
					valueAt.setValue(sp );
				}
			}
		});
		
		valueChanged = new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				long[] current = getCurrentValue();
				System.err.println(Arrays.toString(current));
				int i = (int) index.getValue() - 1;
				long[] newv;
				if(i >= current.length) {
					newv = new long[i + 1];
					
				}
				else {
					newv = new long[current.length];
				}
				System.arraycopy(current, 0, newv, 0, current.length);
				
				newv[i] = ((Number) valueAt.getValue()).longValue();
				System.err.println(Arrays.toString(newv));
				setNewValue(newv);
			}
		};
		indexChanged = new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				long[] current = getCurrentValue();
				System.err.println(Arrays.toString(current));
				int i = (int) index.getValue() - 1;
				valueAt.setValue(i <current.length ? current[i] : 0);
			}
		};
	}

	@Override
	public long[] getCurrentValue() {
		// TODO Auto-generated method stub
		return super.getCurrentValue();
	}

	@Override
	public void setCurrentValue(long[] currentValue) {
		super.setCurrentValue(currentValue);
	}
	@Override
	public void activate() {
		index.addChangeListener(indexChanged);
		valueAt.addChangeListener(valueChanged);
		indexChanged.stateChanged(null);
	}
}
