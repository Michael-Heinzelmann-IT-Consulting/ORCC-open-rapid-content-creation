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

import java.util.Set;
import java.util.TreeSet;

import javax.sound.sampled.FloatControl;
import javax.swing.UIManager;

import org.mcuosmipcuter.orcc.api.soundvis.SoundCanvas;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.FontStore;
import org.mcuosmipcuter.orcc.util.ClassPathExplodedDirLoader;
import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * Configuration supporting several boot strap like stages to easy widget initialization<br/>
 * 	// args:<br/>
	// appmode [cli, gui] usermode [user, devuser, dev], default input file, default output file, default canvas
 * @author Michael Heinzelmann
 */
public abstract class Configuration {

	private static String appMode;
	private static String usrMode;
	private static int stage;
	
	/**
	 * Initial stage that creates all the basics: look & feel
	 * @param args you should pass the ones from main
	 */
	public static synchronized void init(String[] args) {

		appMode = args.length > 0 ? args[0] : "gui";
		usrMode = args.length > 1 ? args[1] : "user";
	
		if("gui".equals(appMode)) {
			try {
				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
				IOUtil.log("using nimbus look and feel");
			}
			catch(Exception ex) {
				if("true".equals(System.getProperty("force.metal.lf"))) {
					try {
						UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
						IOUtil.log("forced to using metal look and feel");
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				else {
					IOUtil.log("using platform default look and feel");
				}
			} 
		}
		if(usrMode.startsWith("dev")) {
			// video size
			Context.setOutputDimension(1920, 1080);
		}
		else {
			// TODO use user preferences from persistent storage
		}
		
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
		
		if(usrMode.startsWith("dev")) {
			Set<String> canvasClssNames = new TreeSet<String>();
			ClassPathExplodedDirLoader.loadClassNamesInto(canvasClssNames, SoundCanvas.class); // dev canvas from exploded
			for(String canvasClassName : canvasClssNames) {
				Context.addCanvasClassName(canvasClassName);
			}
		}
		else {
			// TODO use user preferences from persistent storage
			Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.ClassicWaves");
			Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.ColorsLR");
			Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.GridPulse");
			Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Image");
			Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.SlideShow");
			Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Pulsating");
			Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.RotatingAmplitudes");
			Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.SolidColor");
			Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.Text");
			Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.ThresholdVerticalLines");
			Context.addCanvasClassName("org.mcuosmipcuter.orcc.soundvis.defaultcanvas.XOR");
		}
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
		
		// set a dummy control to get volume setup
		FloatControl dummy = new FloatControl(FloatControl.Type.MASTER_GAIN, -80, 6, 1, 1, 0, "dB"){};
		Context.setVolumeControl(dummy);
		stage = 3;
	}
}
