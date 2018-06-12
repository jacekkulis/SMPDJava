package pl.jacekkulis.classifier;

import pl.jacekkulis.model.Sample;

public class EuclideanDistanceCalculator implements DistanceCalculator {

	@Override
	public double calculate(Sample sample, ClassStatisticData classStatisticData) {
		return Common.calculateEuclideanDistance(sample.getFeaturesMatrix(), classStatisticData.getMean());
	}

}
