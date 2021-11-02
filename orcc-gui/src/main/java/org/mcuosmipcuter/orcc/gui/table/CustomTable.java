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
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.annotation.Annotation;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.api.soundvis.ChangesIcon;
import org.mcuosmipcuter.orcc.api.soundvis.NumberMeaning;
import org.mcuosmipcuter.orcc.api.soundvis.PropertyListener;
import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.api.soundvis.TimedChange;
import org.mcuosmipcuter.orcc.api.soundvis.Unit;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.WidgetUtil;

/**
 * Custom table for GUI handling of the soundvis canvas layers
 * @author Michael Heinzelmann
 */
public class CustomTable extends JPanel implements Context.Listener{

	private static final long serialVersionUID = 1L;
	
	private CustomTableListener tableListener;
	private boolean moveEnabled = true;
	
	private final Color HIGHLIGHT =  new Color(240, 240, 240);
	private final Color SHADOW = new Color(165, 165, 185);
	
	private final Color UNSELECTED = Color.WHITE;
	private final Color SELECTED = new Color(215, 240, 215);
	private final Color MOVE = new Color(215, 215, 240);
	private final Color REMOVE = Color.RED;
	
	int iconWidth = 84;
	int iconHeight = 32;
	
	// specialized internal class for handling the mouse actions
	private class Mover extends MouseAdapter {
		
		private final JComponent container;
		private final  Row owner;
		private final Cursor moveCursor;
		private final Component grabComponent;
		 
		private  Row source;
		private  Row target;
		private Cursor selectCursor;

		private final Color originalBackground;
		
		
		private Mover(JComponent container, Row owner, Cursor moveCursor, final Component grabComponent, Component selectComponent) {

			this.container = container;
			this.owner = owner;
			this.moveCursor = moveCursor;
			this.originalBackground = owner.getBackground();
			this.grabComponent = grabComponent;
			selectComponent.addMouseListener(new MouseAdapter() {
				boolean mouseDown;
				@Override
				public void mousePressed(MouseEvent e) {
					mouseDown = true;
					owner.setBackground(SELECTED);
					owner.getSoundCanvasWrapper().setSelected(true);
					tableListener.rowSelected(true);
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					mouseDown = false;
					selectComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));	
					owner.setBackground(originalBackground);
					owner.getSoundCanvasWrapper().setSelected(false);
					tableListener.rowSelected(false);
				}
				@Override
				public void mouseEntered(MouseEvent arg0) {
					if(!mouseDown) {
						selectCursor = selectComponent.getCursor();
						selectComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
				}
				@Override
				public void mouseExited(MouseEvent e) {
					if(!mouseDown) {
						selectComponent.setCursor(selectCursor);
					}
				}
				
			});
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(!moveEnabled) {
				return;
			}
			grabComponent.setCursor(Mover.this.moveCursor);	
			final Component oldTarget = target;
			Component c = container.getComponentAt(owner.getX() + e.getX(), owner.getY() + e.getY());
			if(c instanceof Row) {
				target =  (Row)c;
			}
			
			if(oldTarget != null && oldTarget != target) {
				oldTarget.setBackground(originalBackground);
				if(target != null) {
					move();
				}
			}

			if(source != null) {
				source.setBackground(MOVE);
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
			
			owner.setBackground(MOVE);
			owner.getSoundCanvasWrapper().setSelected(true);
			source = owner;

			if(moveEnabled) {
				grabComponent.setCursor(moveCursor);	
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

			grabComponent.setCursor(container.getCursor());	

			if(!moveEnabled) {
				return;
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
		int width = iconWidth;
		int height = iconHeight;
		
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
		
		final Row row = new Row(soundCanvasWrapper, UNSELECTED, 8);
		row.setPreferredSize(new Dimension(630, 78));
		row.setLayout(new BorderLayout());
		row.setBackground(Color.WHITE);	
		
		final JLabel layer = new JLabel();
		layer.setOpaque(false);
		layer.setName("icon_label");
		layer.setPreferredSize(new Dimension(iconWidth, iconHeight));
		layer.setToolTipText("show in timeline");

		soundCanvasWrapper.setIconImage(getImage());
		soundCanvasWrapper.addPropertyChangeListener(new PropertyListener() {
			
			@Override
			public void propertyWritten(Field field) {
				String name = field.getName();
				Context.canvasPropertyWritten(name, soundCanvasWrapper.getSoundCanvas());
				if(field.isAnnotationPresent(TimedChange.class) || field.isAnnotationPresent(ChangesIcon.class)) {
					soundCanvasWrapper.updateUI(iconWidth, iconHeight, (Graphics2D) soundCanvasWrapper.getIconImage().getGraphics());
					layer.setIcon(new ImageIcon(soundCanvasWrapper.getIconImage()));
				}
			}
		});
		
		
		JLabel grab = new JLabel() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				int num = 6;
				int n = 0;
				int marg = 3;

				for (int i = 3; i < getHeight(); i += 6) {
					int d = i < getHeight() / 2 ? -3 : 3;
					if (i + d < 1) {
						continue;
					}
					n++;
					if (n > num) {
						break;
					}
					g.setColor(HIGHLIGHT);
					g.fillPolygon(new int[] { marg, getWidth() / 2, getWidth() - marg },
								  new int[] { i, i + d, i }, 3);
					g.setColor(SHADOW);
					g.drawLine(marg, i, getWidth() - marg, i);

				}
			}
		};
		grab.setPreferredSize(new Dimension(20, 36));
		grab.setToolTipText("move layer");
		grab.setOpaque(true);
		row.add(grab, BorderLayout.WEST);
		
		
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
		fromFrame.setToolTipText("frame from");
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
		final JSpinner toFrame = WidgetUtil.getIntegerSpinner(0, 0, Integer.MAX_VALUE, 1, Unit.OTHER, new NumberMeaning() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return NumberMeaning.class;
			}
			@Override
			public int[] numbers() {
				return new int[] {0};
			}
			@Override
			public String[] meanings() {
				return new String[] {"end"};
			}
		});
		toFrame.setToolTipText("frame to");
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
		expandButton.setToolTipText("expand properties");
		
		expandButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				row.toggleProperties();
				expandButton.setText(row.isPanelVisible() ? " - " : " + ");
				expandButton.setToolTipText(row.isPanelVisible() ? "collapse properties" : "expand properties");
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
		
		final JCheckBox xorCheckBox = new JCheckBox(soundCanvasWrapper.isXor() ? "xor": "std", soundCanvasWrapper.isXor());
		xorCheckBox.setToolTipText("paint standard or xor");
		xorCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				soundCanvasWrapper.setXor(xorCheckBox.isSelected());
				xorCheckBox.setText(xorCheckBox.isSelected() ? "xor" : "std");
				Context.touch();
			}
		});

		int boxH = 24;
		final JPanel timeline = new JPanel();
		
        FlowLayout fll = new FlowLayout();
        fll.setVgap(10);
        fll.setHgap(10);
		timeline.setLayout(fll);
		
		timeline.add(layer);

		timeline.add(expandButton);
		timeline.add(showCheckBox);

		fromFrame.setPreferredSize(new Dimension(80, boxH));
		timeline.add(fromFrame);
		
		toFrame.setPreferredSize(new Dimension(80, boxH));
		timeline.add(toFrame);

		transparency.setPreferredSize(new Dimension(58, boxH));
		timeline.add(transparency);

		xorCheckBox.setPreferredSize(new Dimension(58, boxH));
		timeline.add(xorCheckBox);

		
		BufferedImage image = getImage();
		soundCanvasWrapper.updateUI(iconWidth, iconHeight, image.createGraphics());
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
					row.setBackground(REMOVE);
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
		
		Mover mgm = new Mover(this, row, Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR), grab, layer);
		grab.addMouseMotionListener(mgm);
		grab.addMouseListener(mgm);
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
					soundCanvasWrapper.updateUI(iconWidth, iconHeight, (Graphics2D) soundCanvasWrapper.getIconImage().getGraphics());
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
