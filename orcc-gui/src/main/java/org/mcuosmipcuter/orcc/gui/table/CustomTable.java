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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;

/**
 * Custom table for GUI handling of the soundvis canvas layers
 * @author Michael Heinzelmann
 */
public class CustomTable extends JPanel{

	private static final long serialVersionUID = 1L;
	
	private CustomTableListener tableListener;
	private boolean moveEnabled = true;
	
	// specialized internal class for handling the mouse actions
	private class Mover extends MouseAdapter {
		
		private final JComponent container;
		private final  Row owner;
		private final Cursor moveCursor;
		 
		private  Row source;
		private  Row target;
		private Cursor cursor;
		private final Color originalBackground;
		
		private Color selectColor = Color.GRAY;
		
		
		private Mover(JComponent container, Row owner, Cursor moveCursor) {
			this.container = container;
			this.owner = owner;
			this.moveCursor = moveCursor;
			this.originalBackground = owner.getBackground();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(!moveEnabled) {
				return;
			}
			final Component oldTarget = target;
			Component c = container.getComponentAt(owner.getX() + e.getX(), owner.getY() + e.getY());
			if(c instanceof Row) {
				target =  (Row)c;
			}
			
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
			final Row oldTarget = target;
			if(target != null && source != target) {
				oldTarget.setBackground(originalBackground);
				oldTarget.getSoundCanvasWrapper().setSelected(false);
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
					List<SoundCanvasWrapper> currentList = new ArrayList<SoundCanvasWrapper>();
					for(Component c : list) {
						container.add(c);
						SoundCanvasWrapper s = ((Row)c).getSoundCanvasWrapper();
						if(s != null) {
							currentList.add(s);
						}
					}
					Context.replaceCanvasList(currentList);
					container.revalidate();
				}

			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			
			owner.setBackground(selectColor);
			owner.getSoundCanvasWrapper().setSelected(true);
			source = owner;
			
			if(moveEnabled) {
				cursor = container.getCursor();
				container.setCursor(moveCursor);	
			}
			tableListener.rowSelected(true);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(source != null) {
				source.setBackground(originalBackground);
				source.getSoundCanvasWrapper().setSelected(false);
			}
			if(source != null || target != null) {
				tableListener.rowSelected(false);			
			}
			if(!moveEnabled) {
				return;
			}
			if( cursor != null) {
				container.setCursor(cursor);
			}
			if( cursor != null) {
				container.setCursor(cursor);
			}
			if(target != null) {
				target.setBackground(originalBackground);
				target.getSoundCanvasWrapper().setSelected(false);
			}

			target = null;
		}

	}
	
	/**
	 * Creates a new custom table, no parameters needed
	 */
	public CustomTable() {
		setLayout(new GridLayout(0, 1, 1, 1));
	}
	private  BufferedImage getImage() {
		int width = 60;
		int height = 30;
		
		BufferedImage frameImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = frameImage.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		
		return frameImage;
	}
	/**
	 * Adds a layer containing the given canvas
	 * @param soundCanvasWrapper the wrapped canvas 
	 */
	public void addLayer(final SoundCanvasWrapper soundCanvasWrapper) {
		final Row row = new Row(soundCanvasWrapper);
		row.setPreferredSize(new Dimension(360, 36));
		row.setLayout(new BorderLayout());
		row.setBorder(new EtchedBorder());
		row.setBackground(Color.WHITE);
		final JLabel layer = new JLabel(soundCanvasWrapper.getDisplayName());
		layer.setPreferredSize(new Dimension(180, 16));
		row.add(layer, BorderLayout.WEST);
		
		final JPanel timeline = new JPanel();
		timeline.setLayout(new GridLayout(1, 0, 6, 6));
		timeline.setBorder(new LineBorder(Color.WHITE, 4));
		
		final JCheckBox showCheckBox = new JCheckBox("on", soundCanvasWrapper.isVisible());
		showCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				soundCanvasWrapper.setVisible(showCheckBox.isSelected());
			}
		});
		
		SpinnerNumberModel modelFrom = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
		final JSpinner fromFrame = new JSpinner(modelFrom);
		fromFrame.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				soundCanvasWrapper.setFrameFrom(((Number)fromFrame.getValue()).longValue());
				tableListener.frameSet();
				
			}
		});
		((DefaultEditor)fromFrame.getEditor()).getTextField().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() >= 2) {
					fromFrame.setValue(tableListener.getFrameSelected());
				}
			}
		});
		SpinnerNumberModel modelTo = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
		final JSpinner toFrame = new JSpinner(modelTo);
		toFrame.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				soundCanvasWrapper.setFrameTo(((Number)toFrame.getValue()).longValue());
				tableListener.frameSet();
				
			}
		});
		((DefaultEditor)toFrame.getEditor()).getTextField().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() >= 2) {
					toFrame.setValue(tableListener.getFrameSelected());
				}
			}
		});
		final JButton editor = new JButton("...");
		
		editor.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, row.getCanvasPropertyPanel(), soundCanvasWrapper.getDisplayName(), 
						JOptionPane.PLAIN_MESSAGE);

				BufferedImage image = getImage();
				soundCanvasWrapper.drawCurrentIcon(60, 30, image.createGraphics());
				layer.setIcon(new ImageIcon(image));
			}
		});
		
		timeline.add(editor);
		timeline.add(showCheckBox);
		timeline.add(fromFrame);
		timeline.add(toFrame);

		BufferedImage image = getImage();
		soundCanvasWrapper.drawCurrentIcon(60, 30, image.createGraphics());
		layer.setIcon(new ImageIcon(image));

		final JLabel remove = new JLabel(" x ");
		row.add(timeline);
		
		row.add(remove, BorderLayout.EAST);
		remove.addMouseListener(new MouseAdapter() {
			Color originalBackground;
			@Override
			public void mousePressed(MouseEvent e) {
				originalBackground = row.getBackground();
				if(moveEnabled) {
					row.setBackground(Color.RED);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if(moveEnabled && originalBackground != null) {
					row.setBackground(originalBackground);
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if(moveEnabled) {
					CustomTable.this.remove(row);
					CustomTable.this.revalidate();
					List<SoundCanvasWrapper> currentList = new ArrayList<SoundCanvasWrapper>();
					for(Component c : getComponents()) {
						SoundCanvasWrapper s = ((Row)c).getSoundCanvasWrapper();
						if(s != null) {
							currentList.add(s);
						}
					}
					Context.replaceCanvasList(currentList);
				}
			}
		});
		
		Mover mgm = new Mover(this, row, Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
		row.addMouseMotionListener(mgm);
		row.addMouseListener(mgm);

		this.add(row);
		this.revalidate();
	}
	
	/**
	 * Sets whether the rows will react to mouse events, the popups for the property editor are not affected
	 */
	public void setEnabled(boolean enabled) {
		this.moveEnabled = enabled;
	}
	
	/**
	 * Set the listener, only one listener supported
	 * @param customTableListener the listener
	 */
	public void setListener(CustomTableListener customTableListener) {
		tableListener = customTableListener;
	}
}
