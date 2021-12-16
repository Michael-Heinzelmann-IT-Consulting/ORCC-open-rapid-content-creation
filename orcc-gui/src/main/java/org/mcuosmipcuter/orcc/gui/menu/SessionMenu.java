package org.mcuosmipcuter.orcc.gui.menu;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import org.mcuosmipcuter.orcc.gui.Configuration;
import org.mcuosmipcuter.orcc.gui.util.ExtensionsFileFilter;
import org.mcuosmipcuter.orcc.gui.util.GraphicsUtil;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.SessionToken;
import org.mcuosmipcuter.orcc.soundvis.gui.ChangsBox;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener.CallBack;
import org.mcuosmipcuter.orcc.soundvis.gui.widgets.LoadMessage;
import org.mcuosmipcuter.orcc.soundvis.persistence.Session;
import org.mcuosmipcuter.orcc.util.IOUtil;

public class SessionMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	ChangsBox changesBox = new ChangsBox();

	public SessionMenu(final JFrame frame, final Function<List<String>, Void> errorsOnSessionLoadHandler) {
		super("Session");
		JMenuItem openSession = new JMenuItem("open session");
		JMenuItem saveSessionAs = new JMenuItem("save session as");
		JMenuItem saveSession = new JMenuItem("save session");

		LoadMessage loadMessage = new LoadMessage(32, 32);
		Context.addListener(loadMessage);

		BiFunction<URL, String, Void> sessionLoader = new BiFunction<URL, String, Void>() {
			Popup popup = null;
			
			@Override
			public Void apply(URL url, String classPath) {
				List<String> reportList = new ArrayList<String>();

				String msg = "loading " + (classPath != null ? classPath : url.toString()) + " ...";
				loadMessage.setHeader(msg);

				Rectangle screen = GraphicsUtil.getRootComponentOutline(frame);

				popup = PopupFactory.getSharedInstance().getPopup(frame, loadMessage,
						screen.x + screen.width / 2 - loadMessage.getPreferredSize().width / 2,
						screen.y + screen.height / 2);
				final AppState before = Context.getAppState();
				Context.setAppState(AppState.LOADING);
				popup.show();
				Thread t = new Thread() {

					@Override
					public void run() {
						try {
							boolean loaded = Session.userLoadSession(url, classPath, reportList);
							errorsOnSessionLoadHandler.apply(reportList);
							if (!loaded) {
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
				IOUtil.log("session loader startet for " + url);
				return null;
			}
		};

		CallBack openSessionCallback = new CallBack() {

			public void fileSelected(File file) {
				try {
					sessionLoader.apply(file.toURI().toURL(), null);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		};

		add(openSession);
		FileDialogActionListener openSessionActionListener = new FileDialogActionListener(frame, openSessionCallback,
				"open session");
		openSessionActionListener.setFileFilter(new ExtensionsFileFilter(Session.FILE_EXTENSION));
		openSession.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (allowSessionOpenRoutine()) {
					openSessionActionListener.actionPerformed(e);
				}
			}
		});

		CallBack saveSessionAsCallback = new CallBack() {
			public void fileSelected(File file) {
				if (file.exists()) {
					int res = JOptionPane.showConfirmDialog(null,
							file + " exists, are you sure you want to overwrite it ?", "",
							JOptionPane.OK_CANCEL_OPTION);
					if (res != JOptionPane.OK_OPTION) {
						return;
					}
				}
				try {
					Session.userSaveSession(file);
				} catch (IllegalArgumentException | IllegalAccessException | IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		addSeparator();
		add(saveSessionAs);
		FileDialogActionListener saveSessionAsActionListener = new FileDialogActionListener(frame, saveSessionAsCallback,
				"save session");
		saveSessionAsActionListener.setFileFilter(new ExtensionsFileFilter(Session.FILE_EXTENSION));
		saveSessionAsActionListener.setForcedExtension(Session.FILE_EXTENSION);
		saveSessionAs.addActionListener(saveSessionAsActionListener);

		addSeparator();
		add(saveSession);
		saveSession.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Context.getSessionToken().isNamed()) {
					try {
						File file = new File(Context.getSessionToken().getFullPath());
						Session.userSaveSession(file);
					} catch (IllegalArgumentException | IllegalAccessException | IOException e1) {
						throw new RuntimeException(e1);
					}
				} else {
					saveSessionAsActionListener.actionPerformed(e);
				}
			}
		});

		JMenuItem newSession = new JMenuItem("new");
		newSession.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (allowSessionOpenRoutine()) {
					Session.newSession();
				}
			}
		});
		addSeparator();
		add(newSession);
		
		JMenu demoSessions = new JMenu("demos");
		for(String path : Configuration.BUILT_IN_SESSIONS) {
			JMenuItem item = new JMenuItem(path.substring(path.lastIndexOf("/") + 1));
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (allowSessionOpenRoutine()) {	
						URL url = Session.fromClasspath(path);
						sessionLoader.apply(url, path);
					}
				}
			});
			demoSessions.add(item);
		}
		addSeparator();
		add(demoSessions);
		
		JMenuItem showChanges = new JMenuItem("show changes");
		showChanges.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changesBox.showUnsavedChanges(false);
			}
		});
		Context.addListener(changesBox);
		addSeparator();
		add(showChanges);
		
		Context.addListener(new Listener() {
			public void contextChanged(PropertyName propertyName) {
				if (PropertyName.AppState.equals(propertyName)) {
					AppState current = Context.getAppState();

					openSession.setEnabled(current == AppState.READY);
					newSession.setEnabled(current == AppState.READY);				
					saveSession.setEnabled(Context.getSessionToken().getClassPath() == null && current != AppState.INIT && current != AppState.LOADING);
					saveSessionAs.setEnabled(current == AppState.READY);
					demoSessions.setEnabled(current == AppState.READY);
				}
				if(PropertyName.NewSession.equals(propertyName)) {
					AppState current = Context.getAppState();
					saveSession.setEnabled(Context.getSessionToken().getClassPath() == null && current != AppState.INIT && current != AppState.LOADING);
				}
			}
		});
	}

	private boolean allowSessionOpenRoutine() {
		SessionToken st = Context.getSessionToken();
		if (st.isDefault() || st.isChanged()) {
			String message = (st.isDefault() ? "session is not saved, " : st .getDisplayPath() + "\nhas unsaved changes,") + " do you want to continue ?";
			int res = JOptionPane.showOptionDialog(null, message, "session not saved!", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, new String[] { "yes", "no" }, "no");
			if (res == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		return true;
	}


}
