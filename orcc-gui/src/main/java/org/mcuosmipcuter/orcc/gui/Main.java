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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.gui.table.CustomTable;
import org.mcuosmipcuter.orcc.gui.util.ExtensionsFileFilter;
import org.mcuosmipcuter.orcc.gui.util.GraphicsUtil;
import org.mcuosmipcuter.orcc.soundvis.AppLogicException;
import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStopHolder;
import org.mcuosmipcuter.orcc.soundvis.RealtimeSettings.SettingsListener;
import org.mcuosmipcuter.orcc.soundvis.SessionToken;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.AboutBox;
import org.mcuosmipcuter.orcc.soundvis.gui.AudioOutputLayoutMenu;
import org.mcuosmipcuter.orcc.soundvis.gui.CanvasClassMenu;
import org.mcuosmipcuter.orcc.soundvis.gui.ChangsBox;
import org.mcuosmipcuter.orcc.soundvis.gui.FrameModulusMenu;
import org.mcuosmipcuter.orcc.soundvis.gui.FrameRateMenu;
import org.mcuosmipcuter.orcc.soundvis.gui.GraphPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.PlayBackPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.PreferencesBox;
import org.mcuosmipcuter.orcc.soundvis.gui.ResolutionMenu;
import org.mcuosmipcuter.orcc.soundvis.gui.ZoomMenu;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener.CallBack;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener.PreSelectCallBack;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.StopActionListener;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.GraphicsJInternalFrame;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.TimeLabel;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.WidgetUtil;
import org.mcuosmipcuter.orcc.soundvis.persistence.FileConfiguration;
import org.mcuosmipcuter.orcc.soundvis.persistence.Session;
import org.mcuosmipcuter.orcc.soundvis.threads.SaveThread;
import org.mcuosmipcuter.orcc.soundvis.util.ExportUtil;
import org.mcuosmipcuter.orcc.util.IOUtil;


/**
 * Main method class
 * @author Michael Heinzelmann
 */
public class Main {
	
	static final int infoW = 690;
	static final int infoH = 200;
	static final int playBackH = 240;
	static final int minCells = 3;
	
	private static ChangsBox changesBox = new ChangsBox();
	
	/**
	 * @param args
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws Exception {
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable t) {
				System.err.println("UNCAUGHT Exception in " + thread);
				t.printStackTrace();
				String msg = t.getClass().getSimpleName() + ": " + t.getMessage();
				JOptionPane.showConfirmDialog(null, msg, "Error",
	                    JOptionPane.DEFAULT_OPTION,
	                    JOptionPane.ERROR_MESSAGE);
			}
		});
		
		FileConfiguration.init();
		
		org.mcuosmipcuter.orcc.gui.Configuration.init(args);
		
		final JFrame frame = new JFrame("soundvis");
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setMinimumSize(new Dimension(infoW, infoH + playBackH));
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitRoutine();
			}
		});
		
		final JDesktopPane deskTop = new JDesktopPane();
		final JInternalFrame playBackFrame = new JInternalFrame("Audio Timeline");
		final GraphicsJInternalFrame graphicFrame = new GraphicsJInternalFrame("Video", true, false, false, false);
		final GraphPanel graphicPanel = new GraphPanel();

		
		org.mcuosmipcuter.orcc.gui.Configuration.stage1(args);
		
		JMenuBar mb = new JMenuBar();
		frame.setJMenuBar(mb);
		{
			mb.add(new JMenu("  "));
			
			JMenu fileMenu = new JMenu("File");
			mb.add(fileMenu);
			JMenuItem openSession = new JMenuItem("open session");
			JMenuItem openAudio = new JMenuItem("open audio");
			JMenuItem saveSessionAs = new JMenuItem("save session as");
			JMenuItem saveSession = new JMenuItem("save session");
			{
				
				fileMenu.add(openAudio);
				CallBack openAudioCallback = new CallBack() {
					public void fileSelected(File file) {
						try {
							Context.setAudioFromFile(file.getAbsolutePath());
						} catch (AppLogicException ex) {
							throw new RuntimeException(ex);
						}
					}
				};
				FileDialogActionListener importActionListener 
					= new FileDialogActionListener(null, openAudioCallback, "open as audio input");
				openAudio.addActionListener(importActionListener);
				
				CallBack openSessionCallback = new CallBack() {
					Popup popup = null;;

					public void fileSelected(File file) {
						
						List<String> reportList = new ArrayList<String>();

						String msg = "loading " + file.getName() + " ...";

						JPanel popUpContentPanel = WidgetUtil.getMessagePanel(msg, 72, frame.getGraphics());
						Rectangle screen = GraphicsUtil.getRootComponentOutline(frame);

						popup = PopupFactory.getSharedInstance().getPopup(frame, popUpContentPanel,
								screen.x + screen.width / 2 - popUpContentPanel.getPreferredSize().width / 2,
								screen.y + screen.height / 2);
						final AppState before = Context.getAppState();
						Context.setAppState(AppState.LOADING);
						popup.show();
						Thread t = new Thread() {

							@Override
							public void run() {
								try {
									boolean loaded = Session.userLoadSession(file, reportList);
									errorsOnSessionLoadRoutine(reportList);
									if(!loaded) {
										throw new RuntimeException("could not load session: " + reportList);
									}
								} finally {
									Context.setAppState(before);
									if (popup != null) {
										popup.hide();
									}
								}
							}

						};
						t.start();

					}
				};
				fileMenu.addSeparator();
				
				fileMenu.add(openSession);
				FileDialogActionListener openSessionActionListener = new FileDialogActionListener(null, openSessionCallback, "open session");
				openSessionActionListener.setFileFilter(new ExtensionsFileFilter(Session.FILE_EXTENSION));
				openSession.addActionListener(new ActionListener() {				
					@Override
					public void actionPerformed(ActionEvent e) {
						if(allowSessionOpenRoutine()) {
							openSessionActionListener.actionPerformed(e);
						}	
					}
				});
				
				CallBack saveSessionAsCallback = new CallBack() {
					public void fileSelected(File file) {
						try {
							Session.userSaveSession(file);
						} catch (IllegalArgumentException | IllegalAccessException | IOException e) {
							throw new RuntimeException(e);
						}
					}
				};
				fileMenu.addSeparator();
				fileMenu.add(saveSessionAs);
				FileDialogActionListener saveSessionAsActionListener = new FileDialogActionListener(null, saveSessionAsCallback, "save session");
				saveSessionAsActionListener.setFileFilter(new ExtensionsFileFilter(Session.FILE_EXTENSION));
				saveSessionAsActionListener.setForcedExtension(Session.FILE_EXTENSION);
				saveSessionAs.addActionListener(saveSessionAsActionListener);
				
				fileMenu.addSeparator();
				fileMenu.add(saveSession);
				saveSession.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						if(Context.getSessionToken().isNamed()) {
							try {
								File file = new File(Context.getSessionToken().getFullPath());
								Session.userSaveSession(file);
							} catch (IllegalArgumentException | IllegalAccessException | IOException e1) {
								throw new RuntimeException(e1);
							}
						}
						else {
							saveSessionAsActionListener.actionPerformed(e);
						}
					}
				});
				
				fileMenu.addSeparator();
				JMenuItem exit = new JMenuItem("exit");
				fileMenu.add(exit);
				exit.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						exitRoutine();
					}
				});
			}
			JMenu sessionMenu = new JMenu("Session");
			
			mb.add(sessionMenu);
			JMenuItem newSession = new JMenuItem("new");
			newSession.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(allowSessionOpenRoutine()) {
						Session.newSession();
					}
				}
			});
			sessionMenu.add(newSession);
			JMenuItem showChanges = new JMenuItem("show changes");
			showChanges.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changesBox.showUnsavedChanges(false);
				}
			});
			Context.addListener(changesBox);
			sessionMenu.add(showChanges);
			
			final JMenu exportMenu = new JMenu("Export");
			final JMenuItem exportStart = new JMenuItem("start");
			final JMenuItem exportStop = new JMenuItem("stop");
			final JMenuItem exportFrameImage = new JMenuItem("frame as image");
			mb.add(exportMenu);
			{
				CallBack exportVideo = new CallBack() {
					public void fileSelected(File file) {
						if(file.exists()) {
							int res = JOptionPane.showConfirmDialog(null, file + " exists, are you sure you want to overwrite it ?", "", JOptionPane.OK_CANCEL_OPTION);
							if(res != JOptionPane.OK_OPTION) {
								return;
							}
						}
						Context.setExportFileName(file.getAbsolutePath());
						final PlayPauseStop exportThread = ExportUtil.getExportPlayPause(graphicPanel);
						for(ActionListener a : exportStop.getActionListeners()) {
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
				FileDialogActionListener exportActionListener = new FileDialogActionListener(frame, exportVideo, "set as export file");
				exportActionListener.addChoosableFilter(new ExtensionsFileFilter(".mov"));
				exportActionListener.addChoosableFilter(new ExtensionsFileFilter(".mp4"));
				exportActionListener.setForcedExtension(".mov");
				exportStart.addActionListener(exportActionListener);
				
				CallBack exportFrameImageCallback= new CallBack() {
					public void fileSelected(File file) {
						if(file.exists()) {
							int res = JOptionPane.showConfirmDialog(null, file + " exists, are you sure you want to overwrite it ?", "", JOptionPane.OK_CANCEL_OPTION);
							if(res != JOptionPane.OK_OPTION) {
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
				FileDialogActionListener exportFrameActionListener = new FileDialogActionListener(frame, exportFrameImageCallback, "set as export image file");
				exportFrameActionListener.addChoosableFilter(new ExtensionsFileFilter(".jpg"));
				exportFrameActionListener.addChoosableFilter(new ExtensionsFileFilter(".png"));
				exportFrameActionListener.addChoosableFilter(new ExtensionsFileFilter(".gif"));
				exportFrameActionListener.setPreSelectCallBack(new PreSelectCallBack() {				
					@Override
					public File preSelected() {
						String preselected = "frame" + Context.getSongPositionPointer() + ".jpg";
						return new File(preselected);
					}
				});
				exportFrameImage.addActionListener(exportFrameActionListener);
				
				if(ExportUtil.isExportEnabled()) {
					exportMenu.add(exportStart);
					exportMenu.addSeparator();
					exportMenu.add(exportStop);
					exportMenu.addSeparator();
					exportMenu.add(exportFrameImage);
				}
				else {
					exportMenu.add(new JMenuItem("not enabled"));
				}
				Context.addListener(new Listener() {
					public void contextChanged(PropertyName propertyName) {
						if(PropertyName.AppState.equals(propertyName)) {
							AppState current = Context.getAppState();
							exportMenu.setEnabled(current == AppState.READY || current == AppState.EXPORTING);
							exportStart.setEnabled(current != AppState.INIT && current != AppState.EXPORTING && current != AppState.LOADING);
							exportStop.setEnabled(current == AppState.EXPORTING);
							openAudio.setEnabled(current == AppState.READY);
							openSession.setEnabled(current == AppState.READY);
							newSession.setEnabled(current == AppState.READY);
							saveSession.setEnabled(current != AppState.INIT && current != AppState.LOADING);
							saveSessionAs.setEnabled(current != AppState.INIT && current != AppState.LOADING);
						}
					}
				});
			}
			
			final JMenu helpMenu = new JMenu("Help");
			mb.add(helpMenu);
			{
				JMenuItem basics = new JMenuItem("basics");
				helpMenu.add(basics);
				basics.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AboutBox.showFileText("/help.txt", false);
					}
				});
				JMenuItem system = new JMenuItem("system environment");
				helpMenu.add(system);
				system.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AboutBox.showSystemProperties(true);
					}
				});
				JMenuItem about = new JMenuItem("about");
				helpMenu.add(about);
				about.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AboutBox.showFileText("/license.txt", true);
					}
				});
			}
		}

		//deskTop.setDesktopManager(new CustomDeskTopManager(playBackFrame, graphicFrame));
		deskTop.setVisible(true);
		
		frame.getContentPane().add(deskTop);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.addWindowStateListener(new WindowStateListener() {
			
			@Override
			public void windowStateChanged(WindowEvent e) {
				IOUtil.log("window state " + e);
				synchronized(frame) {
					frame.notify();
				}
			}
		});
		frame.setVisible(true);
		synchronized(frame) {
			frame.wait(30000);
		}
		
		final PlayBackPanel playBackPanel = new PlayBackPanel(graphicPanel);
		IOUtil.log("frame size: " + frame.getSize());
		{
			deskTop.add(playBackFrame);
			
			{
				playBackFrame.getContentPane().add(playBackPanel, BorderLayout.SOUTH);
				graphicPanel.setMixin(playBackPanel);
			}
			
			playBackFrame.setSize(deskTop.getWidth(), playBackH);
			playBackFrame.setVisible(true);

			Context.addListener(new Listener() {		
				@Override
				public void contextChanged(PropertyName propertyName) {
					if(PropertyName.AudioInputInfo.equals(propertyName)) {
						AudioInput audioInput = Context.getAudioInput();
						final AudioFormat audioFormat = audioInput.getAudioInputInfo().getAudioFormat();
						TimeLabel tl = new TimeLabel();
						tl.update(audioInput.getAudioInputInfo().getFrameLength(), audioFormat.getSampleRate());
						playBackFrame.setTitle(audioInput.getAudioInputInfo().getLayout() + ": " +audioInput.getName() + " | " + ((int)audioFormat.getSampleRate()) + " HZ | " + audioFormat.getSampleSizeInBits() + " bit | length " + tl.getText());
					}
				}
			});
		}

		{
			final JInternalFrame propertiesFrame = new JInternalFrame("Layers");
			JMenuBar layersMenuBar = new JMenuBar();
			final JMenu canvas = new JMenu("canvas");
			layersMenuBar.add(canvas);
			CanvasClassMenu classes = new CanvasClassMenu("add canvas");
			canvas.add(classes);
			propertiesFrame.setJMenuBar(layersMenuBar);
			final CustomTable propTable = new CustomTable();
			propTable.setListener(playBackPanel.getCustomTableListener());
			Context.addListener(propTable);
			
	        JPanel container = new JPanel();
	        container.setOpaque(true);
	        container.setLayout(new BorderLayout());
	        container.add(propTable, BorderLayout.NORTH);     
	        JScrollPane scrollPane = new JScrollPane(container);
	        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			propertiesFrame.add(scrollPane);
			
			{
				Context.addListener(new Listener() {	
					@Override
					public void contextChanged(PropertyName propertyName) {
						if(PropertyName.SoundCanvasAdded.equals(propertyName)) {
							List<SoundCanvasWrapper> list = Context.getSoundCanvasList();
							
							propTable.addLayer(list.get(list.size() - 1));
						}
						if(PropertyName.AppState.equals(propertyName)) {
							propTable.setEnabled(Context.getAppState() == AppState.READY || Context.getAppState() == AppState.PAUSED);
							canvas.setEnabled(Context.getAppState() == AppState.READY);
						}
					}
				});

			}
			
			propertiesFrame.setLocation(0, playBackH);
			propertiesFrame.setResizable(true);
			propertiesFrame.setVisible(true);
			propertiesFrame.setSize(infoW, deskTop.getHeight() - playBackH);
			deskTop.add(propertiesFrame);
		}	

		{
			{			
				
				final JMenu configMenu = new JMenu("Configuration");
				mb.add(configMenu);

				VideoOutputInfo v = Context.getVideoOutputInfo();
				ResolutionMenu resolutions = new ResolutionMenu("video size", v.getWidth(), v.getHeight());
				configMenu.add(resolutions);
				Context.addListener(resolutions);
				
				AudioOutputLayoutMenu audioOutputLayoutMenu = new AudioOutputLayoutMenu("audio output");
				configMenu.addSeparator();
				configMenu.add(audioOutputLayoutMenu);
				Context.addListener(audioOutputLayoutMenu);
				
				final FrameRateMenu frameRates = new FrameRateMenu("frame rate", v.getFramesPerSecond());
				configMenu.addSeparator();
				configMenu.add(frameRates);
				Context.addListener(frameRates);
				
				JMenuItem preferences = new JMenuItem("preferences (startup)");
				preferences.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						PreferencesBox.showPreferncesDialog();
					}
				});
				configMenu.addSeparator();
				configMenu.add(preferences);

				// context listener for menu enabling
				Context.addListener(new Listener() {
					public void contextChanged(PropertyName propertyName) {
						if(PropertyName.AppState.equals(propertyName)) {
							configMenu.setEnabled(Context.getAppState() == AppState.READY);
						}
						if(PropertyName.AudioInputInfo.equals(propertyName)) {
							frameRates.checkFrameRatesEnabled(Context.getAudioInput().getAudioInputInfo());
						}
					}
				});
			}
			final JMenu viewMenu = new JMenu("View");
			//graphicMenuBar.add(viewMenu);
			mb.add(viewMenu);
			viewMenu.add(new ZoomMenu("zoom", 0.0f, graphicPanel));
			graphicPanel.setZoomFactor(0.0f);
			graphicPanel.setOpaque(true);
			viewMenu.add(new FrameModulusMenu("realtime framerate reduction", 1, graphicPanel));
			graphicPanel.addSettingsListener(new SettingsListener() {
				
				@Override
				public void update(String description) {
					graphicFrame.setRealtimeTitle(description);
					
				}
			});
			Context.addListener(graphicPanel);

			
			deskTop.add(graphicFrame);
			{
				graphicFrame.getContentPane().add(graphicPanel);
			}
			graphicFrame.setSize(deskTop.getWidth() - infoW, deskTop.getHeight() - playBackH);
			graphicFrame.setLocation(infoW, playBackH);
			graphicFrame.setVisible(true);
			
			Context.addListener(new Listener() {
				public void contextChanged(PropertyName propertyName) {
					if(PropertyName.SessionChanged.equals(propertyName)) {
						String inputTitle = Context.getSessionToken().isNamed() ? Context.getSessionToken().getFullPath() : "unnamed session";
						String complete = Context.getSessionToken().hasLoadErrors() ? " !!incomplete!! " : "";
						String changed = Context.getSessionToken().isChanged() ? complete + " * " : complete;
						graphicFrame.setInputTitle(inputTitle + changed);
					}
					if(PropertyName.SoundCanvasAdded.equals(propertyName)||
							PropertyName.SoundCanvasRemoved.equals(propertyName)||
							PropertyName.VideoDimension.equals(propertyName) || 
							PropertyName.VideoFrameRate.equals(propertyName)) { 
						String title = "Video " + Context.getVideoOutputInfo().getWidth() 
								+ "x" + Context.getVideoOutputInfo().getHeight() + "p  @"
								+ Context.getVideoOutputInfo().getFramesPerSecond() + "fps | " +
								Context.getSoundCanvasList();
						graphicFrame.setOutputTitle(title);
					}
				}
			});
		}
		
		

		org.mcuosmipcuter.orcc.gui.Configuration.stage2(args);
		
		FileConfiguration.ensureAppDir(new Supplier<File>() {

			@Override
			public File get() {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
				chooser.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						return "directories";
					}
					
					@Override
					public boolean accept(File f) {
						return f != null && f.isDirectory();
					}
				});
				int res = chooser.showDialog(frame, "Appdir for soundvis");
				if(res == JFileChooser.APPROVE_OPTION) {
					return chooser.getSelectedFile();
				}
				return null;
			}
		});

		JPanel popUpContentPanel = WidgetUtil.getMessagePanel("loading last session ...", 72, frame.getGraphics());
		Rectangle screen = frame.getBounds();
		Popup popup = PopupFactory.getSharedInstance().getPopup(frame, popUpContentPanel, screen.x + screen.width / 2 - popUpContentPanel.getPreferredSize().width / 2, screen.y + screen.height / 2);
		popup.show();
		Context.setAppState(AppState.LOADING);
		List<String> reportList = new ArrayList<String>();
		boolean restoredSession = Session.restoreSession(reportList);
		if(!restoredSession) {
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
		if(!reportList.isEmpty()) {
			StringBuilder messages = new StringBuilder("Errors during session restore:");
			for(String m : reportList) {
				messages.append("\n" + m);
			}
			JOptionPane.showMessageDialog(null, messages.toString());
		}
	}
	private static boolean allowSessionOpenRoutine() {
		if (Context.getSessionToken().needsSave()) {
			String message = "Do you want to continue ?";
			int res = JOptionPane.showOptionDialog(null,
					message, "session not saved!",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					new String[] { "yes", "no" }, "no");
			if(res == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		return true;
	}
	private static void exitRoutine() {
		if(Context.getAppState() != AppState.READY) {
			int res = JOptionPane.showOptionDialog(null, "Confirm exit in state " 
		+ Context.getAppState(), "Do you want to exit in state " + Context.getAppState() + " ?", 
		JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] {"ok", "cancel"}, "cancel");
			if(res != JOptionPane.OK_OPTION) {
				return;
			}
		}

		try {
			SessionToken st = Context.getSessionToken();
			if (st.isDefault()) {
				Session.saveDefaultSession();
			} else if (st.needsSave()) {
				int res = JOptionPane.showOptionDialog(null,
						"save named session " + Context.getSessionToken().getFullPath(), "Do you want to save ?",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						new String[] { "yes", "no", "cancel" }, "cancel");
				if (res == JOptionPane.OK_OPTION) {
					File file = new File(Context.getSessionToken().getFullPath());
					try {
						Session.userSaveSession(file);
					} catch (IllegalArgumentException | IllegalAccessException | IOException e) {
						e.printStackTrace();
					}
				}
				if (res == JOptionPane.NO_OPTION) {
					Session.saveDefaultSession();
				}
				if (res == JOptionPane.CANCEL_OPTION) {
					return;
				}

			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.exit(0);
	}
}

