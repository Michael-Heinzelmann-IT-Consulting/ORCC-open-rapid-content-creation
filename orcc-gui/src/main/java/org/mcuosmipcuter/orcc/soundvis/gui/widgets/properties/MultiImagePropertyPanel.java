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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;


/**
 * Panel for {@link BufferedImage} properties using a {@link FileDialogActionListener} that pops up on pressing the button.
 * @author Michael Heinzelmann
 */
public class MultiImagePropertyPanel extends PropertyPanel<BufferedImage[]> {

	private static final long serialVersionUID = 1L;
	private JButton fileButton = new JButton("...");

	private JPanel imagebar = new JPanel();
	private Image selectedImage; // TODO image wrapper
	
	/**
	 * Constructor
	 * @param soundCanvas the canvas to work with
	 */
	public MultiImagePropertyPanel(final SoundCanvasWrapper soundCanvasWrapper) {
		super(soundCanvasWrapper);

		JPanel valueSelect = new JPanel();
		valueSelect.setLayout(new BorderLayout(2, 2));
		fileButton.setPreferredSize(new Dimension(40, 20));
		valueSelect.add(fileButton, BorderLayout.NORTH);
		imagebar.setBackground(Color.BLACK);
        JScrollPane scrollPane = new JScrollPane(imagebar);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		valueSelect.add(scrollPane);
		valueSelect.setPreferredSize(new Dimension(500, 240));
		add(valueSelect);
		
		fileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				// TODO image thumbnails chooser.setFileView(fileView);
		        int returnVal = chooser.showDialog(null, "select files");
		        if(returnVal == JFileChooser.APPROVE_OPTION) {
		        	Context.beforePropertyUpdate("files");
		        	File[] selectedFiles =  chooser.getSelectedFiles();
		        	BufferedImage[] images = new BufferedImage[selectedFiles.length];
		        	for(int i = 0; i <  selectedFiles.length; i++) {
						BufferedImage image;
						try {
							image = ImageIO.read(selectedFiles[i]);
							images[i] = image;
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}

		        	}
					setNewValue(images);
		        }
			}
		});
		
	}
	
	@Override
	public void setField(Field field) {
		String name = field.getName();
		nameLabel.setText("");
		this.name = name;
	}
	@Override
	public void setCurrentValue(BufferedImage[] currentValue) {
		super.setCurrentValue(currentValue);
		imagebar.removeAll();
		JPanel images = new JPanel();
		images.setBackground(Color.BLACK);
		
		if(currentValue != null && currentValue.length > 0){
			int COLS = 6;
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridwidth = GridBagConstraints.REMAINDER;	
			//gc.fill = GridBagConstraints.HORIZONTAL;
			//gc.gridx = GridBagConstraints.EAST;
			gc.insets = new Insets(16, 16, 16, 16);
			int rows = currentValue.length / COLS ;
			rows = currentValue.length % COLS == 0 ? rows : rows+1;
			images.setLayout(new GridLayout(rows, currentValue.length < COLS ? currentValue.length : COLS));
			final Set<JButton> jbuttons = new HashSet<JButton>();
			for(final Image i : currentValue) {
			JButton ib = new JButton();

			ib.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectedImage = i;
					jbuttons.stream().forEach(jb -> jb.setSelected(jb == e.getSource()));
				}
			});

			ib.setBackground(Color.YELLOW);
			ib.setBorder(new LineBorder(Color.BLACK, 2));
			ib.setSelectedIcon(new ImageIcon(i.getScaledInstance(60, 60, Image.SCALE_FAST)));
			ib.setMaximumSize(new Dimension(80, 80));
			ib.setPreferredSize(new Dimension(80, 80));
			ib.setIcon(new ImageIcon(i.getScaledInstance(80, 80, Image.SCALE_FAST)));
			images.add(ib, gc);
			jbuttons.add(ib);
			imagebar.add(images, BorderLayout.LINE_START);
			
		}
			imagebar.revalidate();
			
		}
		this.repaint();
	}

}

