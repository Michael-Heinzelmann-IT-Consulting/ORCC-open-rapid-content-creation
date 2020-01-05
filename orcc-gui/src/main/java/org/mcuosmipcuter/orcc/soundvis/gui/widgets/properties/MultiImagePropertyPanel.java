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

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;


/**
 * Panel for {@link BufferedImage} properties using a {@link FileDialogActionListener} that pops up on pressing the button.
 * @author Michael Heinzelmann
 */
public class MultiImagePropertyPanel extends PropertyPanel<BufferedImage[]> {

	private static final long serialVersionUID = 1L;
	private JButton fileButton = new JButton("...");
	private JLabel thumbLabel = new JLabel("");
	
	/**
	 * Constructor
	 * @param soundCanvas the canvas to work with
	 */
	public MultiImagePropertyPanel(final SoundCanvasWrapper soundCanvasWrapper) {
		super(soundCanvasWrapper);
		thumbLabel.setOpaque(true);
		JPanel valueSelect = new JPanel();
		valueSelect.setLayout(new GridLayout(1, 2));
		valueSelect.add(thumbLabel);
		valueSelect.add(fileButton);
		add(valueSelect);
//		FileDialogActionListener.CallBack callBack = new FileDialogActionListener.CallBack(){
//			@Override
//			public void fileSelected(File file) {
//				try {
//					BufferedImage image = ImageIO.read(file);
//					setNewValue(image);
//
//				} catch (IOException ex) {
//					throw new RuntimeException(ex);
//				}
//				
//			}
//		};
		fileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				// TODO image thumbnails chooser.setFileView(fileView);
		        int returnVal = chooser.showDialog(null, "select files");
		        if(returnVal == JFileChooser.APPROVE_OPTION) {
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
	public void setCurrentValue(BufferedImage[] currentValue) {
		super.setCurrentValue(currentValue);
		if(currentValue != null && currentValue.length > 0){
			thumbLabel.setIcon(new ImageIcon(currentValue[0].getScaledInstance(thumbLabel.getWidth(), thumbLabel.getHeight(), Image.SCALE_FAST)));
		}
		this.repaint();
	}

}

