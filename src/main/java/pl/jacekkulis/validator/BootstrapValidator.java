package pl.jacekkulis.validator;

import pl.jacekkulis.classifier.IClassifier;
import pl.jacekkulis.exception.MatrixIrreversibleException;
import pl.jacekkulis.model.ModelClass;
import pl.jacekkulis.model.SampleWithClass;

import java.util.*;

public class BootstrapValidator implements IValidator {

	private List<SampleWithClass> trainingSamples = new ArrayList<SampleWithClass>();
	private List<SampleWithClass> testSamples = new ArrayList<SampleWithClass>();

    private List<SampleWithClass> samples;
	
	public BootstrapValidator() {
	}

	@Override
	public double validate(IClassifier classifier, int numberOfIterations, int percent) {
		List<Double> results = new ArrayList<>();

		int i = 0;
		while (i < numberOfIterations) {
            splitSamplesIntoTrainingAndTestSets(percent);
			try {
                classifier.train(trainingSamples);
				results.add(testClassifier(classifier));
				
				i += 1;
			} catch (MatrixIrreversibleException e) {
				System.out.println(e.getMessage());
			}
		}
		
		return averageOf(results);
	}

	@Override
	public void splitSamplesIntoTrainingAndTestSets(int percent) {
		trainingSamples = new ArrayList<>();
		testSamples = new ArrayList<>();
		Set<Integer> usedIndexes = new TreeSet<>();
		Random random = new Random();

		int trainSize = samples.size() * percent/100;
        System.out.println("TrainSize: " + trainSize);

		for (int i = 0; i < trainSize; i++) {
			int index = random.nextInt(samples.size());
			trainingSamples.add(samples.get(index));
			usedIndexes.add(index);
		}

		int testSize = samples.size() - trainSize;
        System.out.println("Testsize: " + testSize);
		for (int index = 0; index < testSize; index++) {
			if (!usedIndexes.contains(index)) {
				testSamples.add(samples.get(index));
			}
		}
	}
	
	private double testClassifier(IClassifier IClassifier) {
		int numberOfValidClassifications = 0;
		
		for (SampleWithClass sample : testSamples) {
			ModelClass modelClass = IClassifier.classify(sample);
			if (modelClass.equals(sample.getModelClass())) {
				numberOfValidClassifications += 1;
			}
		}
		
		return (double)numberOfValidClassifications / testSamples.size();
	}

	private double averageOf(List<Double> results) {
		double sum = results
			.stream()
			.mapToDouble(r -> r)
			.sum();
		
		return sum / results.size();
	}

    public void setSamples(List<SampleWithClass> samples) {
        this.samples = samples;
    }
}
