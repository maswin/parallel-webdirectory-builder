package edu.tce.cse.util;

public class Statistics {

	float[] data;
	float size;

	public Statistics(float[] data) {
		this.data = data;
		size = data.length;
	}

	public float getMean() {
		float sum = 0.0f;
		for (float a : data)
			sum += a;
		return sum / size;
	}

	public float getVariance() {
		float mean = getMean();
		float temp = 0;
		for (float a : data)
			temp+= Math.pow(a - mean, 2);
			//temp += (mean - a) * (mean - a);
		return temp / (size-1);
	}

	public float getStdDev() {
		return (float)Math.sqrt(getVariance());
	}

}
