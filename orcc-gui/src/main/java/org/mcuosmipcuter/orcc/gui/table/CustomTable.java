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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyListener;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.TimedChange;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;

/**
 * Custom table for GUI handling of the soundvis canvas layers
 * @author Michael Heinzelmann
 */
public class CustomTable extends JPanel implements Context.Listener{

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
		
		
		private Mover(JComponent container, Row owner, Cursor moveCursor, final Component grabComponent) {
			this.container = container;
			this.owner = owner;
			this.moveCursor = moveCursor;
			this.originalBackground = owner.getBackground();
			grabComponent.addMouseListener(new MouseAdapter() {
				boolean mouseDown;
				@Override
				public void mousePressed(MouseEvent e) {
					mouseDown = true;
					if(moveEnabled) {
						grabComponent.setCursor(Mover.this.moveCursor);	
					}
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					mouseDown = false;
					grabComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));	
				}
				@Override
				public void mouseEntered(MouseEvent arg0) {
					if(!mouseDown) {
						cursor = grabComponent.getCursor();
						grabComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
				}
				@Override
				public void mouseExited(MouseEvent e) {
					if(!mouseDown) {
						grabComponent.setCursor(cursor);
					}
				}
				
			});
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
					GridBagConstraints gc = new GridBagConstraints();
					gc.gridwidth = GridBagConstraints.REMAINDER; //end row
					for(Component c : list) {
						container.add(c, gc);
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
				source.getSoundCanvasWrapper().setSelected(owner.isPanelVisible());
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
		setLayout(new GridBagLayout());
	}
	private  BufferedImage getImage() {
		int width = 120;
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
		row.setPreferredSize(new Dimension(640, 64));
		row.setLayout(new BorderLayout());
		TitledBorder tb = new TitledBorder(new LineBorder(Color.WHITE, 8));
		tb.setTitle(soundCanvasWrapper.getDisplayName());
		tb.setTitlePosition(TitledBorder.TOP);
		row.setBorder(tb);
		row.setBackground(Color.WHITE);
		final JLabel layer = new JLabel(/*soundCanvasWrapper.getDisplayName()*/);
		layer.setOpaque(true);
		layer.setName("icon_label");
		layer.setPreferredSize(new Dimension(140, 16));
		layer.setToolTipText("edit or move " + soundCanvasWrapper.getDisplayName());
		soundCanvasWrapper.setIconImage(getImage());
		soundCanvasWrapper.addPropertyChangeListener(new PropertyListener() {
			
			@Override
			public void propertyWritten(Field field) {
				String name = field.getName();
				Context.canvasPropertyWritten(name, soundCanvasWrapper.getSoundCanvas());
				if(field.isAnnotationPresent(TimedChange.class) || field.isAnnotationPresent(ChangesIcon.class)) {
					soundCanvasWrapper.updateUI(120, 30, (Graphics2D) soundCanvasWrapper.getIconImage().getGraphics());
					layer.setIcon(new ImageIcon(soundCanvasWrapper.getIconImage()));
				}
			}
		});

		row.add(layer, BorderLayout.WEST);
		
		final JPanel timeline = new JPanel();
		timeline.setLayout(new GridLayout(1, 0, 6, 6));
		timeline.setBorder(new LineBorder(timeline.getBackground(), 4));
		
		final JCheckBox showCheckBox = new JCheckBox(soundCanvasWrapper.isVisible() ? "on" : "off", soundCanvasWrapper.isVisible());
		showCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				soundCanvasWrapper.setVisible(showCheckBox.isSelected());
				showCheckBox.setText(showCheckBox.isSelected() ? "on" : "off");
				Context.touch();
			}
		});
		
		SpinnerNumberModel modelFrom = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
		final JSpinner fromFrame = new JSpinner(modelFrom);
		fromFrame.setToolTipText("from");
		fromFrame.setValue((int)soundCanvasWrapper.getFrameFrom());
		fromFrame.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				soundCanvasWrapper.setFrameFrom(((Number)fromFrame.getValue()).longValue());
				tableListener.frameSet();
				Context.touch();
			}
		});
		((DefaultEditor)fromFrame.getEditor()).getTextField().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() >= 2) {
					fromFrame.setValue((int)tableListener.getFrameSelected());
					Context.touch();
				}
			}
		});
		SpinnerNumberModel modelTo = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
		final JSpinner toFrame = new JSpinner(modelTo);
		toFrame.setToolTipText("to");
		toFrame.setValue(soundCanvasWrapper.isFrameToAuto() ? (int)0 : (int)soundCanvasWrapper.getFrameTo());
		toFrame.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				soundCanvasWrapper.setFrameTo(((Number)toFrame.getValue()).longValue());
				tableListener.frameSet();
				Context.touch();
			}
		});
		((DefaultEditor)toFrame.getEditor()).getTextField().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() >= 2) {
					toFrame.setValue((int)tableListener.getFrameSelected());
					Context.touch();
				}
			}
		});
		final JButton expandButton = new JButton(" + ");
		
		expandButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				row.toggleProperties();
				expandButton.setText(row.isPanelVisible() ? " - " : " + ");
				soundCanvasWrapper.setEditorOpen(row.isPanelVisible());
				row.revalidate();
				CustomTable.this.revalidate();
				Context.touch();
			}
		});
		
		SpinnerNumberModel modelTransparency = new SpinnerNumberModel(100, 0, 100, 1);
		final JSpinner transparency = new JSpinner(modelTransparency);
		transparency.setToolTipText("repaint transparency");
		transparency.setValue(soundCanvasWrapper.getTransparency());
		transparency.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				soundCanvasWrapper.setTransparency((((Number)transparency.getValue()).intValue()));
				Context.touch();
			}
		});
		
		SpinnerNumberModel modelThreshold = new SpinnerNumberModel(0, 0, 100, 1);
		final JSpinner threshold = new JSpinner(modelThreshold);
		threshold.setToolTipText("repaint threshold");
		threshold.setValue(soundCanvasWrapper.getRepaintThreshold());		
		threshold.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				soundCanvasWrapper.setRepaintThreshold(((((Number)threshold.getValue()).intValue())));
				Context.touch();
			}
		});
		final JCheckBox xorCheckBox = new JCheckBox(soundCanvasWrapper.isXor() ? "xor": "std", soundCanvasWrapper.isXor());
		xorCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				soundCanvasWrapper.setXor(xorCheckBox.isSelected());
				xorCheckBox.setText(xorCheckBox.isSelected() ? "xor" : "std");
				Context.touch();
			}
		});
		
		timeline.add(expandButton);
		timeline.add(showCheckBox);
		timeline.add(fromFrame);
		timeline.add(toFrame);
		timeline.add(transparency);
		timeline.add(threshold);
		timeline.add(xorCheckBox);

		BufferedImage image = getImage();
		soundCanvasWrapper.updateUI(120, 30, image.createGraphics());
		layer.setIcon(new ImageIcon(image));

		final JLabel remove = new JLabel(" x ");
		remove.setOpaque(true);
		remove.setBackground(new Color(240, 225, 225));
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
					row.passivatePropertyEditors();
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
		
		Mover mgm = new Mover(this, row, Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR), layer);
		layer.addMouseMotionListener(mgm);
		layer.addMouseListener(mgm);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER; //end row

		this.add(row, c);
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
	
	@Override
	public void contextChanged(PropertyName propertyName) {
		if (Context.PropertyName.SoundCanvasListCleared == propertyName) {
			for (Component c : getComponents()) {
				if (c instanceof Row) {
					((Row)c).passivatePropertyEditors();
					remove(c);
				}
			}
			repaint();
		}
		if (PropertyName.AudioInputInfo.equals(propertyName) || PropertyName.VideoFrameRate.equals(propertyName)) {
			for (Component c : getComponents()) {
				if (c instanceof Row) {
					SoundCanvasWrapper soundCanvasWrapper = ((Row) c).getSoundCanvasWrapper();
					if (soundCanvasWrapper.isFrameToAuto()) {
						soundCanvasWrapper.setFrameTo(0);
						tableListener.frameSet();
					}
				}
			}
		}

	}
	@Override
	public void updateUI(SoundCanvas soundCanvas) {
		for (Component c : getComponents()) {
			if (c instanceof Row) {
				SoundCanvasWrapper soundCanvasWrapper = ((Row) c).getSoundCanvasWrapper();
				if (soundCanvasWrapper.getSoundCanvas() == soundCanvas) {
					soundCanvasWrapper.updateUI(120, 30, (Graphics2D) soundCanvasWrapper.getIconImage().getGraphics());
					for (Component ci : ((Row) c).getComponents()) {
						if("icon_label".equals(ci.getName())){
							((JLabel)ci).setIcon(new ImageIcon(soundCanvasWrapper.getIconImage()));
						}
					}
				}
			}
		}
	}
	
}
