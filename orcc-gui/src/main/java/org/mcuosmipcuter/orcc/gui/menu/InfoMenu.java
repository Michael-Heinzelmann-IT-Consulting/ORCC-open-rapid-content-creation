package org.mcuosmipcuter.orcc.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.mcuosmipcuter.orcc.soundvis.gui.AboutBox;
import org.mcuosmipcuter.orcc.soundvis.gui.LogBox;

public class InfoMenu extends JMenu {

	private static final long serialVersionUID = 1L;


	public InfoMenu(final JFrame frame, final LogBox logBox ) {
		super("Info");
		JMenuItem system = new JMenuItem("system environment");
		add(system);
		system.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutBox.showSystemProperties(true, frame);
			}
		});
		JMenuItem showLog = new JMenuItem("show log");
		showLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logBox.showLog(false);
			}
		});
		addSeparator();
		add(showLog);
		
	}



}
