package pl.jacekkulis.selector;

import Jama.Matrix;
import org.apache.commons.math3.util.Combinations;
import pl.jacekkulis.database.Database;
import pl.jacekkulis.model.Sample;
import pl.jacekkulis.utils.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class FischerSelection implements ISelector{

    private Database db;

    public FischerSelection(Database database) {
        db = database;
    }

    public List<Integer> select(int dimension) {
        if (dimension == 1) {
            double FLD = 0, tmp;
            int bestFeatureIndex = -1;
            for (int i = 0; i < db.getFeatureCount(); i++) {
                if ((tmp = selectBestFeatureUsingFischerFor1D(db.getFeatures()[i])) > FLD) {
                    FLD = tmp;
                    bestFeatureIndex = i;
                }
            }

            return asList(bestFeatureIndex);
        } else if (dimension > 1) {
            int[] bestFeatureIndexes = null;
            double fisherDiscriminant = Double.MIN_VALUE;

            Combinations combinations = new Combinations(db.getFeatures().length, dimension);
            for (int[] combination : combinations) {
                double tmp = calculateFischerFor2DOrMore(combination);
                if (tmp > fisherDiscriminant) {
                    fisherDiscriminant = tmp;
                    bestFeatureIndexes = combination;
                }
            }

            List<Integer> listOfBestFeatureIndexes = IntStream.of(bestFeatureIndexes)
                    .boxed()
                    .collect(toList());

            return listOfBestFeatureIndexes;
        } else {
            throw new IllegalArgumentException("Illegal number of features <" + dimension + "> to select");
        }
    }

    public double selectBestFeatureUsingFischerFor1D(double[] values) {
        double avgA = 0, avgB = 0, stdA = 0, stdB = 0;
        for (int i = 0; i < values.length; i++) {
            if (db.getClassLabels()[i] == 0) {
                avgA += values[i];
                stdA += values[i] * values[i];
            } else {
                avgB += values[i];
                stdB += values[i] * values[i];
            }
        }


        avgA /= db.getSampleCount()[0];
        avgB /= db.getSampleCount()[1];
        stdA = stdA / db.getSampleCount()[0] - avgA * avgA;
        stdB = stdB / db.getSampleCount()[1] - avgB * avgB;
        return Math.abs(avgA - avgB) / (Math.sqrt(stdA) + Math.sqrt(stdB));
    }

    /**
     *  Calculates fischer value for features passed as parameter.
     * @param featureIndexes Indexes of features to calculate.
     * @return Best feature fischer value
     */
    public double calculateFischerFor2DOrMore(int[] featureIndexes) {
        List<Sample> samplesOfFirstClass = new ArrayList<>();
        List<Sample> samplesOfSecondClass = new ArrayList<>();

        for (int i = 0; i < db.getFeatures()[0].length; i++) {
            List<Double> features = new ArrayList<>();
            for (int featureIndex : featureIndexes) {
                features.add(this.db.getFeatures()[featureIndex][i]);
            }

            if (db.getClassLabels()[i] == 0) {
                samplesOfFirstClass.add(new Sample(features));
            } else {
                samplesOfSecondClass.add(new Sample(features));
            }
        }

        Matrix meanOfFirstClass = Common.calculateMean(samplesOfFirstClass);
        Matrix meanOfSecondClass = Common.calculateMean(samplesOfSecondClass);

        Matrix covarianceMatrixOfFirstClass = Common.calculateCovarianceMatrix(samplesOfFirstClass, meanOfFirstClass);
        Matrix covarianceMatrixOfSecondClass = Common.calculateCovarianceMatrix(samplesOfSecondClass, meanOfSecondClass);

        return Common.calculateEuclideanDistance(meanOfFirstClass, meanOfSecondClass) / (covarianceMatrixOfFirstClass.det() + covarianceMatrixOfSecondClass.det());
    }
}
