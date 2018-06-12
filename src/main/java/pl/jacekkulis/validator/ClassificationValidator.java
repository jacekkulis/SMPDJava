package pl.jacekkulis.validator;

import pl.jacekkulis.classifier.Classifier;
import pl.jacekkulis.model.SampleWithClass;

import java.util.List;

public interface ClassificationValidator {

	double validate(Classifier classifier, List<SampleWithClass> samples);
	
}
