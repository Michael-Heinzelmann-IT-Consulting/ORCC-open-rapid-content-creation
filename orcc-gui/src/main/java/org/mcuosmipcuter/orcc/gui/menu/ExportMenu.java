package org.mcuosmipcuter.orcc.gui.menu;

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.mcuosmipcuter.orcc.gui.util.ExtensionsFileFilter;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStopHolder;
import org.mcuosmipcuter.orcc.soundvis.gui.GraphPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener.CallBack;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener.PreSelectCallBack;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.StopActionListener;
import org.mcuosmipcuter.orcc.soundvis.util.ExportUtil;

public class ExportMenu extends JMenu {

	private static final long serialVersionUID = 1L;


	public ExportMenu(final JFrame frame, final GraphPanel graphicPanel) {
		super("Export");
		final JMenuItem exportStart = new JMenuItem("start");
		final JMenuItem exportStop = new JMenuItem("stop");
		final JMenuItem exportFrameImage = new JMenuItem("frame as image");

		CallBack exportVideo = new CallBack() {
			public void fileSelected(File file) {
				if (file.exists()) {
					int res = JOptionPane.showConfirmDialog(null,
							file + " exists, are you sure you want to overwrite it ?", "",
							JOptionPane.OK_CANCEL_OPTION);
					if (res != JOptionPane.OK_OPTION) {
						return;
					}
				}
				Context.setExportFileName(file.getAbsolutePath());
				final PlayPauseStop exportThread = ExportUtil.getExportPlayPause(graphicPanel);
				for (ActionListener a : exportStop.getActionListeners()) {
					exportStop.removeActionListener(a);
				}
				exportStop.addActionListener(new StopActionListener(new PlayPauseStopHolder() {
					@Override
					public PlayPauseStop getPlayPauseStop() {
						return exportThread;
					}
				}));
				exportThread.startPlaying();
			}
		};
		FileDialogActionListener exportActionListener = new FileDialogActionListener(frame, exportVideo,
				"set as export file");
		exportActionListener.addChoosableFilter(new ExtensionsFileFilter(".mov"));
		exportActionListener.addChoosableFilter(new ExtensionsFileFilter(".mp4"));
		exportActionListener.setForcedExtension(".mov");
		exportStart.addActionListener(exportActionListener);

		CallBack exportFrameImageCallback = new CallBack() {
			public void fileSelected(File file) {
				if (file.exists()) {
					int res = JOptionPane.showConfirmDialog(null,
							file + " exists, are you sure you want to overwrite it ?", "",
							JOptionPane.OK_CANCEL_OPTION);
					if (res != JOptionPane.OK_OPTION) {
						return;
					}
				}
				BufferedImage image = graphicPanel.getFrameImage();
				try {
					String name = file.getName();
					int idx = name.length() - 3;
					String type = idx > 0 ? name.substring(idx) : "jpg";
					ImageIO.write(image, type, file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		FileDialogActionListener exportFrameActionListener = new FileDialogActionListener(frame,
				exportFrameImageCallback, "set as export image file");
		exportFrameActionListener.addInitialChoosableFilter(new ExtensionsFileFilter(".jpg"));
		exportFrameActionListener.addChoosableFilter(new ExtensionsFileFilter(".png"));
		exportFrameActionListener.addChoosableFilter(new ExtensionsFileFilter(".gif"));
		exportFrameActionListener.setForcedExtension(".jpg");
		exportFrameActionListener.setPreSelectCallBack(new PreSelectCallBack() {
			@Override
			public File preSelected() {
				String preselected = "frame" + Context.getSongPositionPointer();
				return new File(preselected);
			}
		});
		exportFrameImage.addActionListener(exportFrameActionListener);

		if (ExportUtil.isExportEnabled()) {
			add(exportStart);
			addSeparator();
			add(exportStop);
			addSeparator();
			add(exportFrameImage);
		} else {
			add(new JMenuItem("not enabled"));
		}
		Context.addListener(new Listener() {
			public void contextChanged(PropertyName propertyName) {
				if (PropertyName.AppState.equals(propertyName)) {
					AppState current = Context.getAppState();
					setEnabled(current == AppState.READY || current == AppState.EXPORTING);
					exportStart.setEnabled(
							current != AppState.INIT && current != AppState.EXPORTING && current != AppState.LOADING);
					exportStop.setEnabled(current == AppState.EXPORTING);
				}
			}
		});
	}
}
