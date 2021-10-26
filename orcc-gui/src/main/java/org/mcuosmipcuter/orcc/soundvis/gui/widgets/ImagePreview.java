/**
*   ORCC rapid content creation for entertainment, education and media production
*   Copyright (C) 2021 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
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
package org.mcuosmipcuter.orcc.soundvis.gui.widgets;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.border.EtchedBorder;

import org.mcuosmipcuter.orcc.api.util.TextHelper;


public class ImagePreview extends JComponent
                          implements PropertyChangeListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ImageIcon previewIcon;
    File file;
    String dimension;
 
    public ImagePreview(JFileChooser fileChooser) {
        setPreferredSize(new Dimension(100, 50));
        fileChooser.addPropertyChangeListener(this);
        setBorder(new EtchedBorder());
    }
 
    public void loadImage() {
        if (file == null) {
            previewIcon = null;
            dimension = null;
            return;
        }
 
        ImageIcon icon = new ImageIcon(file.getPath());
        
        if (icon != null) {
        	int w = icon.getImage().getWidth(null);
        	int h = icon.getImage().getHeight(null);
        	dimension = w > 0 && h > 0 ? w + "x" + h : "not an image";
            if (icon.getIconWidth() > 90) {
                previewIcon = new ImageIcon(icon.getImage().
                                          getScaledInstance(90, -1,
                                                      Image.SCALE_DEFAULT));
            } else if (icon.getIconHeight() > 50) {
                previewIcon = new ImageIcon(icon.getImage().
                                          getScaledInstance(-1, 50,
                                                      Image.SCALE_DEFAULT));
            } else {
                previewIcon = icon;
            }
        }
    }
 
    public void propertyChange(PropertyChangeEvent e) {
        boolean update = false;
        String property = e.getPropertyName();
 
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(property)) {
            file = null;
            update = true;
 
        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(property)) {
            file = (File) e.getNewValue();
            update = true;
        }
        else if(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(property)) {
        	File[] files = (File[]) e.getNewValue();
        	if(files != null) {
        		file =  files[files.length > 0 ? files.length - 1 : null];
        	}
        	else {
        		file = null;
        	}

            update = true;
        }
        if (update) {
            previewIcon = null;
            if (isShowing()) {
                loadImage();
                repaint();
            }
        }
    }
 
    protected void paintComponent(Graphics g) {
        if (previewIcon == null) {
            loadImage();
        }
        if (previewIcon != null) {
            int x = getWidth() / 2 - previewIcon.getIconWidth() / 2;
            int y = getHeight() / 2 - previewIcon.getIconHeight() / 2;
 
            if (y < 0) {
                y = 0;
            }
 
            if (x < 5) {
                x = 5;
            }
            previewIcon.paintIcon(this, g, x, y);
            TextHelper.writeText(dimension, (Graphics2D) g, 12, getBackground().darker(), getWidth(), getHeight() / 2 + previewIcon.getIconHeight() / 2 + 20);
        }
        else {
        	TextHelper.writeText("preview", (Graphics2D) g, 16, getBackground().darker(), getWidth(), getHeight() / 2);
        }
    }

}
