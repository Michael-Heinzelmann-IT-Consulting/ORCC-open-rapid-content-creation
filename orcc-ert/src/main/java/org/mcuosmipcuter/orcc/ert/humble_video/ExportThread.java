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
 * Thread writing a movie file by using the humble video library - it has compile
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

		Muxer muxer = null;
		AudioInputStream ais = null;

		try {
			AudioInput audioInput = Context.getAudioInput();
			ais = audioInput.getAudioStream();
			audioFormat = audioInput.getAudioInputInfo().getAudioFormat();
			samplesPerFrame = (int) audioFormat.getSampleRate() / framesPerSecond;
			renderer.start(audioInput.getAudioInputInfo(), Context.getVideoOutputInfo());

			final Rational framerate = Rational.make(1, framesPerSecond);
			for(MuxerFormat mf : MuxerFormat.getFormats()) {
				//System.err.println(mf.getExtensions() + " | " + mf);
			}
			String formatName = Context.getExportFileName().endsWith(".mp4") ? "mov" : null;
			muxer = Muxer.make(Context.getExportFileName(), null, formatName);
			final Muxer muxerPointer = muxer;
			System.err.println("my MuxerFormat " + muxer.getFormat());
			
			final MuxerFormat videoFormat = muxer.getFormat();
			final Codec videoCodec;
			String vCName = Context.getExportFileName().endsWith(".mp4") ? "mpeg4" : "libx264";
			String videoCodecname = vCName;
			String audioCodecName =  "libmp3lame"; ////"pcm_s16be";//
			Type sampleFormat = Type.SAMPLE_FMT_S16P;//Type.SAMPLE_FMT_S16P for mp3

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
			audioEncoder.setSampleFormat(sampleFormat);
			if (videoFormat.getFlag(MuxerFormat.Flag.GLOBAL_HEADER)) {
				audioEncoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
			}
			int sr = (int) audioFormat.getSampleRate();
			audioEncoder.setSampleRate(sr);
			audioEncoder.setChannels(2);
			audioEncoder.setChannelLayout(Layout.CH_LAYOUT_STEREO);
			audioEncoder.open(null, null);
			muxer.addNewStream(audioEncoder);
			final AudioExportHelper audioExportHelper = new AudioExportHelper(audioEncoder, muxer);

			final Encoder videoEncoder = Encoder.make(videoCodec);
			videoEncoder.setWidth(width);
			videoEncoder.setHeight(height);
			// We are going to use 420P as the format because that's what most video formats
			// these days use
			final PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;// PIX_FMT_NV12;
			videoEncoder.setPixelFormat(pixelformat);
			videoEncoder.setTimeBase(framerate);

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

			final MediaPicture picture = MediaPicture.make(videoEncoder.getWidth(), videoEncoder.getHeight(),
					pixelformat);
			picture.setTimeBase(framerate);


			final MediaPacket videoPacket = MediaPacket.make();

			ByteArrayLinearDecoder.decodeLinear(ais, new DecodingCallback() {
//				final int NR_CHANNELS = 2;

//				private long sampleCount;
				MediaPictureConverter videoConverter = null;

//				int bufPos;

//				MediaAudio samples = makeNewSample();
//
//				private void writeaudioPacket() {
//					samples.setTimeStamp(sampleCount);
//					samples.setComplete(true);
//					MediaPacket audioPacket = MediaPacket.make();
//					do {
//						audioEncoder.encodeAudio(audioPacket, samples);
//						if (audioPacket.isComplete()) {
//							muxerPointer.write(audioPacket, false);
//						}
//					} while (audioPacket.isComplete());
//				}
//
//				private MediaAudio makeNewSample() {
//					bufPos = 0;
//					samples = MediaAudio.make(audioEncoder.getFrameSize(), audioEncoder.getSampleRate(),
//							audioEncoder.getChannels(), audioEncoder.getChannelLayout(),
//							audioEncoder.getSampleFormat());
//					return samples;
//				}

				@Override
				public boolean nextSample(int[] amplitudes, byte[] rawData, long sampleCount) {

					boolean cont = renderer.nextSample(amplitudes, rawData, 0 * samplesPerFrame + sampleCount);
					audioExportHelper.append4ByteData(rawData, sampleCount);

					if (sampleCount % samplesPerFrame == 0) {

						cont = status == Status.RUNNING;
						frameCount++;
						renderer.newFrame(frameCount, cont);

						final BufferedImage screen = renderer.getFrameImage();
						if (videoConverter == null) {
							videoConverter = MediaPictureConverterFactory.createConverter(screen, picture);
						}
						long timeStampMicroSeconds = (frameCount - 1);
						videoConverter.toPicture(picture, screen, timeStampMicroSeconds);

						do {
							videoEncoder.encode(videoPacket, picture);
							if (videoPacket.isComplete()) {
								muxerPointer.write(videoPacket, false);
							}
						} while (videoPacket.isComplete());

					}
					return cont;
				}

				@Override
				public void finished() {
					audioExportHelper.flush();
				}
			});

			// flushing
			do {
				videoEncoder.encode(videoPacket, null);
				if (videoPacket.isComplete()) {
					muxer.write(videoPacket, false);
				}
			} while (videoPacket.isComplete());

				audioExportHelper.flush();
//			MediaPacket audioPacket = MediaPacket.make();
//			do {
//				audioEncoder.encode(audioPacket, null);
//				if (audioPacket.isComplete())
//					muxer.write(audioPacket, false);
//			} while (audioPacket.isComplete());

		} catch (Throwable e) {
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
