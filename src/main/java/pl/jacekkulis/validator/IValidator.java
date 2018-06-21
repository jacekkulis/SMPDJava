package pl.jacekkulis.validator;

import pl.jacekkulis.classifier.IClassifier;

public interface IValidator {

	double validate(IClassifier IClassifier, int numberOfIterations, int percent);

	void setTestAndTrainingSet(int percent);
}
