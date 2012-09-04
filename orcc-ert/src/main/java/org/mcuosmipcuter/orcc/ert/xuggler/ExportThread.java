/**
* Copyright 2012 Michael Heinzelmann IT-Consulting
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
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
 *
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
	private  long time = 0;
	Status status = Status.NEW;


  /* (non-Javadoc)
   * @see java.lang.Thread#run()
   */
	@SuppressWarnings("deprecation")
	public void run()  {

		status = Status.RUNNING;
		AudioInput audioInput = Context.getAudioInput();
		format = audioInput.getAudioInputInfo().getAudioFormat();
		String inputAudioFilename = audioInput.getName();
		samplesPerFrame = (int)format.getSampleRate() / framesPerSecond;
		renderer.start(audioInput.getAudioInputInfo(), Context.getVideoOutputInfo());
		IStreamCoder audioCoder = null;
		IContainer container = null;
		final IMediaWriter writer = ToolFactory.makeWriter(Context.getExportFileName()); // output
		try {
			ICodec codec = ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_PCM_S16BE);


			final IRational FRAME_RATE =  IRational.make(framesPerSecond);
			writer.addVideoStream(0, 0, FRAME_RATE, width, height);
			writer.addAudioStream(1, 0, codec, 2, 44100);

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

								int[] amplitudes = ByteArrayLinearDecoder.decodeLinear(barr, 2, 2, false); // TODO how do we know endian ?
								renderer.nextSample(amplitudes, null);

								if(sampleCount % samplesPerFrame == 0){
									frameCount++;
									renderer.newFrame(frameCount);
									writer.encodeVideo(0, renderer.getFrameImage(), time, TimeUnit.MILLISECONDS);
									time += (long)(1000 / framesPerSecond);
								}
								sampleCount++;
							}

						}
					}
				}

			}
		}
		finally {
			status = Status.DONE;
			Context.setAppState(AppState.READY);
			writer.flush();
			writer.close();

			if (audioCoder != null) {
				audioCoder.close();
				audioCoder = null;
			}
			if (container !=null) {
				container.close();
				container = null;
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
