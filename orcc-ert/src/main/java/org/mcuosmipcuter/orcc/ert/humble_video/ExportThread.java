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
package org.mcuosmipcuter.orcc.ert.humble_video;

import java.awt.image.BufferedImage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.mcuosmipcuter.orcc.soundvis.AudioInput;
import org.mcuosmipcuter.orcc.soundvis.Context;
import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
import org.mcuosmipcuter.orcc.soundvis.DecodingCallback;
import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
import org.mcuosmipcuter.orcc.soundvis.Renderer;
import org.mcuosmipcuter.orcc.soundvis.util.ByteArrayLinearDecoder;
import org.mcuosmipcuter.orcc.soundvis.util.ExportUtil.VideoRenderer;
import org.mcuosmipcuter.orcc.util.IOUtil;

import io.humble.video.AudioChannel.Layout;
import io.humble.video.AudioFormat.Type;
import io.humble.video.Codec;
import io.humble.video.Encoder;
import io.humble.video.MediaAudio;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

/**
 * Thread writing a movie file by using the xuggler library - it has compile
 * time dependencies to it
 */
public class ExportThread extends Thread implements PlayPauseStop {

	@VideoRenderer
	Renderer renderer;

	// snapshot of context
	private final int framesPerSecond = Context.getVideoOutputInfo().getFramesPerSecond();
	private final int width = Context.getVideoOutputInfo().getWidth();
	private final int height = Context.getVideoOutputInfo().getHeight();

	// calculated
	private int samplesPerFrame;
	AudioFormat audioFormat;

	// state

	private long frameCount;
	Status status = Status.NEW;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {

		status = Status.RUNNING;
		Context.setAppState(AppState.EXPORTING);

		// final Muxer muxer = Muxer.make(Context.getExportFileName(), null, null);
		final Muxer muxer = Muxer.make(Context.getExportFileName(), null, null);
		AudioInputStream ais = null;

		try {
			AudioInput audioInput = Context.getAudioInput();
			ais = audioInput.getAudioStream();
			audioFormat = audioInput.getAudioInputInfo().getAudioFormat();
			samplesPerFrame = (int) audioFormat.getSampleRate() / framesPerSecond;
			renderer.start(audioInput.getAudioInputInfo(), Context.getVideoOutputInfo());

			final Rational framerate = Rational.make(1, framesPerSecond);
			for(MuxerFormat mf : MuxerFormat.getFormats()) {
				System.err.println(mf.getExtensions() + " | " + mf);
			}
			/**
			 * First we create a muxer using the passed in filename and formatname if given.
			 */
			/// final Muxer muxer = Muxer.make(Context.getExportFileName(), null, "mov");
			/**
			 * Now, we need to decide what type of codec to use to encode video. Muxers have
			 * limited sets of codecs they can use. We're going to pick the first one that
			 * works, or if the user supplied a codec name, we're going to force-fit that in
			 * instead.
			 */
			System.err.println("my MuxerFormat " + muxer.getFormat());
			final MuxerFormat videoFormat = muxer.getFormat();
			final Codec videoCodec;
//		    String videoCodecname = "libx264";//"mov";
//		    String audioCodecName = "libmp3lame"; //mov
//		    Type sampleFormat = Type.SAMPLE_FMT_S16P;
			String vCName = Context.getExportFileName().endsWith(".mp4") ? "mpeg4" : "libx264";
			String videoCodecname = vCName;// "mpeg4"; libx264rgb: qtrle rpza 8bps adpcm_ima_qt -notw h264
			String audioCodecName = "libmp3lame"; // "pcm_s16le_planar"; 
			Type sampleFormat = Type.SAMPLE_FMT_S16P;

			videoCodec = Codec.findEncodingCodecByName(videoCodecname);

			for (Codec codec : Codec.getInstalledCodecs()) {
				if (codec.getSupportedAudioSampleRates().size() >= 0) {
					try {
//						 System.err.println(codec.getName() + ":" + codec.getLongName() + ":" +
//						 codec.getIDAsInt() + ":" + codec.getSupportedAudioSampleRates()+ " - " +
//						 codec.getSupportedAudioFormats());
					} catch (Throwable t) {
						System.err.println(t.getMessage());
					}
				}
			}

			Codec audioCodec = Codec.findEncodingCodecByName(audioCodecName);
			System.err.println("SupportedAudioSampleRates " + audioCodec.getSupportedAudioSampleRates());
			System.err.println("SupportedVideoFrameRates " + audioCodec.getSupportedVideoFrameRates());
			System.err.println("getSupportedProfile(0) " + audioCodec.getSupportedProfile(0));
			System.err.println("SupportedAudioFormats " + audioCodec.getSupportedAudioFormats());
			final Encoder audioEncoder = Encoder.make(audioCodec);
			audioEncoder.setSampleFormat(sampleFormat);// SAMPLE_FMT_S16P
			if (videoFormat.getFlag(MuxerFormat.Flag.GLOBAL_HEADER)) {
				audioEncoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
			}
			int sr = (int) audioFormat.getSampleRate();
			audioEncoder.setSampleRate(sr);
			audioEncoder.setChannels(2);
			audioEncoder.setChannelLayout(Layout.CH_LAYOUT_STEREO);
			audioEncoder.open(null, null);
			muxer.addNewStream(audioEncoder);

			/**
			 * Now that we know what codec, we need to create an encoder
			 */
			final Encoder videoEncoder = Encoder.make(videoCodec);
			/**
			 * Video encoders need to know at a minimum: width height pixel format Some also
			 * need to know frame-rate (older codecs that had a fixed rate at which video
			 * files could be written needed this). There are many other options you can set
			 * on an encoder, but we're going to keep it simpler here.
			 */
			videoEncoder.setWidth(width);
			videoEncoder.setHeight(height);
			// We are going to use 420P as the format because that's what most video formats
			// these days use
			final PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;// PIX_FMT_NV12;
			videoEncoder.setPixelFormat(pixelformat);
			videoEncoder.setTimeBase(framerate);

			/**
			 * An annoynace of some formats is that they need global (rather than
			 * per-stream) headers, and in that case you have to tell the encoder. And since
			 * Encoders are decoupled from Muxers, there is no easy way to know this beyond
			 */
			if (videoFormat.getFlag(MuxerFormat.Flag.GLOBAL_HEADER)) {
				videoEncoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
			}

			/** Open the encoder. */
			videoEncoder.open(null, null);

			/** Add this stream to the muxer. */
			muxer.addNewStream(videoEncoder);

			System.err.println("muxer.getNumStreams() " + muxer.getNumStreams());
			/** And open the muxer for business. */
			muxer.open(null, null);

			/**
			 * Next, we need to make sure we have the right MediaPicture format objects to
			 * encode data with. Java (and most on-screen graphics programs) use some
			 * variant of Red-Green-Blue image encoding (a.k.a. RGB or BGR). Most video
			 * codecs use some variant of YCrCb formatting. So we're going to have to
			 * convert. To do that, we'll introduce a MediaPictureConverter object later.
			 * object.
			 */
//		      MediaPictureConverter videoConverter = null;

			final MediaPicture picture = MediaPicture.make(videoEncoder.getWidth(), videoEncoder.getHeight(),
					pixelformat);
			picture.setTimeBase(framerate);

			//////////////// mcuosmip

			final MediaPacket videoPacket = MediaPacket.make();

			ByteArrayLinearDecoder.decodeLinear(ais, new DecodingCallback() {
				final int NR_CHANNELS = 2;

				private long sampleCount;
				MediaPictureConverter videoConverter = null;
				int bufPos;

				MediaAudio samples = makeNewSample();

				private void writeaudioPacket() {
					samples.setTimeStamp(sampleCount);
					samples.setComplete(true);
					MediaPacket audioPacket = MediaPacket.make();
					do {
						audioEncoder.encodeAudio(audioPacket, samples);
						if (audioPacket.isComplete()) {
							muxer.write(audioPacket, false);
						}
					} while (audioPacket.isComplete());
				}

				private MediaAudio makeNewSample() {
					bufPos = 0;
					samples = MediaAudio.make(audioEncoder.getFrameSize(), audioEncoder.getSampleRate(),
							audioEncoder.getChannels(), audioEncoder.getChannelLayout(),
							audioEncoder.getSampleFormat());
					return samples;
				}

				@Override
				public boolean nextSample(int[] amplitudes, byte[] rawData, long sampleCount) {
					this.sampleCount = sampleCount;
					boolean cont = renderer.nextSample(amplitudes, rawData, 0 * samplesPerFrame + sampleCount);
					int dataSize = Math.min(samples.getDataPlaneSize(0), samples.getDataPlaneSize(0));
					// System.err.println("dataSize " + dataSize);
					if (bufPos < dataSize) {
						// continue appending
					} else {
						writeaudioPacket();
						makeNewSample();
					}
					byte[] L = new byte[2];
					byte[] R = new byte[2];
					L[0] = rawData[0];
					L[1] = rawData[1];
					R[0] = rawData[2];
					R[1] = rawData[3];
					samples.getData(0).put(L, 0, bufPos, L.length);
					samples.getData(1).put(R, 0, bufPos, R.length);

					bufPos += rawData.length / NR_CHANNELS;

					if (sampleCount % samplesPerFrame == 0) {

						cont = status == Status.RUNNING;
						frameCount++;
						renderer.newFrame(frameCount, cont);

						final BufferedImage screen = renderer.getFrameImage();
						/**
						 * This is LIKELY not in YUV420P format, so we're going to convert it using some
						 * handy utilities.
						 */
						if (videoConverter == null) {
							videoConverter = MediaPictureConverterFactory.createConverter(screen, picture);
						}
						long timeStampMicroSeconds = (frameCount - 1);
						videoConverter.toPicture(picture, screen, timeStampMicroSeconds);

						do {
							videoEncoder.encode(videoPacket, picture);
							if (videoPacket.isComplete()) {
								muxer.write(videoPacket, false);
							}
						} while (videoPacket.isComplete());

					}
					return cont;
				}

				@Override
				public void finished() {
					writeaudioPacket();
				}
			});

			/**
			 * Encoders, like decoders, sometimes cache pictures so it can do the right
			 * key-frame optimizations. So, they need to be flushed as well. As with the
			 * decoders, the convention is to pass in a null input until the output is not
			 * complete.
			 */
			do {
				videoEncoder.encode(videoPacket, null);
				if (videoPacket.isComplete()) {
					muxer.write(videoPacket, false);
				}
			} while (videoPacket.isComplete());

			MediaPacket audioPacket = MediaPacket.make();
			do {
				audioEncoder.encode(audioPacket, null);
				if (audioPacket.isComplete())
					muxer.write(audioPacket, false);
			} while (audioPacket.isComplete());

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			status = Status.DONE;
			Context.setAppState(AppState.READY);
			if (muxer != null) {
				try {
					muxer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			IOUtil.safeClose(ais);

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
