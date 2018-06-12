package pl.jacekkulis.classifier;

import pl.jacekkulis.model.Sample;

public interface DistanceCalculator {

	double calculate(Sample sample, ClassStatisticData classStatisticData);
	
}
