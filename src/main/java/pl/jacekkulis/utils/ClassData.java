package pl.jacekkulis.utils;

import Jama.Matrix;

public class ClassData {

	private Matrix mean;
	private Matrix covarianceMatrix;
	
	public ClassData(Matrix mean, Matrix covarianceMatrix) {
		this.mean = mean;
		this.covarianceMatrix = covarianceMatrix;
	}

	public Matrix getMean() {
		return mean;
	}

	public Matrix getCovarianceMatrix() {
		return covarianceMatrix;
	}
	
}
