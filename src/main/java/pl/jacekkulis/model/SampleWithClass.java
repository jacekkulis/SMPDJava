package pl.jacekkulis.model;

import java.util.List;

public class SampleWithClass extends Sample {

	private ModelClass modelClass;

	public SampleWithClass(List<Double> features, ModelClass modelClass) {
		super(features);
		this.modelClass = modelClass;
	}
	
	public SampleWithClass(Sample sample, ModelClass modelClass) {
		super(sample.getFeatures());
		this.modelClass = modelClass;
	}
	
	public ModelClass getModelClass() {
		return modelClass;
	}

}
