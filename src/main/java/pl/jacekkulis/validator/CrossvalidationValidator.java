package pl.jacekkulis.validator;

import pl.jacekkulis.classifier.IClassifier;
import pl.jacekkulis.exception.MatrixIrreversibleException;
import pl.jacekkulis.model.ModelClass;
import pl.jacekkulis.model.SampleWithClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrossvalidationValidator implements IValidator {

	private final int numberOfSets;
	
	private List<SamplesSet> sets = new ArrayList<>();
	
	public CrossvalidationValidator(int numberOfSets) {
		this.numberOfSets = numberOfSets;
	}
	
	@Override
	public double validate(IClassifier IClassifier, List<SampleWithClass> samples) {
		while (true) {
			try {
				List<Double> results = new ArrayList<>();
				splitSamplesIntoSets(samples);
				
				for (SamplesSet testSet : sets) {
					results.add(validateClassifierUsingTestSet(IClassifier, testSet));
				}
				
				return averageOf(results);
			} catch (MatrixIrreversibleException e) { }
		}
	}
	
	private void splitSamplesIntoSets(List<SampleWithClass> samples) {
		int numberOfElementsPerSet = Math.round(samples.size() / (float)numberOfSets);
		int position = 0;
		sets = new ArrayList<>();
		
		Collections.shuffle(samples);
		
		do {
			int endPosition = Math.min(position + numberOfElementsPerSet, samples.size());
			sets.add(new SamplesSet(samples.subList(position, endPosition)));
			
			position += numberOfElementsPerSet;
		} while (position < samples.size());
	}

	private double validateClassifierUsingTestSet(IClassifier IClassifier, SamplesSet testSet) {
		List<SampleWithClass> trainSamples = trainingSamplesWithout(testSet);
		IClassifier.train(trainSamples);
		
		return testClassifier(IClassifier, testSet);
	}
	
	private List<SampleWithClass> trainingSamplesWithout(SamplesSet testSet) {
		List<SampleWithClass> trainingSamples = new ArrayList<>();
		
		for (SamplesSet set : sets) {
			if (testSet != set) {
				trainingSamples.addAll(set.samples);
			}
		}
		
		return trainingSamples;
	}
	
	private double testClassifier(IClassifier IClassifier, SamplesSet testSet) {
		int numberOfValidClassifications = 0;
		
		for (SampleWithClass sample : testSet.samples) {
			ModelClass modelClass = IClassifier.classify(sample);
			if (modelClass.equals(sample.getModelClass())) {
				numberOfValidClassifications += 1;
			}
		}
		
		return (double)numberOfValidClassifications / testSet.samples.size();
	}

	private double averageOf(List<Double> results) {
		return results
			.stream()
			.mapToDouble(r -> r)
			.summaryStatistics()
			.getAverage();
	}

	
	private static class SamplesSet {
		
		List<SampleWithClass> samples;

		SamplesSet(List<SampleWithClass> samples) {
			this.samples = samples;
		}
		
	}

}
