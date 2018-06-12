package pl.jacekkulis.classifier;

import Jama.Matrix;

class ClassStatisticData {

	private Matrix mean;
	private Matrix covarianceMatrix;
	
	ClassStatisticData(Matrix mean, Matrix covarianceMatrix) {
		this.mean = mean;
		this.covarianceMatrix = covarianceMatrix;
	}

	Matrix getMean() {
		return mean;
	}

	Matrix getCovarianceMatrix() {
		return covarianceMatrix;
	}
	
}
