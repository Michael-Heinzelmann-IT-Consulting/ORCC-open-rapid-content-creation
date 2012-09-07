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
package org.mcuosmipcuter.orcc.soundvis.threads;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.DecodingCallback;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
import org.mcuosmipcuter.orcc.soundvis.Renderer;
import org.mcuosmipcuter.orcc.soundvis.util.ByteArrayLinearDecoder;
import org.mcuosmipcuter.orcc.util.IOUtil;


/**
 * Playback controller
 * @author Michael Heinzelmann
 */
public class PlayThread extends Thread implements PlayPauseStop {
	
	Renderer renderer;

	private long sampleCount;
	private long frameCount;
	
	private SourceDataLine sourceDataLine;
	private int chunkSize;
	private int samplesPerFrame;
	private byte[] data;
	private int dataPos;
	
	Status status = Status.NEW;
	
	/**
	 * @param renderer the renderer to work with
	 */
	public PlayThread(Renderer renderer) {
		this.renderer = renderer;
	}
	
	@Override
	public void run() {
		
		status = Status.RUNNING;
		Context.setAppState(AppState.PLAYING);

		AudioInput audioInput = Context.getAudioInput();
		try {
			renderer.start(audioInput.getAudioInputInfo(), Context.getVideoOutputInfo());
			AudioFormat format = audioInput.getAudioInputInfo().getAudioFormat();
			sourceDataLine = AudioSystem.getSourceDataLine(format);
			chunkSize =  format.getFrameSize();
			samplesPerFrame = (int)format.getSampleRate() / Context.getVideoOutputInfo().getFramesPerSecond();
			data = new byte[samplesPerFrame * chunkSize];
			sourceDataLine.open(format, samplesPerFrame * chunkSize);
			sourceDataLine.start();
			AudioInputStream ais = audioInput.open();
			
			ByteArrayLinearDecoder.decodeLinear(ais, new DecodingCallback() {
				
				@Override
				public boolean nextSample(int[] amplitudes, byte[] rawData) {
					for(byte b : rawData) {
						data[dataPos] = b;
						dataPos++;
					}
					boolean cont = renderer.nextSample(amplitudes, rawData);
					
					if(sampleCount % samplesPerFrame == 0){
						frameCount++;
						renderer.newFrame(frameCount);
						sourceDataLine.write(data, 0, data.length); // blocks for the time of playing
						data = new byte[samplesPerFrame * chunkSize];
						dataPos = 0;
						cont = checkState();
					}
					sampleCount++;	
					return cont;
				}
			});
			IOUtil.log("done play.");
		}
		catch(IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (LineUnavailableException ex) {
			throw new RuntimeException(ex);
		}
		finally {
			status = Status.DONE;
			Context.setAppState(AppState.READY);
			if(sourceDataLine != null) {
				sourceDataLine.stop();
				sourceDataLine.close();
			}
			IOUtil.safeClose(audioInput);
		}
	}
	
	@Override
	public void continuePlaying() {
		if(status == Status.PAUSED) {
			status = Status.RUNNING;
			Context.setAppState(AppState.PLAYING);
		}
	}

	private boolean checkState() {
		while(status == Status.PAUSED) {
			IOUtil.log(".");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return status != Status.DONE;
	}

	@Override
	public void pausePlaying() {
		status = Status.PAUSED;
		Context.setAppState(AppState.PAUSED);
	}

	@Override
	public void stopPlaying() {
		status = Status.DONE;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void startPlaying() {
		start();
	}
}