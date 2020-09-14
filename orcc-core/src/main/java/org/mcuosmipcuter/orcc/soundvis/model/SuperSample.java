package org.mcuosmipcuter.orcc.soundvis.model;

/**
 * Sample data structure
 * @author Michael Heinzelmann
 */
public class SuperSample {
	
	public SuperSample(int min, int max, int noOfSamples, int avgUp, int avgDown) {
		this.min = min;
		this.max = max;
		this.noOfSamples = noOfSamples;
		this.avgUp = avgUp;
		this.avgDown = avgDown;
	}
	private final int min;
	private final int max;
	private final int noOfSamples;
	private final int avgUp;
	private final int avgDown;
	
	/**
	 * Get the minimum aggregated
	 * @return the minimum
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Get the maximum aggregated
	 * @return the maximum
	 */
	public int getMax() {
		return max;
	}

	/**
	 * Get the number of samples that have been aggregated
	 * @return
	 */
	public int getNoOfSamples() {
		return noOfSamples;
	}

	public int getAvgUp() {
		return avgUp;
	}

	public int getAvgDown() {
		return avgDown;
	}

	@Override
	public String toString() {
		return "SuperSample [min=" + min + ", max=" + max
				+ ", noOfSamples=" + noOfSamples + "]";
	}
	
}