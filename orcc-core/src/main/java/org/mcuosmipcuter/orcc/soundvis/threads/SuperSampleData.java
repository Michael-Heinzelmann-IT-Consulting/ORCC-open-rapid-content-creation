package org.mcuosmipcuter.orcc.soundvis.threads;

import java.util.ArrayList;

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
	
	public SuperSampleData reduce(final int factor) {
		if(factor < 2) {
			return this;
		}
		ArrayList<SuperSample> reducedList = new ArrayList<>();
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		long sumUp = 0;
		long sumDown = 0;
		int r = 0;
		int f = 0;
		for(int i = 0; i < list.length; i++) {
			int noOfSamples = list[i].getNoOfSamples() * factor;
			if(list[i].getMax() > max) {
				max = list[i].getMax();
			}
			if(list[i].getMin() < min) {
				min = list[i].getMin();
			}
			sumUp += list[i].getAvgUp();
			sumDown += list[i].getAvgDown();
			f++;
			if(i > 0 && (i % factor == 0 || i == list.length - 1)) {
				reducedList.add(new SuperSample(min, max, noOfSamples, (int)(sumUp / f), (int)(sumDown / f)));
				r++;
				max = Integer.MIN_VALUE;
				min = Integer.MAX_VALUE;
				sumUp = 0;
				sumDown = 0;
				f = 0;
			}
		}
		return new SuperSampleData(reducedList.toArray(new SuperSample[] {}), this.overallMin, this.overallMax);
	}
}