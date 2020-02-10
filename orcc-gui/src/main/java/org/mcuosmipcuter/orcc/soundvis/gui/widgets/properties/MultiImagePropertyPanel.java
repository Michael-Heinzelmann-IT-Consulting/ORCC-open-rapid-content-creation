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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;

import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.ImageStore;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.defaultcanvas.model.Slide;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;


/**
 * Panel for {@link BufferedImage} properties using a {@link FileDialogActionListener} that pops up on pressing the button.
 * @author Michael Heinzelmann
 */
public class MultiImagePropertyPanel extends PropertyPanel<Slide[]> {

	private static final long serialVersionUID = 1L;
	private JButton fileButton = new JButton("+");

	private JPanel imagebar = new JPanel();
	private Slide selectedSlide;
	
	/**
	 * Constructor
	 * @param soundCanvas the canvas to work with
	 */
	public MultiImagePropertyPanel(final SoundCanvasWrapper soundCanvasWrapper, Object valueOwner) {
		super(soundCanvasWrapper, valueOwner);

		JPanel valueSelect = new JPanel();
		valueSelect.setLayout(new BorderLayout(2, 2));
		fileButton.setPreferredSize(new Dimension(80, 80));
		fileButton.setFont(getFont().deriveFont(48.0f));

		imagebar.setBackground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(imagebar);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		valueSelect.add(scrollPane);
		valueSelect.setPreferredSize(new Dimension(500, 240));
		add(valueSelect);
		
		fileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser() {

					@Override
					public void approveSelection() {
						System.err.println(System.currentTimeMillis()  + " chooser approve " + e);
						super.approveSelection();
					}};
				chooser.setMultiSelectionEnabled(true);
	        	System.err.println(System.currentTimeMillis()  + " chooser return " + e);
	        	Context.beforePropertyUpdate(name);
				// TODO image thumbnails chooser.setFileView(fileView);
		        int returnVal = chooser.showDialog(null, "select files");
		        if(returnVal == JFileChooser.APPROVE_OPTION) {

		        	File[] selectedFiles =  chooser.getSelectedFiles();
		        	Slide[] slides = new Slide[selectedFiles.length];
		        	for(int i = 0; i <  selectedFiles.length; i++) {
		        		slides[i] = new Slide();
		        		System.err.println(System.currentTimeMillis()  + " reading image " + i);
		        		Context.progressUpdate(" loading image " + i);
		        		
						BufferedImage image;
						Image fromStore = ImageStore.getImage(selectedFiles[i]);
						if(fromStore instanceof BufferedImage) {
							slides[i].setImage(fromStore);
						}
						else {
							try {
								image = ImageIO.read(selectedFiles[i]);
								slides[i].setImage(image);
								ImageStore.addImage(selectedFiles[i], image);
							} catch (IOException ex) {
								throw new RuntimeException(ex);
							}
						}

		        	}
		        	//System.err.println(System.currentTimeMillis()  + " before setNewValue " + e);
					setNewValue(slides);
		        	//System.err.println(System.currentTimeMillis()  + " after setNewValue " + e);
		        }
		        else {
		        	Context.cancelPropertyUpdate(name);
		        }
			}
		});
		setCurrentValue(new Slide[]{});
	}
	
	@Override
	public void setField(Field field) {
		String name = field.getName();
		nameLabel.setText("");
		this.name = name;
	}
	@Override
	public void setCurrentValue(Slide[] currentValue) {
		int pos = 0;
		if(currentValue != null) {
		for(Slide slide : currentValue) {
			slide.setPosition(++pos);
		}
		}
		super.setCurrentValue(currentValue);
		imagebar.removeAll();
		JPanel images = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		images.setLayout(gridbag);
		images.setBackground(Color.BLACK);
		

			int COLS = 6;
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridwidth = COLS;//GridBagConstraints.REMAINDER;	
			gc.fill = GridBagConstraints.BOTH;
//			gc.gridx = GridBagConstraints.WEST;
//			gc.gridy = GridBagConstraints.NORTH;
			//gc.insets = new Insets(16, 16, 16, 16);
			int col = 1;
			if(currentValue != null && currentValue.length > 0){
			int rows = (currentValue.length + 1) / COLS ;
			rows = (currentValue.length + 1) % COLS == 0 ? rows : rows+1;
			gc.gridheight = rows;
			//images.setLayout(new GridLayout(rows, currentValue.length < COLS ? currentValue.length : COLS));
			//// images.setLayout(new GridLayout(rows, COLS));

			
			final Set<JButton> jbuttons = new HashSet<JButton>();
			for(final Slide slide : currentValue) {
				JButton ib = new JButton() {

					@Override
					public void setSelected(boolean b) {
						// TODO Auto-generated method stub
						super.setSelected(b);
						
						setBackground(b ? Color.RED : Color.YELLOW);
						
					}};

				Context.progressUpdate(" creating preview " + (jbuttons.size() + 1));
				System.err.println(System.currentTimeMillis()  + " new preview " + slide);
				ib.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						selectedSlide = slide;
						jbuttons.stream().forEach(jb -> jb.setSelected(jb == e.getSource()));
					}
				});
				ib.addMouseListener(new MouseListener() {
					
					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub
						ib.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
					
					@Override
					public void mousePressed(MouseEvent e) {
						ib.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseClicked(MouseEvent e) {
						// TODO Auto-generated method stub
						//System.err.println(e.getButton());
						ib.setToolTipText("insert here");
						System.err.println("ib " + System.identityHashCode(ib));
						System.err.println(e.getX());
					}
				});
				ib.addMouseMotionListener(new MouseMotionListener() {
					
					@Override
					public void mouseMoved(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseDragged(MouseEvent e) {
						// TODO Auto-generated method stub
						ib.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
				});
	
				ib.setBackground(Color.YELLOW);
				ib.setBorder(new LineBorder(Color.BLACK, 2));
				ImageIcon icon = new ImageIcon(slide.getImage().getScaledInstance(80, 80, Image.SCALE_FAST));

				ib.setMaximumSize(new Dimension(80, 80));
				ib.setPreferredSize(new Dimension(80, 80));
				//ib.setIcon(new ImageIcon(image.getScaledInstance(80, 80, Image.SCALE_FAST)));
				ib.setIcon(icon);
				ib.setSelectedIcon(new ImageIcon(icon.getImage().getScaledInstance(60, 60, Image.SCALE_FAST)));
				if(col == COLS) {
					gc.gridwidth = GridBagConstraints.REMAINDER; //end row
					col = 0;
				}
				else {
					gc.gridwidth = 1;
				}
				gc.weightx = 1.0;
				gridbag.setConstraints(ib, gc);
				images.add(ib, gc);
				jbuttons.add(ib);
				col++;
			
			}
		}
			else {
				//images.setLayout(new GridLayout(1, COLS));
			}
			gc.weightx = 1.0;
			gc.gridwidth = col == COLS ? GridBagConstraints.REMAINDER : 1; //end row
			gridbag.setConstraints(fileButton, gc);
			images.add(fileButton, gc);
			imagebar.add(images, BorderLayout.LINE_START);


			
			imagebar.revalidate();
		this.repaint();
	}

}

