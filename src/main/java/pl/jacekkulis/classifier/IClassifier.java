package pl.jacekkulis.classifier;

import pl.jacekkulis.model.Class;
import pl.jacekkulis.model.Sample;
import pl.jacekkulis.model.SampleClass;

import java.util.List;

public interface IClassifier {

	void train(List<SampleClass> trainSamples);

	Class classify(Sample sample);

	boolean isTrained();
	
}
