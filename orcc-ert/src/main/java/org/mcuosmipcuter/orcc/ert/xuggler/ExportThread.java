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
package org.mcuosmipcuter.orcc.ert.xuggler;


import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.sound.sampled.AudioFormat;

import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
import org.mcuosmipcuter.orcc.soundvis.Renderer;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.util.ByteArrayLinearDecoder;
import org.mcuosmipcuter.orcc.util.IOUtil;

import com.xuggle.ferry.IBuffer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

/**
 * Thread writing a movie file by using the xuggler library - it has compile time dependencies to it
 */
public class ExportThread extends Thread implements PlayPauseStop {
	
	@Resource
	Renderer renderer;
	
	// snapshot of context
	private  final int framesPerSecond = Context.getVideoOutputInfo().getFramesPerSecond();
	private  final int width = Context.getVideoOutputInfo().getWidth();
	private  final int height = Context.getVideoOutputInfo().getHeight();
	
	// calculated
	private int samplesPerFrame;
	AudioFormat format;
	
	// state
	private  long sampleCount;
	private  long frameCount;
	Status status = Status.NEW;


  /* (non-Javadoc)
   * @see java.lang.Thread#run()
   */
	@SuppressWarnings("deprecation")
	public void run()  {

		status = Status.RUNNING;
		Context.setAppState(AppState.EXPORTING);
		
		IMediaWriter writer = null;
		IStreamCoder audioCoder = null;
		IContainer container = null;
		try {
			AudioInput audioInput = Context.getAudioInput();
			format = audioInput.getAudioInputInfo().getAudioFormat();
			String inputAudioFilename = audioInput.getName();
			samplesPerFrame = (int)format.getSampleRate() / framesPerSecond;
			renderer.start(audioInput.getAudioInputInfo(), Context.getVideoOutputInfo());

			
			writer = ToolFactory.makeWriter(Context.getExportFileName()); // output
		
			ICodec codec = ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_PCM_S16BE);


			final IRational FRAME_RATE =  IRational.make(framesPerSecond);
			writer.addVideoStream(0, 0, FRAME_RATE, width, height);
			writer.addAudioStream(1, 0, codec, 2, (int)format.getSampleRate());

			container = IContainer.make();
			if (container.open(inputAudioFilename, IContainer.Type.READ, null) < 0) {
				throw new IllegalArgumentException("could not open file: " + inputAudioFilename);
			}
			int numStreams = container.getNumStreams();

			int audioStreamId = -1;

			for(int i = 0; i < numStreams; i++) {
				IStream stream = container.getStream(i);
				IStreamCoder coder = stream.getStreamCoder();
				if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
					audioStreamId = i;
					audioCoder = coder;
					break;
				}
			}
			if (audioStreamId == -1) {
				throw new RuntimeException("could not find audio stream in container: " + inputAudioFilename);
			}
			if (audioCoder.open() < 0) {
				throw new RuntimeException("could not open audio decoder for container: " + inputAudioFilename);
			}

			IPacket packet = IPacket.make();
			while(container.readNextPacket(packet) >= 0) {

				if (packet.getStreamIndex() == audioStreamId) {

					IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());
					int offset = 0;

					while(status == Status.RUNNING && offset < packet.getSize()) {

						int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
						if (bytesDecoded < 0) {
							throw new RuntimeException("got error decoding audio in: " + inputAudioFilename);
						}
						offset += bytesDecoded;

						if (samples.isComplete()) {

							writer.encodeAudio(1, samples);
							IBuffer ibuf = samples.getData();
							int destPos = 0;
							int frameSize = format.getFrameSize();

							int size = (int)samples.getNumSamples() * frameSize;

							for(int srcPos = 0; srcPos < size; srcPos += frameSize) {
								byte[] barr = new byte[frameSize];
								ibuf.get(srcPos, barr, destPos, frameSize);

								int[] amplitudes = new int[2]; // TODO constants for channel size
								ByteArrayLinearDecoder.decodeLinear(barr, amplitudes, 2, 2, false); // TODO how do we know endian ?
								sampleCount++;
								renderer.nextSample(amplitudes, null, sampleCount);

								if(sampleCount % samplesPerFrame == 0){
									frameCount++;
									renderer.newFrame(frameCount, true);
									writer.encodeVideo(0, renderer.getFrameImage(), ((frameCount - 1) * 1000) / framesPerSecond, TimeUnit.MILLISECONDS);
								}
							}

						}
					}
				}

			}
		}
		finally {
			status = Status.DONE;
			Context.setAppState(AppState.READY);
			
			if(writer != null) {
				writer.flush();
				writer.close();
			}
			if (audioCoder != null) {
				audioCoder.close();
			}
			if (container !=null) {
				container.close();
			}
			IOUtil.log("done export.");
		}

	}

	@Override
	public void continuePlaying() {
		// ignored
	}

	@Override
	public void pausePlaying() {
		// ignored
	}

	@Override
	public void stopPlaying() {
		status = Status.ABORTING;
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
