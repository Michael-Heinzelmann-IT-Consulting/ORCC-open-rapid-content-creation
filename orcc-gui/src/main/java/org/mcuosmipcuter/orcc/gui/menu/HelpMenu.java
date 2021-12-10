package org.mcuosmipcuter.orcc.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.mcuosmipcuter.orcc.soundvis.gui.AboutBox;

public class HelpMenu extends JMenu {

	private static final long serialVersionUID = 1L;


	public HelpMenu(final JFrame frame) {
		super("Help");
		JMenuItem basics = new JMenuItem("basics");
		add(basics);
		basics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutBox.showFileText("/help.txt", false, frame);
			}
		});
		JMenuItem about = new JMenuItem("about");
		addSeparator();
		add(about);
		about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutBox.showFileText("/license.txt", true, frame);
			}
		});
		
	}



}
