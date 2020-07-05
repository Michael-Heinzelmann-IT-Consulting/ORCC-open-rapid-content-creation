package org.mcuosmipcuter.orcc.soundvis.threads;

import org.mcuosmipcuter.orcc.soundvis.model.SuperSample;

/**
 * Represents the complete wave with meta information
 * @author Michael Heinzelmann
 */
public class SuperSampleData {
	private final SuperSample[] list;
	private final int overallMin;
	private final int overallMax;
	
	public SuperSampleData(SuperSample[] list, int overallMin, int overallMax) {
		this.list = list;
		this.overallMin = overallMin;
		this.overallMax = overallMax;
	}
	/**
	 * Get the list of samples, an array for performance reasons
	 * @return the array
	 */
	public SuperSample[] getList() {
		return list;
	}
	/**
	 * Meta information about the overall minimum
	 * @return the minimum overall
	 */
	public int getOverallMin() {
		return overallMin;
	}
	/**
	 * Meta information about the overall maximum
	 * @return the maximum overall
	 */
	public int getOverallMax() {
		return overallMax;
	}
	
}