/**
 * 
 */
package org.mcuosmipcuter.orcc.soundvis.util;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;

import org.mcuosmipcuter.orcc.util.IOUtil;

/**
 * @author michael
 */
public class AudioUtil {
	
	/**
	 * @author michael
	 *
	 */
	public static class DummyVolumeControl extends FloatControl {

		/**
		 * @param type
		 * @param minimum
		 * @param maximum
		 * @param precision
		 * @param updatePeriod
		 * @param initialValue
		 * @param units
		 */
		public DummyVolumeControl() {
			// type, minimum, maximum, precision, updatePeriod, initialValue, units);
			super(FloatControl.Type.MASTER_GAIN, -80, 6, 1, 1, 0, "dB");
		}

	}

	/**
	 * 
	 */
	public static FloatControl getVolumeControl(SourceDataLine sourceDataLine ) {

		FloatControl vol = new DummyVolumeControl();
		if(sourceDataLine != null && sourceDataLine.isOpen()) {
			for(Control c : sourceDataLine.getControls()) {
				IOUtil.log(c + " " + c.getType());
				if(FloatControl.Type.MASTER_GAIN.equals(c.getType()) && c instanceof FloatControl){
					vol = (FloatControl) c;
				}
			}
		}
		return vol;
	}
	
	public static void checkAudioSystem() {

		IOUtil.log("AudioFileTypes:");
		for(Type type : AudioSystem.getAudioFileTypes()) {
			IOUtil.log(type.toString() + " " + type.getExtension());
		}
		
		IOUtil.log("Mixer infos:");
		for(Info info : AudioSystem.getMixerInfo()) {
			IOUtil.log(info.getName() + " " + info.getVendor() + " " + info.getVersion() + " " + info.getDescription());
		}
		
		IOUtil.log("MIDI device infos:");
		for(MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
			IOUtil.log(info.getName() + " " + info.getVendor() + " " + info.getVersion() + " " + info.getDescription());
		}
	}

}
