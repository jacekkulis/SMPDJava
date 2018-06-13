package pl.jacekkulis.controller;

import Jama.Matrix;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.math3.util.Combinations;
import pl.jacekkulis.database.Database;
import pl.jacekkulis.selector.FischerSelection;
import pl.jacekkulis.selector.ISelector;
import pl.jacekkulis.selector.SFSSelection;
import pl.jacekkulis.utils.Common;
import pl.jacekkulis.model.ModelClass;
import pl.jacekkulis.model.Sample;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class SmpdController {

    /*Selection*/
    @FXML
    private ComboBox boxDimension;
    @FXML
    private ComboBox boxSelection;
    @FXML
    private Button btnSelectionExecute;
    @FXML
    private TextArea selectionResults;
    /*Classification*/
    @FXML
    private ComboBox boxClassification;
    @FXML
    private TextField fieldTrainingPart;
    @FXML
    private Button btnClassificationExecute;
    @FXML
    private TextArea classificationResults;
    /*Validation*/
    @FXML
    private ComboBox boxValidation;
    @FXML
    private TextField fieldValidation;
    @FXML
    private Label labValidation;
    @FXML
    private Button btnValidationExecute;
    @FXML
    private TextArea validationResults;

    private Database db;

    double[][] FNew;

    @FXML
    public void initialize() {
        db = new Database();
        initBoxes();
    }

    private void initBoxes(){
        for (int i = 1; i < 65; i++) {
            boxDimension.getItems().add(i);
        }
        boxDimension.getSelectionModel().select(0);

        boxSelection.setItems(FXCollections.observableArrayList(new String[]{"Fischer", "SFS"}));
        boxSelection.getSelectionModel().select(0);

        boxClassification.setItems(FXCollections.observableArrayList(new String[]{"NN - Nearest Neighbor", "NM - Nearest Mean", "k-NN - k-Nearest Neighbor", "k-NM - k-Nearest Mean"}));
        boxClassification.getSelectionModel().select(0);

        boxValidation.setItems(FXCollections.observableArrayList(new String[]{"Bootstrap", "Crossvalidation"}));
        boxValidation.getSelectionModel().select(0);
    }

    @FXML
    protected  void clearAction(){
        selectionResults.clear();
        classificationResults.clear();
        validationResults.clear();
    }

    @FXML
    protected void selectionExecuteAction() {
        long start = System.currentTimeMillis();
        if (db.getFeatures() == null) {
            return;
        }

        int numberOfFeaturesToSelect = (int)boxDimension.getSelectionModel().getSelectedItem();
        int method = boxSelection.getSelectionModel().getSelectedIndex();

        ISelector selector;
        if (method == 0){
            selectionResults.appendText("Fischer method\n");
            selector = new FischerSelection(db);
        } else {
            selectionResults.appendText("SFS method\n");
            selector = new SFSSelection(db);
        }

        List<Integer> bestFeatureIndexes = selector.select(numberOfFeaturesToSelect);
        selectionResults.appendText("Best features id: " + bestFeatureIndexes + "\n");

        db.setClasses(new ArrayList<>());

        for (String className : db.getClassNames()) {
            db.getClasses().add(new ModelClass(className));
        }

        for (int j = 0; j < db.getFeatures()[0].length; j++) {
            List<Double> features = new ArrayList<>();

            for (int featureIndex : bestFeatureIndexes) {
                features.add(this.db.getFeatures()[featureIndex][j]);
            }

            db.getClasses().get(db.getClassLabels()[j]).addSample(new Sample(features));
        }

        long duration = System.currentTimeMillis() - start;
        selectionResults.appendText("Time of computing: " + duration + " ms\n");
    }


    @FXML
    protected void classificiationTrainAction() {

    }

    @FXML
    protected void classificationExecuteAction() {

    }

    @FXML
    protected void validationExecuteAction() {

    }

    @FXML
    protected void validationMethodChange() {
        if(boxValidation.getSelectionModel().getSelectedIndex() == 0){
            labValidation.setText("Number of sets:");
        } else if (boxValidation.getSelectionModel().getSelectedIndex() == 1){
            labValidation.setText("No. of iterations:");
        }

    }

//    private List<Integer> selectBestFeatureIndexes(int method, int dimension) {
//        if (method == 0) {
//            return selectBestFeatureIndexesUsingFisher(dimension);
//        } else if (method == 1) {
//            return selectBestFeatureIndexesUsingSFS(dimension);
//        } else {
//            throw new IllegalArgumentException("Selected method is not implemented");
//        }
//    }

//    private List<Integer> selectBestFeatureIndexesUsingFisher(int dimension) {
//        selectionResults.appendText("Fischer method\n");
//        if (dimension == 1) {
//            double FLD = 0, tmp;
//            int bestFeatureIndex = -1;
//            for (int i = 0; i < db.getFeatureCount(); i++) {
//                if ((tmp = selectBestFeatureUsingFischerFor1D(db.getFeatures()[i])) > FLD) {
//                    FLD = tmp;
//                    bestFeatureIndex = i;
//                }
//            }
//
//            selectionResults.appendText("Best features id: " + bestFeatureIndex + "\n");
//            return asList(bestFeatureIndex);
//        } else if (dimension > 1) {
//            int[] bestFeatureIndexes = null;
//            double fisherDiscriminant = Double.MIN_VALUE;
//
//            Combinations combinations = new Combinations(db.getFeatures().length, dimension);
//            for (int[] combination : combinations) {
//                double tmp = calculateFischerFor2DOrMore(combination);
//                if (tmp > fisherDiscriminant) {
//                    fisherDiscriminant = tmp;
//                    bestFeatureIndexes = combination;
//                }
//            }
//
//            List<Integer> listOfBestFeatureIndexes = IntStream.of(bestFeatureIndexes)
//                    .boxed()
//                    .collect(toList());
//
//            selectionResults.appendText("Best features id: " + listOfBestFeatureIndexes + "\n");
//            return listOfBestFeatureIndexes;
//        } else {
//            throw new IllegalArgumentException("Illegal number of features <" + dimension + "> to select");
//        }
//    }
//
//    private double selectBestFeatureUsingFischerFor1D(double[] values) {
//        double avgA = 0, avgB = 0, stdA = 0, stdB = 0;
//        for (int i = 0; i < values.length; i++) {
//            if (db.getClassLabels()[i] == 0) {
//                avgA += values[i];
//                stdA += values[i] * values[i];
//            } else {
//                avgB += values[i];
//                stdB += values[i] * values[i];
//            }
//        }
//
//
//        avgA /= db.getSampleCount()[0];
//        avgB /= db.getSampleCount()[1];
//        stdA = stdA / db.getSampleCount()[0] - avgA * avgA;
//        stdB = stdB / db.getSampleCount()[1] - avgB * avgB;
//        return Math.abs(avgA - avgB) / (Math.sqrt(stdA) + Math.sqrt(stdB));
//    }
//
//    /**
//     *  Calculates fischer value for features passed as parameter.
//     * @param featureIndexes Indexes of features to calculate.
//     * @return Best feature fischer value
//     */
//    private double calculateFischerFor2DOrMore(int[] featureIndexes) {
//        List<Sample> samplesOfFirstClass = new ArrayList<>();
//        List<Sample> samplesOfSecondClass = new ArrayList<>();
//
//        for (int i = 0; i < db.getFeatures()[0].length; i++) {
//            List<Double> features = new ArrayList<>();
//            for (int featureIndex : featureIndexes) {
//                features.add(this.db.getFeatures()[featureIndex][i]);
//            }
//
//            if (db.getClassLabels()[i] == 0) {
//                samplesOfFirstClass.add(new Sample(features));
//            } else {
//                samplesOfSecondClass.add(new Sample(features));
//            }
//        }
//
//        Matrix meanOfFirstClass = Common.calculateMean(samplesOfFirstClass);
//        Matrix meanOfSecondClass = Common.calculateMean(samplesOfSecondClass);
//
//        Matrix covarianceMatrixOfFirstClass = Common.calculateCovarianceMatrix(samplesOfFirstClass, meanOfFirstClass);
//        Matrix covarianceMatrixOfSecondClass = Common.calculateCovarianceMatrix(samplesOfSecondClass, meanOfSecondClass);
//
//        return Common.calculateEuclideanDistance(meanOfFirstClass, meanOfSecondClass) / (covarianceMatrixOfFirstClass.det() + covarianceMatrixOfSecondClass.det());
//    }
//
//    private List<Integer> selectBestFeatureIndexesUsingSFS(int dimension) {
//        selectionResults.appendText("SFS method\n");
//        List<Integer> bestFeatureIndexes = new ArrayList<>(dimension);
//        double FLD = 0, tmp;
//        int bestFeatureIndex = -1;
//        for (int i = 0; i < db.getFeatureCount(); i++) {
//            if ((tmp = selectBestFeatureUsingFischerFor1D(db.getFeatures()[i])) > FLD) {
//                FLD = tmp;
//                bestFeatureIndex = i;
//            }
//        }
//        bestFeatureIndexes.add(bestFeatureIndex);
//
//        for (int i = 1; i < dimension; i++) {
//            double fisherDiscriminant = Double.MIN_VALUE;
//            bestFeatureIndexes.add(-1);
//
//            for (int j = 0; j < db.getFeatures().length; j++) {
//                if (bestFeatureIndexes.contains(j)) {
//                    continue;
//                }
//
//                int[] featureIndexes = new int[i + 1];
//                for (int k = 0; k < i; k++) {
//                    featureIndexes[k] = bestFeatureIndexes.get(k);
//                }
//                featureIndexes[i] = j;
//
//                tmp = calculateFischerFor2DOrMore(featureIndexes);
//                if (tmp > fisherDiscriminant) {
//                    fisherDiscriminant = tmp;
//                    bestFeatureIndexes.set(i, j);
//                }
//            }
//        }
//
//        selectionResults.appendText("Best features id: " + bestFeatureIndexes + "\n");
//        return bestFeatureIndexes;
//    }
}