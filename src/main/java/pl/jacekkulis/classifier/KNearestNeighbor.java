package pl.jacekkulis.classifier;

import pl.jacekkulis.model.Class;
import pl.jacekkulis.model.Sample;
import pl.jacekkulis.model.SampleClass;
import pl.jacekkulis.utils.MathUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toList;

public class KNearestNeighbor implements IClassifier {

	private static final int K = 3;
	
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
		
		List<SampleClass> kNearestSamples = findKNearestSamplesTo(sample, K);
		return findTheMostCommonClassOf(kNearestSamples);
	}

	private List<SampleClass> findKNearestSamplesTo(Sample sample, int k) {
		List<SampleDistance> result = new ArrayList<>();
		
		for (SampleClass trainSample : trainSamples) {
			double euclideanDistance = MathUtil.calculateEuclideanDistance(trainSample, sample);
			SampleDistance sampleDistance = new SampleDistance(trainSample, euclideanDistance);
			result.add(sampleDistance);
		}
		
		return result
			.stream()
			.sorted((a, b) -> Double.compare(a.distance, b.distance))
			.map(s -> s.sample)
			.collect(toList())
			.subList(0, k);
	}
	
	private Class findTheMostCommonClassOf(List<SampleClass> samples) {
		Map<Class, Integer> map = new HashMap<>();

	    for (SampleClass sample : samples) {
	        Integer val = map.get(sample.getaClass());
	        map.put(sample.getaClass(), val == null ? 1 : val + 1);
	    }

	    Entry<Class, Integer> max = null;

	    for (Entry<Class, Integer> e : map.entrySet()) {
	        if (max == null || e.getValue() > max.getValue())
	            max = e;
	    }

	    return max.getKey();
	}

	@Override
	public boolean isTrained() {
		return trainSamples != null;
	}
	
	private static class SampleDistance {
		
		SampleClass sample;
		double distance;
		
		SampleDistance(SampleClass sample, double distance) {
			this.sample = sample;
			this.distance = distance;
		}
	}

}
