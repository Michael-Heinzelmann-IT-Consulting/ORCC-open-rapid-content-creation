package org.mcuosmipcuter.orcc.soundvis.gui.widgets;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;

public class LoadMessage extends JPanel implements Listener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int headerFontSize = 64;
	int progressFontSize = 32;
	
	JLabel label = new JLabel();
	
	JLabel update = new JLabel("...");

	public LoadMessage(int fontSize, int progressFontSize) {
		this.headerFontSize = fontSize;
		this.progressFontSize = progressFontSize;
		setLayout(new GridLayout(2, 1));
		setBorder(new  LineBorder(getBackground().darker(), 8, false));

		label.setBorder(new  LineBorder(getBackground(), 8, false));
		Font font = new Font("dialog", Font.PLAIN, headerFontSize);
		label.setOpaque(true);
		label.setFont(font);
		add(label);

		Font fontU = new Font("dialog", Font.PLAIN, progressFontSize);
		update.setFont(fontU);
		add(update);
	}
	
	public void setHeader(String header) {
		label.setText(header);
	}

	@Override
	public void progress(String msg) {
		String marg = msg.startsWith(" ") ? "" : " ";
		update.setText(marg + msg);
		revalidate();
		repaint();
	}

	@Override
	public void contextChanged(PropertyName propertyName) {
		
		
	}

}
