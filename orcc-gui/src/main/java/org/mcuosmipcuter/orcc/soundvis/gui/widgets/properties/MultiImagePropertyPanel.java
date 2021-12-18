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
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
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
	final JFrame frame;
	private final class ClassPathLoadActionListener implements ActionListener {
		private boolean append = true;

		@Override
		public void actionPerformed(ActionEvent e) {
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(0, 1));
			JCheckBox[] checkboxes = new JCheckBox [ImageStore.CLASSPATH_IMAGES.length];
			int idx = 0;
			for(Key key : ImageStore.CLASSPATH_IMAGES) {
				JPanel p = new JPanel();
				JLabel l = new JLabel();
				ImageIcon icon = new ImageIcon(ImageStore.getOrLoadScaledImage(key, CP_IMAGE_PREVIEW_SIZE, CP_IMAGE_PREVIEW_SIZE));
				l.setIcon(icon);
				JCheckBox c = new JCheckBox();
				checkboxes[idx++] = c;
				p.add(c);
				p.add(l);
				String path = key.getAbsolutePath();
				p.add(new JLabel(path.substring(path.lastIndexOf("/") + 1)));
				panel.add(p);
			}
			
			int res = JOptionPane.showConfirmDialog(frame ,panel, "built in images",
					JOptionPane.OK_CANCEL_OPTION);
			if (res == JOptionPane.OK_OPTION) {
				List<Key> selected = new ArrayList<>();
				for(int i = 0; i < checkboxes.length; i++) {
					if(checkboxes[i].isSelected()) {
						selected.add(ImageStore.CLASSPATH_IMAGES[i]);
					}
				}
				if(!selected.isEmpty()) {
					Slide[] slides = new Slide[selected.size()];
					int j = 0;
					for (Key key : selected) {
						slides[j] = new Slide();
						slides[j].setText(key.getAbsolutePath());
						slides[j].setKey(key);
						j++;
					}
					setOrAppendSlides(slides, append);
				}
			}
			
		}
	}

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

			int returnVal = chooser.showDialog(frame, "select files");
			if (returnVal == JFileChooser.APPROVE_OPTION) {

				File[] selectedFiles = chooser.getSelectedFiles();
				Slide[] slides = new Slide[selectedFiles.length];
				for (int i = 0; i < selectedFiles.length; i++) {
					slides[i] = new Slide();
					slides[i].setText(selectedFiles[i].getName());
					Key key = new Key(selectedFiles[i]); // the original not rotated and not mirror
					slides[i].setKey(key);
				}
				setOrAppendSlides(slides, append);

			} else {
				Context.cancelPropertyUpdate(name);
			}
		}
	}
	
	private void setOrAppendSlides(Slide[] slides, boolean append) {
		for (int i = 0; i < slides.length; i++) {
			
			IOUtil.log(System.currentTimeMillis() + " reading image " + i);
			Context.progressUpdate(" loading image " + i);
			Key key = slides[i].getKey();

			BufferedImage image;
			Image fromStore = ImageStore.getOrLoadImage(key);
			if (fromStore instanceof BufferedImage) {
				slides[i].setImage(key, fromStore);
			} else {
				image = ImageStore.createPlaceHolderImage(slides[i].getText());							
				slides[i].setImage(key, image);
				ImageStore.addImage(key, image);
			}
		}
		IOUtil.log(System.currentTimeMillis() + " before setNewValue");
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
		IOUtil.log(System.currentTimeMillis() + " after setNewValue");
	}
	
	private final class EditPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		
		private Point loc;
		private Slide slide;
		JButton prevButton = new JButton("<");
		JButton nextButton = new JButton(">");
		JLabel pos = new JLabel();
 		JTextField infoPath = new JTextField();
 		private JTextArea text = new JTextArea(3, 20);
		private JButton moveLeftButton = new JButton("< move");
		private JButton moveRightButton = new JButton("move >");
		private JLabel infoDim = new JLabel();
		
		public EditPanel() {
				
			setBorder(new LineBorder(valueSelect.getBackground(), 6));
			GridBagLayout gridbag = new GridBagLayout();
			
			setLayout(gridbag);
			GridBagConstraints gc = new GridBagConstraints();
			
			gc.insets = new Insets(3, 3, 3, 3);
			gc.fill = GridBagConstraints.HORIZONTAL;
			gc.weightx = GridBagConstraints.CENTER;
			gc.gridwidth = GridBagConstraints.REMAINDER;
			
			prevButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Slide[] slides = getCurrentValue();
					int idx = slide.getPosition() - 1;
					showSlideEditPopup(loc, slides[idx - 1], true);
				}
			});
			
			nextButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Slide[] slides = getCurrentValue();
					int idx = slide.getPosition() - 1;
					showSlideEditPopup(loc, slides[idx + 1], true);			
				}
			});
			
			JButton closeButton = new JButton("close");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					hideSlideEditPopup();			
				}
			});
			
			JPanel buttons = new JPanel();
			buttons.add(prevButton);
			buttons.add(nextButton);
			buttons.add(pos);
			buttons.add(closeButton);
			add(buttons, gc);
			
	 		infoPath.setEditable(false);
	 		infoPath.setPreferredSize(new Dimension(280, 24));
	 		JScrollPane infoSp = new JScrollPane(infoPath);
	 		add(infoSp, gc);
	 		
	 		add(infoDim, gc);
	 		
	 		JLabel label = new JLabel("slide text:");
	 		add(label, gc);

			text.setEnabled(true);
			text.setEditable(true);			
			text.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					final String oldText = slide.getText();
					slide.setText(text.getText());
					MultiImagePropertyPanel.this.changeSession(slide.getId() + "::text", oldText, text.getText());
				}		
			});
			
			JScrollPane sp = new JScrollPane(text);
			add(sp, gc);

			JPanel imagePanel = new JPanel() {
				private static final long serialVersionUID = 1L;
				@Override
				public void paint(Graphics g) {				
					super.paint(g);
					Image image = slide.getImage();
					float h = image.getHeight(null);
					float w = image.getWidth(null);
					float hf = 300f / h;
					float wf = 300f / w;
					float f = Math.min(hf, wf);
					((Graphics2D)g).scale(f, f);
					g.drawImage(image, (int)(10 / f), (int)(10 / f), null);
					setToolTipText(slide.getText() + ": " + image.getHeight(null) + " x " + image.getWidth(null));
				}};
			imagePanel.setPreferredSize(new Dimension(320, 320));
			imagePanel.setBackground(Color.BLACK);
			gridbag.setConstraints(imagePanel, gc);
			add(imagePanel, gc);

			gc.gridwidth = 2;
			gridbag.setConstraints(moveLeftButton, gc);
			add(moveLeftButton, gc);
			
			gc.gridwidth = GridBagConstraints.REMAINDER;
			gridbag.setConstraints(moveRightButton, gc);
			add(moveRightButton, gc);

			JButton rotateButton = new JButton("rotate");
			gridbag.setConstraints(rotateButton, gc);
			add(rotateButton, gc);	
			
			rotateButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Key oldKey = slide.getKey();
					Key newKey = oldKey.rotateClockWise();
					updateSlideImage(slide, oldKey, newKey);
					imagePanel.repaint();
					update(loc, slide);
				}
			});
			
			JButton mirrorButton = new JButton("mirror");
			gridbag.setConstraints(mirrorButton, gc);
			add(mirrorButton, gc);	
			
			mirrorButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Key oldKey = slide.getKey();
					Key newKey = oldKey.mirrorY();
					updateSlideImage(slide, oldKey, newKey);
					imagePanel.repaint();
					update(loc, slide);
				}
			});
			
			JButton removeButton = new JButton("remove");
			gridbag.setConstraints(removeButton, gc);
			add(removeButton, gc);

			moveLeftButton.addActionListener(new ActionListener() {	
				@Override
				public void actionPerformed(ActionEvent e) {
					moveSlide(slide, -1);
					update(loc, slide);
				}
			});
			moveRightButton.addActionListener(new ActionListener() {	
				@Override
				public void actionPerformed(ActionEvent e) {
					moveSlide(slide, 1);
					update(loc, slide);
				}
			});
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					hideSlideEditPopup();
					removeSlide(slide);
				}
			});	
		}
		
		public void update(Point loc, Slide slide) {
			this.loc = loc;
			this.slide = slide;
			int idx = slide.getPosition() - 1;
			prevButton.setEnabled(idx > 0);
			Slide[] slides = getCurrentValue();	
			nextButton.setEnabled(idx < slides.length - 1);
			pos.setText("slide " + (idx + 1));
	    	String infoText = slide.getKey().getAbsolutePath();
	    	infoPath.setText(infoText);
			text.setText(slide.getText());
	 		if(slide.getImage() != null) {
		    	int w = slide.getImage().getWidth(null);
		    	int h = slide.getImage().getHeight(null);
		    	String m = slide.getKey().isMirrored() ? " | mirrored" : "";
		 		infoDim.setText("orig size: " + w + "x" + h + " | rotation: " + slide.getKey().getQuadrantRotation() + m );
	 		}
			moveLeftButton.setEnabled(slide.getPosition() > 1);
			moveRightButton.setEnabled(getCurrentValue() != null && slide.getPosition() < getCurrentValue().length);
		}
	}

	private static final long serialVersionUID = 1L;
	private JButton fileButton = new JButton("+");
	private JButton editButton = new JButton("...");
	private JButton addFileButton = new JButton("+");
	private JButton addResourceButton = new JButton("*");
	JPanel valueSelect = new JPanel();
	JScrollPane scrollPane;

	private JPanel commands = new JPanel();
	private JPanel imagebar = new JPanel();
	private JPanel images = new JPanel();
	private Color origBackground;
	Popup popup = null;
	Popup editPopup = null;
	boolean isPopupShowing;
	final Set<JButton> imageButtons = new LinkedHashSet<JButton>();
	private final static Dimension BUTTON_SIZE = new Dimension(80, 80);
	private final static int CP_IMAGE_PREVIEW_SIZE = 40;

	private EditPanel editPanel = new EditPanel();

	/**
	 * Constructor
	 * 
	 * @param soundCanvas the canvas to work with
	 */
	public MultiImagePropertyPanel(final SoundCanvasWrapper soundCanvasWrapper, Object valueOwner, final JFrame frame) {
		super(soundCanvasWrapper, valueOwner);
		this.frame = frame;

		valueSelect.setLayout(new BorderLayout(2, 2));
		valueSelect.setBorder(new LineBorder(valueSelect.getBackground(), 6));

		addFileButton.setPreferredSize(BUTTON_SIZE);
		addFileButton.setFont(getFont().deriveFont(48.0f));
		addResourceButton.setPreferredSize(BUTTON_SIZE);
		addResourceButton.setFont(getFont().deriveFont(48.0f));

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
		addResourceButton.addActionListener(new ClassPathLoadActionListener());
		setCurrentValue(new Slide[] {});
		
	}

	private void showPopup() {
		Point loc = editButton.getLocationOnScreen();
		
		Rectangle screen = GraphicsUtil.getRootComponentOutline(this);
		int lowLimit = screen.y + screen.height;
		valueSelect.setPreferredSize(new Dimension(520, screen.height - 240));
		valueSelect.doLayout();
		int extentY = loc.y + valueSelect.getPreferredSize().height;
		int yToUse = extentY < lowLimit ? loc.y : loc.y - (extentY - lowLimit) - 10;
		
		popup = PopupFactory.getSharedInstance().getPopup(this, valueSelect, loc.x, yToUse);

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
	
	private void showSlideEditPopup(Point loc, Slide slide, boolean hide){
		if(hide) {
			hideSlideEditPopup(); // hide/remove others
		}
		editPanel.update(loc, slide);
		Rectangle screen = GraphicsUtil.getRootComponentOutline(MultiImagePropertyPanel.this);
		int lowLimit = screen.y + screen.height;
		editPanel.doLayout();
		int extentY = loc.y + editPanel.getPreferredSize().height;
		// debug: System.err.println("extentY " + extentY  + " lowLimit " + lowLimit);
		int yToUse = extentY < lowLimit ? loc.y : loc.y - (extentY - lowLimit) - 10;
		
		if(hide || editPopup == null) {
			editPopup = PopupFactory.getSharedInstance().getPopup(this, editPanel, loc.x, yToUse);
		}

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
		JButton ib = (JButton) imageButtons.toArray()[slide.getPosition() - 1];
		ib.setIcon(new ImageIcon(ImageStore.getOrLoadScaledImage(slide.getKey(), BUTTON_SIZE.width, BUTTON_SIZE.height)));
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
		for(JButton jb : imageButtons) {
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
		images.removeAll();
		imagebar.removeAll();
		imageButtons.clear();
		images = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		images.setLayout(gridbag);
		images.setBackground(Color.BLACK);
		List<JButton>allButtons = new ArrayList<>();
		
		if (currentValue != null && currentValue.length > 0) {
			for (final Slide slide : currentValue) {
				JButton ib = new JButton();
	
				ib.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						imageButtons.stream().forEach(jb -> jb.setSelected(jb == e.getSource()));
						showSlideEditPopup(ib.getLocationOnScreen(), slide, true);
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
						ib.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
					@Override
					public void mouseEntered(MouseEvent e) {
						ib.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
					@Override
					public void mouseClicked(MouseEvent e) {
						ib.setToolTipText(slide.getText());
						if(e.getButton() == MouseEvent.BUTTON3) {
							showSlideEditPopup(ib.getLocationOnScreen(), slide, true);
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

				Context.progressUpdate("creating preview " + (allButtons.size() + 1));
				ImageIcon icon = new ImageIcon(ImageStore.getOrLoadScaledImage(slide.getKey(), BUTTON_SIZE.width, BUTTON_SIZE.height));
				
				ib.setBorder(new LineBorder(Color.BLACK, 2));
				ib.setPreferredSize(BUTTON_SIZE);
				ib.setIcon(icon);
				imageButtons.add(ib);
				allButtons.add(ib);
			}
		}
		// add fixed buttons
		allButtons.add(addFileButton);
		allButtons.add(addResourceButton);
		
		int COLS = 6;
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridwidth = COLS;
		gc.fill = GridBagConstraints.BOTH;
		int rows = (allButtons.size()) / COLS;
		rows = (allButtons.size()) % COLS == 0 ? rows : rows + 1;
		gc.gridheight = rows;

		int col = 1;
		for(JButton b : allButtons) {
			if (col == COLS) {
				gc.gridwidth = GridBagConstraints.REMAINDER; // end row
				col = 0;
			} else {
				gc.gridwidth = 1;
			}
			gc.weightx = 1.0;
			gridbag.setConstraints(b, gc);
			images.add(b, gc);
			col++;
		}

		imagebar.add(images, BorderLayout.LINE_START);
		imagebar.revalidate();

		this.repaint();
	}

	@Override
	public void activate() {
		if(getCurrentValue() != null) {
			for(Slide slide : getCurrentValue()) {
				ImageStore.getOrLoadScaledImage(slide.getKey(), BUTTON_SIZE.width, BUTTON_SIZE.height);
			}
		}
		for(Key key : ImageStore.CLASSPATH_IMAGES) {
			ImageStore.getOrLoadScaledImage(key, CP_IMAGE_PREVIEW_SIZE, CP_IMAGE_PREVIEW_SIZE);
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
