package pl.jacekkulis.model;

import java.util.List;

public class SampleClass extends Sample {

	private Class aClass;

	public SampleClass(List<Double> features, Class aClass) {
		super(features);
		this.aClass = aClass;
	}
	
	public SampleClass(Sample sample, Class aClass) {
		super(sample.getFeatures());
		this.aClass = aClass;
	}
	
	public Class getaClass() {
		return aClass;
	}

}
