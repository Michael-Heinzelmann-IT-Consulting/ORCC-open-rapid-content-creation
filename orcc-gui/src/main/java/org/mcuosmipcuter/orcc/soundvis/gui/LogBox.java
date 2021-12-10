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
package org.mcuosmipcuter.orcc.soundvis.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.function.Function;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;

/**
 * @author Michael Heinzelmann
 *
 */
public class LogBox implements Function<String, Void>{

	private JTextArea ta = new JTextArea(20, 160);
	JSpinner sizeSpinner;
	JCheckBox showTime = new JCheckBox();
	private JScrollPane sp = new JScrollPane(ta);
	private JDialog jd;
	SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.S");

	Deque<String> deque = new ArrayDeque<String>();
	/**
	 * 
	 */
	public LogBox(int size, Component parent) {
		sizeSpinner = new JSpinner(new SpinnerNumberModel(size, 0, Integer.MAX_VALUE, 1));
		ta.setEditable(false);
		showTime.setSelected(true);
		Font font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
		ta.setFont(font);
		JToolBar toolBar = new JToolBar();
		sizeSpinner.setPreferredSize(new Dimension(200, 24));
		sizeSpinner.setMaximumSize(new Dimension(80, 24));
		toolBar.add(new JLabel("buffer size: "));
		toolBar.add(sizeSpinner);
		toolBar.add(new JLabel("   show time: "));
		toolBar.add(showTime);
		toolBar.setFloatable(false);
		Object[] array = {sp, toolBar}; 
		JOptionPane jp = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE);		
		jd = jp.createDialog(parent, "application log");
	}
	
	public void showLog(boolean modal) {
		try {
			update();
			ta.setCaretPosition(ta.getText().length());
			jd.setModal(modal);
			jd.setAlwaysOnTop(!modal);
			jd.setVisible(true);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	private void update() {
		ta.setText(deque.toString());
		ta.setCaretPosition(ta.getText().length());
		sp.revalidate();
	}
	@Override
	public Void apply(String msg) {
		final int size = getSize();

		while(deque.size() > 0 && deque.size() >= size) {
			deque.removeFirst();
		}
		if(size > 0) {
			String time = showTime.isSelected() ? dateFormat.format(new Date()) + " " : "";
			deque.add(time + msg + "\n");
		}

		if(jd.isVisible()) {
			update();
		}
		return null;
	}

	public int getSize() {
		return ((Number)sizeSpinner.getValue()).intValue();
	}

}
