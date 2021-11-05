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
package org.mcuosmipcuter.orcc.soundvis.gui.widgets.properties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import org.mcuosmipcuter.orcc.gui.util.GraphicsUtil;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.ImageStore;
import org.mcuosmipcuter.orcc.soundvis.ImageStore.Key;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.defaultcanvas.model.Slide;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.ImagePreview;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Panel for {@link BufferedImage} properties using a
 * {@link FileDialogActionListener} that pops up on pressing the button.
 * 
 * @author Michael Heinzelmann
 */
public class MultiImagePropertyPanel extends PropertyPanel<Slide[]> {

	private final class FileLoadActionListener implements ActionListener {
		private boolean append;

		public FileLoadActionListener(boolean append) {
			this.append = append;
		}
		JFileChooser chooser = new JFileChooser() {
			private static final long serialVersionUID = 1L;

			@Override
			public void approveSelection() {
				IOUtil.log(System.currentTimeMillis() + " chooser approve ");
				super.approveSelection();
			}
		};
		@Override
		public void actionPerformed(ActionEvent e) {

			chooser.setMultiSelectionEnabled(true);
			chooser.setAccessory(new ImagePreview(chooser));
			IOUtil.log(System.currentTimeMillis() + " chooser return " + e);
			Context.beforePropertyUpdate(name);

			int returnVal = chooser.showDialog(null, "select files");
			if (returnVal == JFileChooser.APPROVE_OPTION) {

				File[] selectedFiles = chooser.getSelectedFiles();
				Slide[] slides = new Slide[selectedFiles.length];
				for (int i = 0; i < selectedFiles.length; i++) {
					slides[i] = new Slide();
					IOUtil.log(System.currentTimeMillis() + " reading image " + i);
					Context.progressUpdate(" loading image " + i);
					slides[i].setText(selectedFiles[i].getName());

					BufferedImage image;
					Key key = new Key(selectedFiles[i]); // the original not rotated and not mirror
					Image fromStore = ImageStore.getOrLoadImage(key);
					if (fromStore instanceof BufferedImage) {
						slides[i].setImage(key, fromStore);
					} else {
						image = ImageStore.createPlaceHolderImage(selectedFiles[i]);							
						slides[i].setImage(key, image);
						ImageStore.addImage(key, image);
					}
				}
				IOUtil.log(System.currentTimeMillis() + " before setNewValue " + e);
				Slide[] existingSlides = getCurrentValue();
				if (append && existingSlides != null) {

					Slide[] newValue = new Slide[existingSlides.length + slides.length];
					int j = 0, k = 0;
					for (int i = 0; i < newValue.length; i++) {
						if (j < existingSlides.length) {
							newValue[i] = existingSlides[j];
							j++;
						} else if (k < slides.length) {
							newValue[i] = slides[k];
							k++;
						}
					}
					setNewValue(newValue);
				} else {
					setNewValue(slides);
				}
				IOUtil.log(System.currentTimeMillis() + " after setNewValue " + e);
			} else {
				Context.cancelPropertyUpdate(name);
			}
		}
	}

	private static final long serialVersionUID = 1L;
	private JButton fileButton = new JButton("+");
	private JButton editButton = new JButton("...");
	private JButton addFileButton = new JButton("+");
	JPanel valueSelect = new JPanel();
	JScrollPane scrollPane;

	private JPanel commands = new JPanel();
	private JPanel imagebar = new JPanel();
	private Color origBackground;
	Popup popup = null;
	Popup editPopup = null;
	boolean isPopupShowing;
	final Set<JButton> jbuttons = new LinkedHashSet<JButton>();


	/**
	 * Constructor
	 * 
	 * @param soundCanvas the canvas to work with
	 */
	public MultiImagePropertyPanel(final SoundCanvasWrapper soundCanvasWrapper, Object valueOwner) {
		super(soundCanvasWrapper, valueOwner);

		valueSelect.setLayout(new BorderLayout(2, 2));
		valueSelect.setBackground(Color.BLACK);
		addFileButton.setPreferredSize(new Dimension(80, 80));
		addFileButton.setFont(getFont().deriveFont(48.0f));

		imagebar.setBackground(Color.BLACK);

		JButton close = new JButton("close");
		close.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				hideSlideEditPopup();
				hidePopup();
			}
		});
		valueSelect.add(close, BorderLayout.NORTH);
		scrollPane = new JScrollPane(imagebar);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		valueSelect.add(scrollPane);
		valueSelect.setPreferredSize(new Dimension(500, 120));

		editButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showPopup();
			}
		});

		commands.setLayout(new GridLayout(1, 2, 0, 0));
		commands.add(fileButton);
		commands.add(editButton);
		add(commands);

		fileButton.addActionListener(new FileLoadActionListener(false));
		addFileButton.addActionListener(new FileLoadActionListener(true));
		setCurrentValue(new Slide[] {});
	}

	private void showPopup() {
		Point loc = editButton.getLocationOnScreen();
		popup = PopupFactory.getSharedInstance().getPopup(this, valueSelect, loc.x, loc.y);
		editButton.setEnabled(false);
		commands.setOpaque(true);
		commands.setBackground(Color.YELLOW);
		origBackground = getBackground();
		setBackground(Color.YELLOW);
		popup.show();
		isPopupShowing = true;
	}

	private void hidePopup() {
		if(popup != null) {
			popup.hide();
			popup = null;
		}
		editButton.setEnabled(true);
		setBackground(origBackground);
		commands.setOpaque(false);
		isPopupShowing = false;
	}
	
	private void showSlideEditPopup(JButton ib, Slide slide){
		hideSlideEditPopup(); // hide/remove others
		Point loc = ib.getLocationOnScreen();
		JPanel editPanel = new JPanel();	
		editPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		GridBagLayout gridbag = new GridBagLayout();
		
		editPanel.setLayout(gridbag);
		GridBagConstraints gc = new GridBagConstraints();
		
		gc.insets = new Insets(3, 3, 3, 3);
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = GridBagConstraints.CENTER;
		
		gc.gridwidth = GridBagConstraints.REMAINDER;
		JButton closeButton = new JButton("close");
		gridbag.setConstraints(closeButton, gc);
		editPanel.add(closeButton, gc);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hideSlideEditPopup();			
			}
		});

		JTextPane text = new JTextPane();

		text.setPreferredSize(new Dimension(280, 56));
		text.setEnabled(true);
		text.setEditable(true);
		text.setText(slide.getText());

		JButton textButton = new JButton(slide.getText());
		textButton.setPreferredSize(new Dimension(280, 28));
		gridbag.setConstraints(textButton, gc);
		editPanel.add(textButton, gc);
		textButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JScrollPane sp = new JScrollPane(text);
				Object[] array = {slide.getDisplayKey(), sp}; 
				int res = JOptionPane.showConfirmDialog(null, array, "set value for text", 
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if(res == JOptionPane.OK_OPTION) {
					String oldText = slide.getText();
					slide.setText(text.getText());
					textButton.setText(text.getText());
					MultiImagePropertyPanel.this.changeSession(slide.getId() + "::text", oldText, text.getText());
				}	
				else {
					text.setText(slide.getText());
				}
			}
		});

		JPanel imagePanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				
				super.paint(g);
				Image image = slide.getImage();
				g.drawImage(GraphicsUtil.getScaledInstanceKeepRatio(image, 300, Image.SCALE_FAST), 10, 10, null);
				setToolTipText(slide.getText() + ": " + image.getHeight(null) + " x " + image.getWidth(null));
			}};
		imagePanel.setPreferredSize(new Dimension(320, 320));
		imagePanel.setBackground(Color.BLACK);

		gridbag.setConstraints(imagePanel, gc);
		
		editPanel.add(imagePanel, gc);

		gc.gridwidth = 2;
		JButton moveLeftButton = new JButton("< move");
		gridbag.setConstraints(moveLeftButton, gc);
		editPanel.add(moveLeftButton, gc);
		moveLeftButton.setEnabled(slide.getPosition() > 1);
		
		gc.gridwidth = GridBagConstraints.REMAINDER;
		
		JButton moveRightButton = new JButton("move >");
		gridbag.setConstraints(moveRightButton, gc);
		editPanel.add(moveRightButton, gc);
		moveRightButton.setEnabled(getCurrentValue() != null && slide.getPosition() < getCurrentValue().length);

		JButton rotateButton = new JButton("rotate");
		gridbag.setConstraints(rotateButton, gc);
		editPanel.add(rotateButton, gc);	
		
		rotateButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Key oldKey = slide.getKey();
				Key newKey = oldKey.rotateClockWise();
				updateSlideImage(slide, oldKey, newKey);
				imagePanel.repaint();
			}
		});
		
		JButton mirrorButton = new JButton("mirror");
		gridbag.setConstraints(mirrorButton, gc);
		editPanel.add(mirrorButton, gc);	
		
		mirrorButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Key oldKey = slide.getKey();
				Key newKey = oldKey.mirrorY();
				updateSlideImage(slide, oldKey, newKey);
				imagePanel.repaint();
			}
		});
		

		JButton removeButton = new JButton("remove");
		gridbag.setConstraints(removeButton, gc);
		editPanel.add(removeButton, gc);
		
		Rectangle screen = GraphicsUtil.getRootComponentOutline(MultiImagePropertyPanel.this);
		int lowLimit = screen.y + screen.height;
		editPanel.doLayout();
		int extentY = loc.y + editPanel.getPreferredSize().height;
		// debug: System.err.println("extentY " + extentY  + " lowLimit " + lowLimit);
		int yToUse = extentY < lowLimit ? loc.y : loc.y - (extentY - lowLimit) - 10;
		
		editPopup = PopupFactory.getSharedInstance().getPopup(this, editPanel, loc.x, yToUse);
		moveLeftButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				hideSlideEditPopup();
				moveSlide(slide, -1);
			}
		});
		moveRightButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				hideSlideEditPopup();
				moveSlide(slide, 1);
			}
		});
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hideSlideEditPopup();
				removeSlide(slide);
			}
		});		
		
		editPopup.show();
		
	}
	private void hideSlideEditPopup() {
		if(editPopup != null) {
			editPopup.hide();
			editPopup = null;
		}
		
	}
	private void updateSlideImage(Slide slide, Key oldKey, Key newKey) {
		BufferedImage newImage = ImageStore.transformImage(newKey);	
		slide.setImage(newKey, newImage);
		JButton ib = (JButton) jbuttons.toArray()[slide.getPosition() - 1];
		ib.setIcon(new ImageIcon(ImageStore.getOrLoadScaledImage(slide.getKey(), 80, 80)));
		ib.setSelectedIcon(new ImageIcon(ImageStore.getOrLoadScaledImage(slide.getKey(), 60, 60)));
		Context.setSongPositionPointer(Context.getSongPositionPointer());
		changeSession(slide.getId() + "::key", oldKey, newKey);
	}
	
	private void removeSlide(Slide slide) {
		Slide[] currentValue = getCurrentValue();
		if(currentValue == null || currentValue.length == 0) {
			IOUtil.log("No slides to remove!");
			return;
		}
		int positionToRemove = -1;
		for(Slide s : currentValue) {
			if(s == slide) {
				positionToRemove = s.getPosition();
			}
		}
		if(positionToRemove == -1) {
			IOUtil.log("slide " + slide + " not found!");
			return;
		}
		IOUtil.log("removing position " + positionToRemove);
		Slide[] newValue = new Slide[currentValue.length - 1];
		int idx = 0;
		for(Slide s : currentValue) {
			if(s != slide) {
				newValue[idx] = s;
				idx++;
			}
		}
		setNewValue(newValue);
	}
	private void moveSlide(Slide slide, int step) {
		Slide[] currentValue = getCurrentValue();
		if(currentValue == null || currentValue.length == 0) {
			IOUtil.log("No slides to move!");
			return;
		}
		int oldIdx = -1;
		int idx = 0;
		for(Slide s : currentValue) {
			if(s == slide) {
				oldIdx = idx;
			}
			idx++;
		}
		if(oldIdx == -1) {
			IOUtil.log("slide " + slide + " not found!");
			return;
		}
		int newIdx = oldIdx + step;
		IOUtil.log("moving position " + oldIdx + " to " + newIdx);
		Slide[] newValue = new Slide[currentValue.length];
		int i = 0;
		for(Slide s : currentValue) {
			if(i == newIdx) {
				newValue[i] = slide;
			}
			else if(i == oldIdx) {
				newValue[i] = currentValue[newIdx];
			}
			else {
				newValue[i] = s;
			}
			i++;
		}
		setNewValue(newValue);
		int bidx = 0;
		for(JButton jb : jbuttons) {
			jb.setSelected(bidx++ == newIdx);
		}
	}
	

	@Override
	public void setCurrentValue(Slide[] currentValue) {
		int pos = 0;
		if (currentValue != null) {
			for (Slide slide : currentValue) {
				slide.setPosition(++pos);
			}
		}
		super.setCurrentValue(currentValue);
		imagebar.removeAll();
		jbuttons.clear();
		JPanel images = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		images.setLayout(gridbag);
		images.setBackground(Color.BLACK);

		int COLS = 6;
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridwidth = COLS;
		gc.fill = GridBagConstraints.BOTH;
		int col = 1;
		if (currentValue != null && currentValue.length > 0) {
			int rows = (currentValue.length + 1) / COLS;
			rows = (currentValue.length + 1) % COLS == 0 ? rows : rows + 1;
			gc.gridheight = rows;
			valueSelect.setPreferredSize(new Dimension(500, Math.max(rows * (80 + 20) + 6, 130)));
			for (final Slide slide : currentValue) {
				JButton ib = new JButton() {
					private static final long serialVersionUID = 1L;

					@Override
					public void setSelected(boolean b) {
						super.setSelected(b);
						setBackground(b ? Color.RED : Color.YELLOW);
					}
				};

				Context.progressUpdate(" creating preview " + (jbuttons.size() + 1));
	
				ib.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						jbuttons.stream().forEach(jb -> jb.setSelected(jb == e.getSource()));
					}
				});
				ib.addMouseListener(new MouseListener() {
					@Override
					public void mouseReleased(MouseEvent e) {
						ib.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
					@Override
					public void mousePressed(MouseEvent e) {
						ib.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
					@Override
					public void mouseExited(MouseEvent e) {
					}
					@Override
					public void mouseEntered(MouseEvent e) {
					}
					@Override
					public void mouseClicked(MouseEvent e) {
						ib.setToolTipText(slide.getText());
						if(e.getButton() == MouseEvent.BUTTON3) {
							showSlideEditPopup(ib, slide);
						}
						else {
							hideSlideEditPopup();
						}
					}
				});
				ib.addMouseMotionListener(new MouseMotionListener() {
					@Override
					public void mouseMoved(MouseEvent e) {
					}
					@Override
					public void mouseDragged(MouseEvent e) {
						ib.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
				});

				ib.setBackground(Color.YELLOW);
				ib.setBorder(new LineBorder(Color.BLACK, 2));
				ImageIcon icon = new ImageIcon(ImageStore.getOrLoadScaledImage(slide.getKey(), 80, 80));
				ib.setPreferredSize(new Dimension(80, 80));
				ib.setIcon(icon);

				ib.setSelectedIcon(new ImageIcon(ImageStore.getOrLoadScaledImage(slide.getKey(), 60, 60)));
				if (col == COLS) {
					gc.gridwidth = GridBagConstraints.REMAINDER; // end row
					col = 0;
				} else {
					gc.gridwidth = 1;
				}
				gc.weightx = 1.0;
				gridbag.setConstraints(ib, gc);
				images.add(ib, gc);
				jbuttons.add(ib);
				col++;

			}
		}

		gc.weightx = 1.0;
		gc.gridwidth = col == COLS ? GridBagConstraints.REMAINDER : 1; // end row
		gridbag.setConstraints(addFileButton, gc);
		images.add(addFileButton, gc);

		imagebar.add(images, BorderLayout.LINE_START);
		

		imagebar.revalidate();
		if (isPopupShowing) {
			hidePopup(); // remove old
			showPopup(); // show new
		}

		this.repaint();
	}

	@Override
	public void activate() {
		if(getCurrentValue() != null) {
			for(Slide slide : getCurrentValue()) {
				ImageStore.getOrLoadScaledImage(slide.getKey(), 80, 80);
				ImageStore.getOrLoadScaledImage(slide.getKey(), 60, 60);
			}
		}
	}

	@Override
	public void passivate() {
		hideSlideEditPopup();
		if(isPopupShowing) {
			hidePopup();
		}
	}
	
}
