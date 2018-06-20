package pl.jacekkulis.classifier;

import pl.jacekkulis.model.Class;
import pl.jacekkulis.model.Sample;
import pl.jacekkulis.model.SampleClass;
import pl.jacekkulis.utils.MathUtil;

import java.util.List;

public class NearestNeighbor implements IClassifier {

	private List<SampleClass> trainSamples;
	
	@Override
	public void train(List<SampleClass> trainSamples) {
		this.trainSamples = trainSamples;
	}

	@Override
	public Class classify(Sample sample) {
		if (!isTrained()) {
			throw new IllegalStateException("IClassifier has to be trained first");
		}
		
		SampleClass nearestNeighborSample = findNearestSample(sample);
		return nearestNeighborSample.getaClass();
	}

	private SampleClass findNearestSample(Sample sample) {
		SampleClass nearestNeighborSample = null;
		double minimumDistance = Double.MAX_VALUE;
		
		for (SampleClass trainSample : trainSamples) {
			double distance = MathUtil.calculateEuclideanDistance(sample, trainSample);
			
			if (distance < minimumDistance) {
				minimumDistance = distance;
				nearestNeighborSample = trainSample;
			}
		}
		
		return nearestNeighborSample;
	}

	@Override
	public boolean isTrained() {
		return trainSamples != null;
	}

}
