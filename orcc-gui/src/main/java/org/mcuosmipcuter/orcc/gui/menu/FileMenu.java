package org.mcuosmipcuter.orcc.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.mcuosmipcuter.orcc.gui.Configuration;
import org.mcuosmipcuter.orcc.soundvis.AppLogicException;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener.CallBack;

public class FileMenu extends JMenu {

	private static final long serialVersionUID = 1L;


	public FileMenu(final JFrame frame, final Function<Void, Void> onExit ) {
		super("File");
		JMenu openAudio = new JMenu("open audio");
		JMenuItem fromFile = new JMenuItem("from file");
		CallBack openAudioCallback = new CallBack() {
			public void fileSelected(File file) {
				final AppState before = Context.getAppState();
				Context.setAppState(AppState.LOADING);
				Thread t = new Thread() {
					@Override
					public void run() {
						try {
							Context.setAudioFromFile(file.getAbsolutePath());
						} catch (AppLogicException ex) {
							JOptionPane.showMessageDialog(frame, ex.getAllowedMessage(Configuration.FRAME_RATES) , ex.getMessage(), JOptionPane.WARNING_MESSAGE);
						}
						catch(Exception ex) {
							JOptionPane.showMessageDialog(frame, ex.getMessage() , "error openeing audio", JOptionPane.ERROR_MESSAGE);
						}
						finally {
							Context.setAppState(before);
						}
					}
				};
				t.start();
			}
		};
		FileDialogActionListener importActionListener = new FileDialogActionListener(frame, openAudioCallback,
				"open as audio input");
		fromFile.addActionListener(importActionListener);
		openAudio.add(fromFile);
		
		for(String path : Configuration.BUILT_IN_AUDIO) {
			JMenuItem item = new JMenuItem(path.substring(path.lastIndexOf("/") + 1));
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final AppState before = Context.getAppState();
					Context.setAppState(AppState.LOADING);
					Thread t = new Thread() {
						@Override
						public void run() {
							try {
								Context.setAudioFromClasspath(path);
							} catch (AppLogicException ex) {
								JOptionPane.showMessageDialog(frame, ex.getAllowedMessage(Configuration.FRAME_RATES) , ex.getMessage(), JOptionPane.WARNING_MESSAGE);
							}
							catch(Exception ex) {
								JOptionPane.showMessageDialog(frame, ex.getMessage() , "error openeing audio", JOptionPane.ERROR_MESSAGE);
							}
							finally {
								Context.setAppState(before);
							}
						}
					};
					t.start();
				}
			});
			openAudio.add(item);
		}
		add(openAudio);
		
		addSeparator();
		JMenuItem exit = new JMenuItem("exit");
		add(exit);
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onExit.apply(null);
			}
		});
		
		Context.addListener(new Listener() {
			public void contextChanged(PropertyName propertyName) {
				if (PropertyName.AppState.equals(propertyName)) {
					AppState current = Context.getAppState();
					openAudio.setEnabled(current == AppState.READY);
				}
			}
		});
	}



}
