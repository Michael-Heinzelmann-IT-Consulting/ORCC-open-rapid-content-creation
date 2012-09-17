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
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStopHolder;
import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
import org.mcuosmipcuter.orcc.soundvis.gui.AboutBox;
import org.mcuosmipcuter.orcc.soundvis.gui.CanvasClassMenu;
import org.mcuosmipcuter.orcc.soundvis.gui.GraphPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.GraphStatusbar;
import org.mcuosmipcuter.orcc.soundvis.gui.InfoPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.PlayBackPanel;
import org.mcuosmipcuter.orcc.soundvis.gui.PropertyTableCellRendererEditor;
import org.mcuosmipcuter.orcc.soundvis.gui.PropertyTableHeaderRenderer;
import org.mcuosmipcuter.orcc.soundvis.gui.ResolutionMenu;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener.CallBack;
import org.mcuosmipcuter.orcc.soundvis.gui.listeners.StopActionListener;
import org.mcuosmipcuter.orcc.soundvis.util.ExportUtil;
import org.mcuosmipcuter.orcc.util.IOUtil;


/**
 * Main method class
 * @author Michael Heinzelmann
 */
public class Main {
	final static JTabbedPane tabbedPane = new JTabbedPane();
	private static Map<Component, Integer> tabsMap = new HashMap<Component, Integer>();
	
	static final int infoW = 320;
	static final int infoH = 400;
	static final int minCells = 4;
	
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
		org.mcuosmipcuter.orcc.gui.Configuration.init(args);
		
		final JFrame frame = new JFrame("soundvis");
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitRoutine();
			}
		});
		
		final GraphPanel graphicPanel = new GraphPanel();		
		
		org.mcuosmipcuter.orcc.gui.Configuration.stage1(args);
		
		// declare all graphics components in one place 
		JMenuBar mb = new JMenuBar();
		frame.setJMenuBar(mb);
		{
			mb.add(new JMenu("  "));
			
			JMenu fileMenu = new JMenu("File");
			mb.add(fileMenu);
			{
				JMenuItem openAudio = new JMenuItem("open audio");
				fileMenu.add(openAudio);
				CallBack openAudioCallback = new CallBack() {
					public void fileSelected(File file) {
						Context.setAudioFromFile(file.getAbsolutePath());
					}
				};
				FileDialogActionListener importActionListener 
					= new FileDialogActionListener(null, openAudioCallback, "open as audio input");
				openAudio.addActionListener(importActionListener);
				fileMenu.addSeparator();
				
				JMenuItem exit = new JMenuItem("exit");
				fileMenu.add(exit);
				exit.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						exitRoutine();
					}
				});
			}
			
			final JMenu exportMenu = new JMenu("Export");
			final JMenuItem exportStart = new JMenuItem("start");
			final JMenuItem exportStop = new JMenuItem("stop");
			mb.add(exportMenu);
			{
				CallBack exportVideo = new CallBack() {
					public void fileSelected(File file) {
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
				exportStart.addActionListener(exportActionListener);
				if(ExportUtil.isExportEnabled()) {
					exportMenu.add(exportStart);
					exportMenu.addSeparator();
					exportMenu.add(exportStop);
				}
				else {
					exportMenu.add(new JMenuItem("not enabled"));
				}
			}
			
			final JMenu configMenu = new JMenu("Configuration");
			mb.add(configMenu);
			{
				configMenu.add(new ResolutionMenu("video size", 1920, 1080));
				final CanvasClassMenu classes = new CanvasClassMenu("add canvas");
				configMenu.addSeparator();
				configMenu.add(classes);
			}
			
			// context listener for menu enabling
			Context.addListener(new Listener() {
				public void contextChanged(PropertyName propertyName) {
					if(PropertyName.AppState.equals(propertyName)) {
						configMenu.setEnabled(Context.getAppState() == AppState.READY);
						exportMenu.setEnabled(Context.getAppState() == AppState.READY || Context.getAppState() == AppState.EXPORTING);
						exportStart.setEnabled(Context.getAppState() != AppState.EXPORTING);
						exportStop.setEnabled(Context.getAppState() == AppState.EXPORTING);
					}
				}
			});
			
			final JMenu helpMenu = new JMenu("Help");
			mb.add(helpMenu);
			{
				JMenuItem about = new JMenuItem("about");
				helpMenu.add(about);
				about.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AboutBox.showModal();
					}
				});
			}
		}
		
		final JDesktopPane deskTop = new JDesktopPane();	
		deskTop.setVisible(true);
		
		appendTab(deskTop, "soundvis");
		frame.getContentPane().add(tabbedPane);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		
		IOUtil.log("frame size: " + frame.getSize());
		{
			JInternalFrame infoFrame = new JInternalFrame("File Info", true, false, true, true);
			deskTop.add(infoFrame);
			{
				InfoPanel infoPanel = new InfoPanel();
				infoFrame.getContentPane().add(infoPanel);
				Context.addListener(infoPanel);
				
				PlayBackPanel playBackPanel = new PlayBackPanel(graphicPanel);
				infoFrame.getContentPane().add(playBackPanel, BorderLayout.SOUTH);
				graphicPanel.setMixin(playBackPanel);
			}
			infoFrame.setSize(infoW, infoH);
			infoFrame.setVisible(true);
		}

		{
			final JInternalFrame propertiesFrame = new JInternalFrame("Properties", false, false, false, true);
			final JTable propTable = new JTable();
			PropertyTableCellRendererEditor ptcr = new PropertyTableCellRendererEditor();
			propTable.setDefaultRenderer(Object.class, ptcr);
			propTable.setRowHeight(infoH);
			propTable.setRowMargin(1);
			
			propTable.setDefaultEditor(Object.class, ptcr);
			
			propTable.getTableHeader().setDefaultRenderer(new PropertyTableHeaderRenderer());
			propTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
				@Override
				public void columnSelectionChanged(ListSelectionEvent arg0) {
				}			
				@Override
				public void columnRemoved(TableColumnModelEvent arg0) {
				}
				@Override
				public void columnMoved(TableColumnModelEvent arg0) {
					List<SoundCanvasWrapper> currentList = new ArrayList<SoundCanvasWrapper>();
					for(int i = 0; i < propTable.getModel().getColumnCount(); i++) {
						SoundCanvasWrapper s = (SoundCanvasWrapper)propTable.getValueAt(0, i);
						if(s != null) {
							currentList.add(s);
						}
					}
					Context.reorderCanvasList(currentList);
				}
				@Override
				public void columnMarginChanged(ChangeEvent arg0) {
				}
				@Override
				public void columnAdded(TableColumnModelEvent arg0) {
				}
			});
			JScrollPane scrollPane = new JScrollPane(propTable);
			propertiesFrame.add(scrollPane);
			{
				Context.addListener(new Listener() {	
					@Override
					public void contextChanged(PropertyName propertyName) {
						if(PropertyName.SoundCanvasAdded.equals(propertyName) || PropertyName.SoundCanvasRemoved.equals(propertyName)) {
							int size = Context.getSoundCanvasList().size() > minCells ? Context.getSoundCanvasList().size() : minCells;
							Object[] canvasData = new SoundCanvas[size];
							Object[] canvasHeader = new Object[size];
							Iterator<SoundCanvasWrapper> iter = Context.getSoundCanvasList().iterator();
							for(int i = 0; i < canvasData.length; i++ ){
								SoundCanvasWrapper val = iter.hasNext() ? iter.next() : null;
								canvasData[i] = val;
								canvasHeader[i] = val != null ? val : "";
							}
							Object[][] tableData = new Object[][] {canvasData};
							propTable.setModel(new DefaultTableModel(tableData, canvasHeader));
						}
						if(PropertyName.AppState.equals(propertyName)) {
							propTable.getTableHeader().setReorderingAllowed(Context.getAppState() == AppState.READY || Context.getAppState() == AppState.PAUSED);
							propTable.repaint();
							propTable.getTableHeader().repaint();
						}
					}
				});

			}
			propertiesFrame.setLocation(0, infoH);
			propertiesFrame.setVisible(true);
			propertiesFrame.setSize(infoW * 4, frame.getSize().height - infoH - 100);
			deskTop.add(propertiesFrame);
		}	

		{
			final JInternalFrame graphicFrame = new JInternalFrame("Graph", true, false, true, true);
			deskTop.add(graphicFrame);
			{
				graphicFrame.getContentPane().add(graphicPanel);
			}
			{
				GraphStatusbar graphStatusbar = new GraphStatusbar(graphicPanel);		
				graphicFrame.getContentPane().add(graphStatusbar, BorderLayout.SOUTH);
			}
			graphicFrame.setLocation(infoW, 0);
			graphicFrame.setVisible(true);
			
			Context.addListener(new Listener() {
				public void contextChanged(PropertyName propertyName) {
					if(PropertyName.SoundCanvasAdded.equals(propertyName)||
							PropertyName.SoundCanvasRemoved.equals(propertyName)||
							PropertyName.VideoDimension.equals(propertyName)) { 
						String title = Context.getVideoOutputInfo().getWidth() 
								+ "x" + Context.getVideoOutputInfo().getHeight() + "p  @"
								+ Context.getVideoOutputInfo().getFramesPerSecond() + "fps | " +
								Context.getSoundCanvasList();
						graphicFrame.setTitle(title);
						graphicPanel.preView();
					}
				}
			});
			graphicFrame.setSize(frame.getSize().width - infoW - 20, infoH);

		}
		
		org.mcuosmipcuter.orcc.gui.Configuration.stage2(args);
		
	}
	public static void appendTab(Component tab, String title){
		final int index = tabsMap.size();
		tabbedPane.add(tab);
		tabbedPane.setTitleAt(index, title);
		tabsMap.put(tab, index);
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
		System.exit(0);
	}
}

