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
package org.mcuosmipcuter.orcc.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.sound.sampled.AudioFormat;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.UnsupportedLookAndFeelException;

import org.mcuosmipcuter.orcc.gui.menu.ConfigurationMenu;
import org.mcuosmipcuter.orcc.gui.menu.ExportMenu;
import org.mcuosmipcuter.orcc.gui.menu.FileMenu;
import org.mcuosmipcuter.orcc.gui.menu.HelpMenu;
import org.mcuosmipcuter.orcc.gui.menu.InfoMenu;
import org.mcuosmipcuter.orcc.gui.menu.SessionMenu;
import org.mcuosmipcuter.orcc.gui.menu.ViewMenu;
import org.mcuosmipcuter.orcc.gui.table.CustomTable;
import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.RealtimeSettings.SettingsListener;
import org.mcuosmipcuter.orcc.soundvis.SessionToken;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.CanvasClassMenu;
import org.mcuosmipcuter.orcc.soundvis.gui.GraphPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.LogBox;
import org.mcuosmipcuter.orcc.soundvis.gui.PlayBackPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.GraphicsJInternalFrame;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.LoadMessage;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.TimeLabel;
import org.mcuosmipcuter.orcc.soundvis.persistence.FileConfiguration;
import org.mcuosmipcuter.orcc.soundvis.persistence.Session;
import org.mcuosmipcuter.orcc.soundvis.threads.SaveThread;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Main method class
 * 
 * @author Michael Heinzelmann
 */
public class Main {

	static final int infoW = 690;
	static final int infoH = 200;
	static final int playBackH = 240;
	private static boolean exitCalled;

	/**
	 * @param args
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws Exception {

		final JFrame frame = new JFrame("soundvis");
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable t) {
				System.err.println("UNCAUGHT Exception in " + thread);
				t.printStackTrace();
				if (!exitCalled) {
					IOUtil.logWithStack(t);
					String msg = t.getClass().getSimpleName() + ": " + t.getMessage();
					JOptionPane.showConfirmDialog(frame, msg, "Error", JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		ImageIcon imageIcon = new ImageIcon(Main.class.getResource("/img/icon_64x64.png"));
		frame.setIconImage(imageIcon.getImage());
		
		FileConfiguration.init(args != null && args.length > 1 ? args[1] : null);
		LogBox logBox = new LogBox(FileConfiguration.getLogBufferSize(100), frame);
		IOUtil.setListener(logBox);
		IOUtil.log("start");

		org.mcuosmipcuter.orcc.gui.Configuration.init();

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setMinimumSize(new Dimension(infoW, infoH + playBackH));
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitRoutine(frame.getSize(), logBox.getSize(), frame);
			}
		});

		final JDesktopPane deskTop = new JDesktopPane();
		final JInternalFrame playBackFrame = new JInternalFrame("Audio Timeline", true, false, false);
		playBackFrame.setFrameIcon(null);
		final GraphicsJInternalFrame graphicFrame = new GraphicsJInternalFrame("Video", true, false, false, false);
		graphicFrame.setFrameIcon(null);
		final GraphPanel graphicPanel = new GraphPanel(new Supplier<Dimension>() {
			@Override
			public Dimension get() {
				IOUtil.log("graphicFrame.getContentPane().getSize() -- " + graphicFrame.getContentPane().getSize());
				return graphicFrame.getContentPane().getSize();
			}
		});
		final PlayBackPanel playBackPanel = new PlayBackPanel(graphicPanel);

		org.mcuosmipcuter.orcc.gui.Configuration.stage1(args);

		JMenuBar mb = new JMenuBar();
		frame.setJMenuBar(mb);

		mb.add(new JMenu("  "));

		FileMenu fileMenu = new FileMenu(frame, new Function<Void, Void>() {	
			@Override
			public Void apply(Void t) {
				exitRoutine(frame.getSize(), logBox.getSize(), frame);
				return null;
			}
		});
		mb.add(fileMenu);
		
		SessionMenu sessionMenu = new SessionMenu(frame, new Function<List<String>, Void>() {
			@Override
			public Void apply(List<String> reportlist) {
				errorsOnSessionLoadRoutine(reportlist);
				return null;
			}
		});
		mb.add(sessionMenu);
		
		final ExportMenu exportMenu = new ExportMenu(frame, graphicPanel);
		mb.add(exportMenu);
		
		final ConfigurationMenu configMenu = new ConfigurationMenu(frame);
		mb.add(configMenu);
		
		final ViewMenu viewMenu = new ViewMenu(frame, graphicPanel, playBackPanel);
		mb.add(viewMenu);
		
		final InfoMenu infoMenu = new InfoMenu(frame, logBox);
		mb.add(infoMenu);
		
		final HelpMenu helpMenu = new HelpMenu(frame);
		mb.add(helpMenu);

		deskTop.setVisible(true);

		frame.getContentPane().add(deskTop);

		if (FileConfiguration.isAppsizeMaximized()) {
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
			Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			frame.setSize(screen);
			IOUtil.log("opening frame maximized, screen size=" + screen);
			frame.setVisible(true);

		} else {
			Optional<Dimension> userDefined = FileConfiguration.loadUserDefinedAppsize();
			if (userDefined.isPresent()) {
				Dimension d = userDefined.get();
				IOUtil.log("opening frame user defined " + d);
				frame.setSize(d.width, d.height);
			} else {
				IOUtil.log("opening frame default size");
				frame.setSize(1400, 800);
			}
			frame.setVisible(true);
		}
		IOUtil.log("frame size: " + frame.getSize());

		deskTop.add(playBackFrame);

		playBackFrame.getContentPane().add(playBackPanel, BorderLayout.SOUTH);
		graphicPanel.setMixin(playBackPanel);

		playBackFrame.setSize(deskTop.getWidth(), playBackH);
		playBackFrame.setVisible(true);

		Context.addListener(new Listener() {
			@Override
			public void contextChanged(PropertyName propertyName) {
				if (PropertyName.AudioInputInfo.equals(propertyName)) {
					AudioInput audioInput = Context.getAudioInput();
					final AudioFormat audioFormat = audioInput.getAudioInputInfo().getAudioFormat();
					TimeLabel tl = new TimeLabel();
					tl.update(audioInput.getAudioInputInfo().getFrameLength(), audioFormat.getSampleRate());
					playBackFrame.setTitle(audioInput.getAudioInputInfo().getLayout() + ": " + audioInput.getName()
							+ " | " + ((int) audioFormat.getSampleRate()) + " HZ | " + audioFormat.getSampleSizeInBits()
							+ " bit | length " + tl.getText());
				}
			}
		});

		final JInternalFrame propertiesFrame = new JInternalFrame("Layers");
		propertiesFrame.setFrameIcon(null);
		JMenuBar layersMenuBar = new JMenuBar();
		final JMenu canvas = new JMenu("canvas");
		layersMenuBar.add(canvas);
		CanvasClassMenu classes = new CanvasClassMenu("add canvas");
		canvas.add(classes);
		propertiesFrame.setJMenuBar(layersMenuBar);
		final CustomTable propTable = new CustomTable(frame, playBackPanel.getCustomTableListener());
		Context.addListener(propTable);

		JPanel container = new JPanel();
		container.setOpaque(true);
		container.setLayout(new BorderLayout());
		container.add(propTable, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(container);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		propertiesFrame.add(scrollPane);

		Context.addListener(new Listener() {
			@Override
			public void contextChanged(PropertyName propertyName) {
				if (PropertyName.SoundCanvasAdded.equals(propertyName)) {
					List<SoundCanvasWrapper> list = Context.getSoundCanvasList();

					propTable.addLayer(list.get(list.size() - 1), frame);
				}
				if (PropertyName.AppState.equals(propertyName)) {
					propTable.setEnabled(
							Context.getAppState() == AppState.READY || Context.getAppState() == AppState.PAUSED);
					canvas.setEnabled(Context.getAppState() == AppState.READY);
				}
			}
		});

		propertiesFrame.setLocation(0, playBackH);
		propertiesFrame.setResizable(true);
		propertiesFrame.setVisible(true);
		propertiesFrame.setSize(infoW, deskTop.getHeight() - playBackH - 10);
		deskTop.add(propertiesFrame);

		graphicPanel.setOpaque(true);
		graphicPanel.addSettingsListener(new SettingsListener() {
			@Override
			public void update(String description) {
				graphicFrame.setRealtimeTitle(description);

			}
		});
		Context.addListener(graphicPanel);

		deskTop.add(graphicFrame);

		graphicFrame.getContentPane().add(graphicPanel);
		graphicFrame.setSize(deskTop.getWidth() - infoW, deskTop.getHeight() - playBackH - 10);
		graphicFrame.setLocation(infoW, playBackH);
		graphicFrame.setVisible(true);

		Context.addListener(new Listener() {
			public void contextChanged(PropertyName propertyName) {
				if (PropertyName.NewSession.equals(propertyName) || PropertyName.SessionChanged.equals(propertyName)) {
					String inputTitle = Context.getSessionToken().isNamed() ? Context.getSessionToken().getDisplayPath()
							: "unnamed session";
					String complete = Context.getSessionToken().hasLoadErrors() ? " !!incomplete!! " : "";
					String changed = Context.getSessionToken().isChanged() ? complete + " * " : complete;
					graphicFrame.setInputTitle(inputTitle + changed);
				}
				if (PropertyName.VideoDimension.equals(propertyName)
						|| PropertyName.VideoFrameRate.equals(propertyName)) {
					String title = "Video " + Context.getVideoOutputInfo().getWidth() + "x"
							+ Context.getVideoOutputInfo().getHeight() + "p  @"
							+ Context.getVideoOutputInfo().getFramesPerSecond() + "fps";
					graphicFrame.setOutputTitle(title);
				}
			}
		});

		org.mcuosmipcuter.orcc.gui.Configuration.stage2(args);


		LoadMessage startUpLoadMessage = new LoadMessage(64, 32);
		Context.addListener(startUpLoadMessage);
		startUpLoadMessage.setHeader("loading last session ...");
		Rectangle screen = frame.getBounds();
		Popup popup = PopupFactory.getSharedInstance().getPopup(frame, startUpLoadMessage,
				screen.x + screen.width / 2 - startUpLoadMessage.getPreferredSize().width / 2, screen.y + screen.height / 2);
		popup.show();
		Context.setAppState(AppState.LOADING);
		List<String> reportList = new ArrayList<String>();
		boolean restoredSession = Session.restoreSession(reportList);
		if (!restoredSession) {
			Session.newSession();
		}
		errorsOnSessionLoadRoutine(reportList);
		popup.hide();

		SaveThread saveThread = new SaveThread();
		Context.addListener(saveThread);
		saveThread.start();
		Context.setAppState(AppState.READY);

	}

	private static void errorsOnSessionLoadRoutine(List<String> reportList) {
		if (!reportList.isEmpty()) {
			StringBuilder messages = new StringBuilder("Errors during session restore:");
			for (String m : reportList) {
				messages.append("\n" + m);
			}
			JOptionPane.showMessageDialog(null, messages.toString());
		}
	}

	private static void exitRoutine(Dimension frameSize, int logSize, JFrame frame) {
		if (Context.getAppState() != AppState.READY) {
			int res = JOptionPane.showOptionDialog(frame, "Confirm exit in state " + Context.getAppState(),
					"Do you want to exit in state " + Context.getAppState() + " ?", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, new String[] { "ok", "cancel" }, "cancel");
			if (res != JOptionPane.OK_OPTION) {
				return;
			}
		}

		try {
			FileConfiguration.storeUserDefinedAppsize(frameSize);
			FileConfiguration.storeLogBufferSize(logSize);

			SessionToken st = Context.getSessionToken();

			if (st.isChanged() && st.isNamed()) {
				int res = JOptionPane.showOptionDialog(frame, "save session " + Context.getSessionToken().getFullPath(),
						"Do you want to save ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						new String[] { "yes", "no", "cancel" }, "cancel");
				if (res == JOptionPane.OK_OPTION) {
					if(st.getClassPath() != null) {
						// can't save to classpath
						JOptionPane.showMessageDialog(frame, "this is a built in session, please use 'save as' menu option!");
						return;
					}
					else {
						File file = new File(Context.getSessionToken().getFullPath());
						try {
							Session.userSaveSession(file);
						} catch (IllegalArgumentException | IllegalAccessException | IOException e) {
							e.printStackTrace();
						}
					}
				}
				if (res == JOptionPane.NO_OPTION) {
					// nothing
				}
				if (res == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
			Session.saveDefaultSession(true);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		exitCalled = true;
		System.exit(0);
	}
}
