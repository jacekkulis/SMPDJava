package pl.jacekkulis.model;

import Jama.Matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class Sample {

	private final List<Double> features;

	public Sample(List<Double> features) {
		this.features = unmodifiableList(new ArrayList<>(features));
	}
	
	public List<Double> getFeatures() {
		return Collections.unmodifiableList(features);
	}
	
	public Matrix getFeaturesMatrix() {
		double[] featuresArray = new double[features.size()];
		for (int i = 0; i < features.size(); i++) {
			featuresArray[i] = features.get(i);
		}
		
		return new Matrix(featuresArray, 1);
	}

	public double getFeature(int i) {
		return features.get(i);
	}
	
	public int getFeaturesCount() {
		return features.size();
	}

}
