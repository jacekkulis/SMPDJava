package pl.jacekkulis.utils;

import pl.jacekkulis.model.Sample;

public interface IDistanceCalculator {

	double calculate(Sample sample, ClassStatisticData classStatisticData);
	
}
