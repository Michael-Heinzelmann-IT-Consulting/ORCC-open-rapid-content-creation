package org.mcuosmipcuter.orcc.ert.humble_video;

import io.humble.video.Encoder;
import io.humble.video.MediaAudio;
import io.humble.video.MediaPacket;
import io.humble.video.Muxer;

public class AudioExportHelper {
	
	private int bufPos;
	private long sampleCount;
	private final Encoder audioEncoderPointer;
	private final Muxer muxerPointer;
	MediaAudio samples;
	private final int PLANE_SIZE;
	
	public AudioExportHelper(Encoder audioEncoderPointer, Muxer muxerPointer) {
		this.audioEncoderPointer = audioEncoderPointer;
		this.muxerPointer = muxerPointer;
		samples = makeNewSample();
		this.PLANE_SIZE = samples.getNumDataPlanes();
		if(PLANE_SIZE != 1 && PLANE_SIZE != 2) {
			throw new IllegalArgumentException("not supportet PLANE_SIZE " + PLANE_SIZE);
		}
	}

	public void append4ByteData(byte[] rawData, long sampleCount) {
		if(rawData.length != 4) {
			throw new IllegalArgumentException("not supportet word size " + rawData.length);
		}
		this.sampleCount = sampleCount;
		final int dataSize  = samples.getDataPlaneSize(0);
		if (bufPos < dataSize) {
			// continue appending
		} else {
			writeaudioPacket();
			makeNewSample();
		}
		if(PLANE_SIZE == 2) {
			byte[] L = new byte[2];
			byte[] R = new byte[2];
			L[0] = rawData[0];
			L[1] = rawData[1];
			R[0] = rawData[2];
			R[1] = rawData[3];
			samples.getData(0).put(L, 0, bufPos, L.length);
			samples.getData(1).put(R, 0, bufPos, R.length);
		} else if(PLANE_SIZE == 1) {
			byte[] INTERLEAVED = new byte[4];
			INTERLEAVED[0] = rawData[0];
			INTERLEAVED[1] = rawData[1];
			INTERLEAVED[2] = rawData[2];
			INTERLEAVED[3] = rawData[3];
			samples.getData(0).put(INTERLEAVED, 0, bufPos, INTERLEAVED.length);
		}
		bufPos += rawData.length / PLANE_SIZE;
	}
	public void flush() {
		writeaudioPacket();
	}
	private void writeaudioPacket() {
		// time base must be in samples
		samples.setTimeStamp(sampleCount);
		samples.setComplete(true);
		MediaPacket audioPacket = MediaPacket.make();
		do {
			audioEncoderPointer.encodeAudio(audioPacket, samples);
			if (audioPacket.isComplete()) {
				muxerPointer.write(audioPacket, false);
			}
		} while (audioPacket.isComplete());
	}

	private MediaAudio makeNewSample() {
		bufPos = 0;
		samples = MediaAudio.make(audioEncoderPointer.getFrameSize(), audioEncoderPointer.getSampleRate(),
				audioEncoderPointer.getChannels(), audioEncoderPointer.getChannelLayout(),
				audioEncoderPointer.getSampleFormat());
		return samples;
	}
}
