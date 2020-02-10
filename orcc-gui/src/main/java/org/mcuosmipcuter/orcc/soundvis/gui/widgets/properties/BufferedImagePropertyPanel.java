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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;


/**
 * Panel for {@link BufferedImage} properties using a {@link FileDialogActionListener} that pops up on pressing the button.
 * @author Michael Heinzelmann
 */
public class BufferedImagePropertyPanel extends PropertyPanel<BufferedImage> {

	private static final long serialVersionUID = 1L;
	private JButton fileButton = new JButton("...");
	private JLabel thumbLabel = new JLabel("");
	
	/**
	 * Constructor
	 * @param soundCanvas the canvas to work with
	 */
	public BufferedImagePropertyPanel(final SoundCanvasWrapper soundCanvasWrapper, Object valueOwner) {
		super(soundCanvasWrapper, valueOwner);
		thumbLabel.setOpaque(true);
		JPanel valueSelect = new JPanel();
		valueSelect.setLayout(new GridLayout(1, 2));
		valueSelect.add(thumbLabel);
		valueSelect.add(fileButton);
		add(valueSelect);
		FileDialogActionListener.CallBack callBack = new FileDialogActionListener.CallBack(){
			@Override
			public void fileSelected(File file) {
				try {
					BufferedImage image = ImageIO.read(file);
					setNewValue(image);

				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
				
			}
		};
		fileButton.addActionListener(new FileDialogActionListener(null, callBack, "set as image"));

	}
	@Override
	public void setCurrentValue(BufferedImage currentValue) {
		super.setCurrentValue(currentValue);
		if(currentValue != null){
			thumbLabel.setIcon(new ImageIcon(currentValue.getScaledInstance(thumbLabel.getWidth(), thumbLabel.getHeight(), Image.SCALE_FAST)));
		}
		this.repaint();
	}

}

