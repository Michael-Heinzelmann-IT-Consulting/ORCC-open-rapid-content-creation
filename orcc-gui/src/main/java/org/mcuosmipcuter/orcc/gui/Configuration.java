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

import java.util.Properties;

import javax.sound.sampled.FloatControl;
import javax.swing.LookAndFeel;
import javax.swing.PopupFactory;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.FontStore;
import org.mcuosmipcuter.orcc.soundvis.persistence.FileConfiguration;
import org.mcuosmipcuter.orcc.soundvis.util.AudioUtil;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Configuration supporting several boot strap like stages to easy widget initialization<br/>
 * @author Michael Heinzelmann
 */
public abstract class Configuration {

	private static int stage;
	
	/**
	 * Initial stage that creates all the basics: look & feel
	 * @param args you should pass the ones from main: install type|sandbox dir
	 */
	public static synchronized void init(String[] args) {
		
		FileConfiguration.init(args != null && args.length > 1 ? args[1] : null);
		
		for(LookAndFeelInfo lfi : UIManager .getInstalledLookAndFeels()) {
			IOUtil.log(lfi.toString());
		}
	
		Properties config = FileConfiguration.getProperties();
		Object lfClassName = config.get(FileConfiguration.SOUNDVIS_PROPERTY_LOOK_AND_FEEL);
		if(lfClassName != null) {
			try {
				UIManager.setLookAndFeel(lfClassName.toString());
				IOUtil.log("look and feel set to : " + UIManager.getLookAndFeel());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				IOUtil.log("could not set look and feel: " + e.getMessage());
			}
		}
		else {
			LookAndFeel def = UIManager.getLookAndFeel();
			IOUtil.log("look and feel set to default: " + def);		
		}
		
		PopupFactory.setSharedInstance(new PopupFactory()); // force cross platform
		
		stage = 1;
	}

	
	/**
	 * Setting up the canvas implementations
	 * @param args you should pass the ones from main
	 */
	public static synchronized void stage1(String[] args) {	
		
		if(stage < 1) {
			throw new IllegalStateException("stage 1 required, but is " + stage);
		}
		
		// TODO use user preferences from persistent storage
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.AudioWave");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Blinds");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Blinker");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Chameleon");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.ClassicWaves");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.ColorsLR");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.GridPulse");
		// Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Image");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Pulsating");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.RotatingAmplitudes");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Shutter");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.SlideShow");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.SolidColor");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Text");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.ThresholdVerticalLines");
		Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Tiles");
		
		stage = 2;
	}
	
	/**
	 * Setting up canvas to use, audio (currently only in dev mode) and creating a dummy volume control
	 * @param args you should pass the ones from main
	 */
	public static synchronized void stage2(String[] args) {	
		
		if(stage < 2) {
			throw new IllegalStateException("stage 1 required, but is " + stage);
		}
		FontStore.init();
		
		AudioUtil.checkAudioSystem(); // report installed
		// set a dummy control to get volume setup
		FloatControl dummy = AudioUtil.getVolumeControl(null);
		Context.setVolumeControl(dummy);
		stage = 3;
	}
}
