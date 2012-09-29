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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.defaultcanvas.SolidColor;
import org.mcuosmipcuter.orcc.soundvis.gui.CanvasPropertyPanel;
import org.mcuosmipcuter.orcc.soundvis.model.SoundCanvasWrapperImpl;


public class CustomTableMain {
	
	private static BufferedImage getImage() {
		int width = 60;
		int height = 30;
		
		BufferedImage frameImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = frameImage.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.setPaint(new RadialGradientPaint(new Point(0, 0), height, new float[] {0.0f, 0.5f}, new Color[] {Color.WHITE, Color.GRAY}, CycleMethod.REFLECT));
		graphics.fillRect(0, 0, width, height);
		
		return frameImage;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        JFrame frame = new JFrame("Custom Table");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JPanel container = new JPanel();
        container.setOpaque(true); //content panes must be opaque
        container.setLayout(new BorderLayout());
        
        final JPanel table = new JPanel();
        table.setLayout(new GridLayout(0, 1, 1, 1));

        JButton add = new JButton("add");
        add.addActionListener(new ActionListener() {
			 
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final SoundCanvasWrapper soundCanvasWrapper = new SoundCanvasWrapperImpl(new org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Image());
				 
				final Row row = new Row( soundCanvasWrapper);
				
				row.setLayout(new BorderLayout());
				row.setBorder(new EtchedBorder());
				row.setBackground(Color.WHITE);
				JLabel layer = new JLabel(soundCanvasWrapper.getDisplayName() + table.getComponentCount());
				layer.setPreferredSize(new Dimension(180, 30));
				row.add(layer, BorderLayout.WEST);
				
				final JPanel timeline = new JPanel();
				timeline.setLayout(new GridLayout(1, 0, 1, 1));
				
				JLabel fromFrame = new JLabel("from 0");
				JLabel toFrame = new JLabel("to 6427");
				final JButton editor = new JButton("...");
				
				editor.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						
						int res = JOptionPane.showConfirmDialog(null, row.getCanvasPropertyPanel());
						BufferedImage image = getImage();
						soundCanvasWrapper.drawCurrentIcon(60, 30, image.createGraphics());
						editor.setIcon(new ImageIcon(image));
					}
				});
				
				timeline.add(editor);
				timeline.add(fromFrame);
				timeline.add(toFrame);

				
				editor.setIcon(new ImageIcon(getImage().getScaledInstance(60, 30, Image.SCALE_FAST)));

				final JLabel remove = new JLabel(" x ");
				row.add(timeline);
				
				row.add(remove, BorderLayout.EAST);
				remove.addMouseListener(new MouseAdapter() {
					Color originalBackground;
					@Override
					public void mousePressed(MouseEvent e) {
						originalBackground = row.getBackground();
						row.setBackground(Color.RED);
					}

					@Override
					public void mouseReleased(MouseEvent e) {
						if(originalBackground != null) {
							row.setBackground(originalBackground);
						}
					}

					@Override
					public void mouseClicked(MouseEvent e) {
						table.remove(row);
						table.revalidate();
					}
				});
				
				MouseGrabAndMove mgm = new MouseGrabAndMove(table, row, Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
				row.addMouseMotionListener(mgm);
				row.addMouseListener(mgm);

				table.add(row);
				table.revalidate();
			}
		});
        //JScrollPane sp = new JScrollPane(t);
        container.add(table, BorderLayout.NORTH);     
        container.add(add, BorderLayout.SOUTH);
        
        frame.setContentPane(container);

        //Display the window.
        frame.pack();
        frame.setVisible(true);// TODO Auto-generated method stub

	}

}
