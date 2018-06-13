package pl.jacekkulis.controller;

import Jama.Matrix;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.math3.util.Combinations;
import pl.jacekkulis.database.Database;
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
        List<Integer> bestFeatureIndexes = selectBestFeatureIndexes(method, numberOfFeaturesToSelect);

        db.setClasses(new ArrayList<>());
        //classes = new ArrayList<>();

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


//    private void getDatasetParameters() throws Exception {
//        // based on data stored in InData determine: class count and names, number of samples
//        // and number of features; set the corresponding variables
//        String stmp = InData, saux = "";
//        // analyze the first line and get feature count: assume that number of features
//        // equals number of commas
//        saux = InData.substring(InData.indexOf(',') + 1, InData.indexOf('$'));
//        if (saux.length() == 0) throw new Exception("The first line is empty");
//        // saux stores the first line beginning from the first comma
//        int count = 0;
//        while (saux.indexOf(',') > 0) {
//            saux = saux.substring(saux.indexOf(',') + 1);
//            count++;
//        }
//        FeatureCount = count + 1; // the first parameter
//        // Determine number of classes, class names and number of samples per class
//        boolean classExists;
//        int index = -1;
//        List<String> NameList = new ArrayList<String>();
//        List<Integer> CountList = new ArrayList<Integer>();
//        List<Integer> LabelList = new ArrayList<Integer>();
//        while (stmp.length() > 1) {
//            saux = stmp.substring(0, stmp.indexOf(' '));
//            classExists = true;
//            index++; // new class index
//            for (int i = 0; i < NameList.size(); i++)
//                if (saux.equals(NameList.get(i))) {
//                    classExists = false;
//                    index = i; // class index
//                }
//            if (classExists) {
//                NameList.add(saux);
//                CountList.add(0);
//            } else {
//                CountList.set(index, CountList.get(index).intValue() + 1);
//            }
//            LabelList.add(index); // class index for current row
//            stmp = stmp.substring(stmp.indexOf('$') + 1);
//        }
//
//
//        // based on results of the above analysis, create variables
//        ClassNames = new String[NameList.size()];
//        for (int i = 0; i < ClassNames.length; i++)
//            ClassNames[i] = NameList.get(i);
//
//        SampleCount = new int[CountList.size()];
//        for (int i = 0; i < SampleCount.length; i++)
//            SampleCount[i] = CountList.get(i).intValue() + 1;
//
//        ClassLabels = new int[LabelList.size()];
//        for (int i = 0; i < ClassLabels.length; i++)
//            ClassLabels[i] = LabelList.get(i).intValue();
//    }
//
//    private void fillFeatureMatrix() throws Exception {
//        // having determined array size and class labels, fills in the feature matrix
//        int n = 0;
//        String saux, stmp = InData;
//        for (int i = 0; i < SampleCount.length; i++)
//            n += SampleCount[i];
//        if (n <= 0) throw new Exception("no samples found");
//        features = new double[FeatureCount][n]; // samples are placed column-wise
//        for (int j = 0; j < n; j++) {
//            saux = stmp.substring(0, stmp.indexOf('$'));
//            saux = saux.substring(stmp.indexOf(',') + 1);
//            for (int i = 0; i < FeatureCount - 1; i++) {
//                features[i][j] = Double.parseDouble(saux.substring(0, saux.indexOf(',')));
//                saux = saux.substring(saux.indexOf(',') + 1);
//            }
//            features[FeatureCount - 1][j] = Double.parseDouble(saux);
//            stmp = stmp.substring(stmp.indexOf('$') + 1);
//        }
//
//    }

    private List<Integer> selectBestFeatureIndexes(int method, int dimension) {
        if (method == 0) {
            return selectBestFeatureIndexesUsingFisher(dimension);
        } else if (method == 1) {
            return selectBestFeatureIndexesUsingSFS(dimension);
        } else {
            throw new IllegalArgumentException("Selected method is not implemented");
        }
    }

    private List<Integer> selectBestFeatureIndexesUsingFisher(int dimension) {
        selectionResults.appendText("Fischer method\n");
        if (dimension == 1) {
            double FLD = 0, tmp;
            int bestFeatureIndex = -1;
            for (int i = 0; i < db.getFeatureCount(); i++) {
                if ((tmp = selectBestFeatureUsingFischerFor1D(db.getFeatures()[i])) > FLD) {
                    FLD = tmp;
                    bestFeatureIndex = i;
                }
            }

            selectionResults.appendText("Best features id: " + bestFeatureIndex + "\n");
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

            selectionResults.appendText("Best features id: " + listOfBestFeatureIndexes + "\n");
            return listOfBestFeatureIndexes;
        } else {
            throw new IllegalArgumentException("Illegal number of features <" + dimension + "> to select");
        }
    }

    private double selectBestFeatureUsingFischerFor1D(double[] values) {
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
    private double calculateFischerFor2DOrMore(int[] featureIndexes) {
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

    private List<Integer> selectBestFeatureIndexesUsingSFS(int dimension) {
        selectionResults.appendText("SFS method\n");
        List<Integer> bestFeatureIndexes = new ArrayList<>(dimension);
        double FLD = 0, tmp;
        int bestFeatureIndex = -1;
        for (int i = 0; i < db.getFeatureCount(); i++) {
            if ((tmp = selectBestFeatureUsingFischerFor1D(db.getFeatures()[i])) > FLD) {
                FLD = tmp;
                bestFeatureIndex = i;
            }
        }
        bestFeatureIndexes.add(bestFeatureIndex);

        for (int i = 1; i < dimension; i++) {
            double fisherDiscriminant = Double.MIN_VALUE;
            bestFeatureIndexes.add(-1);

            for (int j = 0; j < db.getFeatures().length; j++) {
                if (bestFeatureIndexes.contains(j)) {
                    continue;
                }

                int[] featureIndexes = new int[i + 1];
                for (int k = 0; k < i; k++) {
                    featureIndexes[k] = bestFeatureIndexes.get(k);
                }
                featureIndexes[i] = j;

                tmp = calculateFischerFor2DOrMore(featureIndexes);
                if (tmp > fisherDiscriminant) {
                    fisherDiscriminant = tmp;
                    bestFeatureIndexes.set(i, j);
                }
            }
        }

        selectionResults.appendText("Best features id: " + bestFeatureIndexes + "\n");
        return bestFeatureIndexes;
    }
}