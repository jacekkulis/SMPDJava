package pl.jacekkulis.validator;

import pl.jacekkulis.classifier.IClassifier;
import pl.jacekkulis.model.SampleWithClass;

import java.util.List;

public interface IValidator {

	double validate(IClassifier IClassifier, int numberOfIterations, int percent);

	void splitSamplesIntoTrainingAndTestSets(int percent);
}
