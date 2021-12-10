package org.mcuosmipcuter.orcc.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
import org.mcuosmipcuter.orcc.soundvis.gui.AudioOutputLayoutMenu;
import org.mcuosmipcuter.orcc.soundvis.gui.FrameRateMenu;
import org.mcuosmipcuter.orcc.soundvis.gui.PreferencesBox;
import org.mcuosmipcuter.orcc.soundvis.gui.ResolutionMenu;

public class ConfigurationMenu extends JMenu {

	private static final long serialVersionUID = 1L;


	public ConfigurationMenu(final JFrame frame) {
		super("Configuration");
		
		VideoOutputInfo v = Context.getVideoOutputInfo();
		ResolutionMenu resolutions = new ResolutionMenu("video size", v.getWidth(), v.getHeight());
		add(resolutions);
		Context.addListener(resolutions);

		final FrameRateMenu frameRates = new FrameRateMenu("frame rate", v.getFramesPerSecond());
		addSeparator();
		add(frameRates);
		Context.addListener(frameRates);
		
		AudioOutputLayoutMenu audioOutputLayoutMenu = new AudioOutputLayoutMenu("audio output");
		addSeparator();
		add(audioOutputLayoutMenu);
		Context.addListener(audioOutputLayoutMenu);

		JMenuItem preferences = new JMenuItem("preferences (startup)");
		preferences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PreferencesBox.showPreferncesDialog(frame);
			}
		});
		addSeparator();
		add(preferences);

		// context listener for menu enabling
		Context.addListener(new Listener() {
			public void contextChanged(PropertyName propertyName) {
				if (PropertyName.AppState.equals(propertyName)) {
					setEnabled(Context.getAppState() == AppState.READY);
				}
				if (PropertyName.AudioInputInfo.equals(propertyName)) {
					frameRates.checkFrameRatesEnabled(Context.getAudioInput().getAudioInputInfo());
				}
			}
		});
		
	}



}
