package pl.jacekkulis.utils;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import pl.jacekkulis.exception.MatrixIrreversibleException;
import pl.jacekkulis.model.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MathUtil {

	/**
	  * The difference between 1 and the smallest exactly representable number
	  * greater than one. Gives an upper bound on the relative error due to
	  * rounding of floating point numbers.
	  */
	private static final double MACHEPS = 2E-16;
	
	private MathUtil() {}
	
	public static double calculateEuclideanDistance(Sample first, Sample second) {
		return calculateEuclideanDistance(first.getFeaturesMatrix(), second.getFeaturesMatrix());
	}

	public static double calculateEuclideanDistance(Matrix first, Matrix second) {
		Matrix matrix = first.minus(second);
		matrix.arrayTimesEquals(matrix);
		
		double[][] features = matrix.getArray();
		double result = 0.0;
		for (int i = 0; i < matrix.getColumnDimension(); i++) {
			result += features[0][i];
		}
		
		return Math.sqrt(result);
	}
	
	public static double calculateMahalonobisDistance(Sample sample, ClassStatisticData classStatisticData) {
		Matrix invertedCovarianceMatrix;
		try {
			invertedCovarianceMatrix = classStatisticData.getCovarianceMatrix().inverse();
		} catch (RuntimeException exception) {
			invertedCovarianceMatrix = pseudoinverseMoorePenrose(classStatisticData.getCovarianceMatrix());
			if (invertedCovarianceMatrix == null) {
				throw new MatrixIrreversibleException();
			}
		}
		
		Matrix matrix = sample.getFeaturesMatrix().minusEquals(classStatisticData.getMean());
		matrix = matrix
				.times(invertedCovarianceMatrix)
				.times(matrix.transpose());

		return matrix.getArray()[0][0];
	}
	
	public static Matrix calculateMean(List<? extends Sample> samples) {
		Matrix meanMatrix = samples
			.stream()
			.map(Sample::getFeaturesMatrix)
			.reduce((a, b) -> a.plus(b))
			.get();
		
		return meanMatrix.times(1.0 / samples.size());
	}
	
	public static Matrix calculateCovarianceMatrix(List<? extends Sample> samples, Matrix mean) {
		List<Matrix> matrixes = new ArrayList<>();
		for (Sample sample : samples) {
			Matrix matrix = sample.getFeaturesMatrix();
			matrix = matrix.minus(mean);
			matrix = matrix.transpose().times(matrix);
			
			matrixes.add(matrix);
		}
		
		Matrix covarianceMatrix = matrixes
			.stream()
			.reduce((a, b) -> a.plus(b))
			.get();
		
		covarianceMatrix.timesEquals(1.0 / samples.size());
		return covarianceMatrix;
	}
	
	 /**
	  * Computes the Moore�Penrose pseudoinverse using the SVD method.
	  * copied from http://the-lost-beauty.blogspot.com/2009/04/moore-penrose-pseudoinverse-in-jama.html
	  */
	public static Matrix pseudoinverseMoorePenrose(Matrix x) {
		int rows = x.getRowDimension();
		int cols = x.getColumnDimension();
		if (rows < cols) {
			Matrix result = pseudoinverseMoorePenrose(x.transpose());
			if (result != null)
				result = result.transpose();
			return result;
		}
		SingularValueDecomposition svdX = new SingularValueDecomposition(x);
		if (svdX.rank() < 1)
			return null;
		double[] singularValues = svdX.getSingularValues();
		double tol = Math.max(rows, cols) * singularValues[0] * MACHEPS;
		double[] singularValueReciprocals = new double[singularValues.length];
		for (int i = 0; i < singularValues.length; i++)
			if (Math.abs(singularValues[i]) >= tol)
				singularValueReciprocals[i] = 1.0 / singularValues[i];
		double[][] u = svdX.getU().getArray();
		double[][] v = svdX.getV().getArray();
		int min = Math.min(cols, u[0].length);
		double[][] inverse = new double[cols][rows];
		for (int i = 0; i < cols; i++)
			for (int j = 0; j < u.length; j++)
				for (int k = 0; k < min; k++)
					inverse[i][j] += v[i][k] * singularValueReciprocals[k]
							* u[j][k];
		return new Matrix(inverse);
	}
}