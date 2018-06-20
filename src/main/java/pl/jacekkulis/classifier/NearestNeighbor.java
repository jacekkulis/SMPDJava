package pl.jacekkulis.classifier;

import pl.jacekkulis.model.ModelClass;
import pl.jacekkulis.model.Sample;
import pl.jacekkulis.model.SampleWithClass;
import pl.jacekkulis.utils.MathUtil;

import java.util.List;

public class NearestNeighbor implements IClassifier {

	private List<SampleWithClass> trainSamples;
	
	@Override
	public void train(List<SampleWithClass> trainSamples) {
		this.trainSamples = trainSamples;
	}

	@Override
	public ModelClass classify(Sample sample) {
		if (!isTrained()) {
			throw new IllegalStateException("IClassifier has to be trained first");
		}
		
		SampleWithClass nearestNeighborSample = findNearestSample(sample);
		return nearestNeighborSample.getModelClass();
	}

	private SampleWithClass findNearestSample(Sample sample) {
		SampleWithClass nearestNeighborSample = null;
		double minimumDistance = Double.MAX_VALUE;
		
		for (SampleWithClass trainSample : trainSamples) {
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
