package pl.jacekkulis.classifier;

import pl.jacekkulis.model.ModelClass;
import pl.jacekkulis.model.Sample;
import pl.jacekkulis.model.SampleWithClass;

import java.util.List;

public interface Classifier {

	void train(List<SampleWithClass> trainSamples);
	
	ModelClass classify(Sample sample);
	
	boolean isTrained();
	
}
