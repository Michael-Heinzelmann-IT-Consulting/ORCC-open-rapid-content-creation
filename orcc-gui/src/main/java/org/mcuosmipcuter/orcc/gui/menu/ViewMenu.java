package org.mcuosmipcuter.orcc.gui.menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.mcuosmipcuter.orcc.soundvis.gui.GraphPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.PlayBackPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.ZoomMenu;

public class ViewMenu extends JMenu {

	private static final long serialVersionUID = 1L;


	public ViewMenu(final JFrame frame, final GraphPanel graphicPanel, final PlayBackPanel playBackPanel) {
		super("View");
		add(new ZoomMenu("video zoom", 0.0f, graphicPanel));
		graphicPanel.setZoomFactor(0.0f);

		SpinnerNumberModel modelZoom = new SpinnerNumberModel(100, 10, 60000, 10);
		final JSpinner framesToZoom = new JSpinner(modelZoom);
		final JPanel fz = new JPanel();
		fz.setLayout(new BorderLayout());
		fz.add(framesToZoom, BorderLayout.WEST);
		fz.add(new JLabel("samples"));
		JMenuItem zoom = new JMenuItem("wave zoom");
		add(zoom);

		zoom.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final int ftzOld = (Integer) framesToZoom.getValue();
				JOptionPane.showMessageDialog(frame, fz, "wave zoom to number of samples", JOptionPane.PLAIN_MESSAGE);
				int ftz = (Integer) framesToZoom.getValue();
				if (ftz != ftzOld) {
					playBackPanel.changeFrameZoom(ftz);
				}
			}
		});
		
	}

}
