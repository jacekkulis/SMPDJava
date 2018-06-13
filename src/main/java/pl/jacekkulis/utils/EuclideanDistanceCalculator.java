package pl.jacekkulis.utils;

import pl.jacekkulis.model.Sample;

public class EuclideanDistanceCalculator implements IDistanceCalculator {

	@Override
	public double calculate(Sample sample, ClassStatisticData classStatisticData) {
		return Common.calculateEuclideanDistance(sample.getFeaturesMatrix(), classStatisticData.getMean());
	}

}
