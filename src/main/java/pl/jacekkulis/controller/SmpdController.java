package pl.jacekkulis.controller;

import Jama.Matrix;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.math3.util.Combinations;
import pl.jacekkulis.classifier.Common;
import pl.jacekkulis.model.ModelClass;
import pl.jacekkulis.model.Sample;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
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


    String InData; // dataset from a text file will be placed here
    int ClassCount = 0, FeatureCount = 0;
    double[][] F, FNew; // original feature matrix and transformed feature matrix
    int[] ClassLabels, SampleCount;
    String[] ClassNames;
    private List<ModelClass> classes;

    @FXML
    public void initialize() {
        InData = readDataSet();

        try {
            if (InData != null) {
                getDatasetParameters();
                fillFeatureMatrix();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        for (int i = 1; i < 65; i++) {
            boxDimension.getItems().add(i);
        }
        boxDimension.getSelectionModel().select(0);

        boxSelection.setItems(FXCollections.observableArrayList(new String[]{"Fischer", "SFS"}));
        boxSelection.getSelectionModel().select(0);

        boxClassification.setItems(FXCollections.observableArrayList(new String[]{"NN - Nearest neighbor",
                "NM - Nearest Mean",
                "k-NN - k-Nearest Neighbor",
                "k-NM - k-Nearest Mean"}));
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
        if (F == null) {
            return;
        }

        int numberOfFeaturesToSelect = (int)boxDimension.getSelectionModel().getSelectedItem();
        List<Integer> bestFeatureIndexes = selectBestFeatureIndexes(numberOfFeaturesToSelect);

        classes = new ArrayList<>();
        for (String className : ClassNames) {
            classes.add(new ModelClass(className));
        }

        for (int j = 0; j < F[0].length; j++) {
            List<Double> features = new ArrayList<>();

            for (int featureIndex : bestFeatureIndexes) {
                features.add(F[featureIndex][j]);
            }

            classes.get(ClassLabels[j]).addSample(new Sample(features));
        }

        long duration = System.currentTimeMillis() - start;
        selectionResults.appendText("Time of working: " + duration + " ms\n");
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

    private String readDataSet() {
        String s_tmp, s_out = "";

        try {
            //Maple_Oak.txt
            //Get file from resources folder
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("Maple_Oak.txt").getFile());
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            while ((s_tmp = br.readLine()) != null) s_out += s_tmp + '$';
            br.close();
        } catch (Exception e) {
        }
        return s_out;
    }

    private void getDatasetParameters() throws Exception {
        // based on data stored in InData determine: class count and names, number of samples
        // and number of features; set the corresponding variables
        String stmp = InData, saux = "";
        // analyze the first line and get feature count: assume that number of features
        // equals number of commas
        saux = InData.substring(InData.indexOf(',') + 1, InData.indexOf('$'));
        if (saux.length() == 0) throw new Exception("The first line is empty");
        // saux stores the first line beginning from the first comma
        int count = 0;
        while (saux.indexOf(',') > 0) {
            saux = saux.substring(saux.indexOf(',') + 1);
            count++;
        }
        FeatureCount = count + 1; // the first parameter
        // Determine number of classes, class names and number of samples per class
        boolean New;
        int index = -1;
        List<String> NameList = new ArrayList<String>();
        List<Integer> CountList = new ArrayList<Integer>();
        List<Integer> LabelList = new ArrayList<Integer>();
        while (stmp.length() > 1) {
            saux = stmp.substring(0, stmp.indexOf(' '));
            New = true;
            index++; // new class index
            for (int i = 0; i < NameList.size(); i++)
                if (saux.equals(NameList.get(i))) {
                    New = false;
                    index = i; // class index
                }
            if (New) {
                NameList.add(saux);
                CountList.add(0);
            } else {
                CountList.set(index, CountList.get(index).intValue() + 1);
            }
            LabelList.add(index); // class index for current row
            stmp = stmp.substring(stmp.indexOf('$') + 1);
        }
        // based on results of the above analysis, create variables
        ClassNames = new String[NameList.size()];
        for (int i = 0; i < ClassNames.length; i++)
            ClassNames[i] = NameList.get(i);
        SampleCount = new int[CountList.size()];
        for (int i = 0; i < SampleCount.length; i++)
            SampleCount[i] = CountList.get(i).intValue() + 1;
        ClassLabels = new int[LabelList.size()];
        for (int i = 0; i < ClassLabels.length; i++)
            ClassLabels[i] = LabelList.get(i).intValue();
    }

    private void fillFeatureMatrix() throws Exception {
        // having determined array size and class labels, fills in the feature matrix
        int n = 0;
        String saux, stmp = InData;
        for (int i = 0; i < SampleCount.length; i++)
            n += SampleCount[i];
        if (n <= 0) throw new Exception("no samples found");
        F = new double[FeatureCount][n]; // samples are placed column-wise
        for (int j = 0; j < n; j++) {
            saux = stmp.substring(0, stmp.indexOf('$'));
            saux = saux.substring(stmp.indexOf(',') + 1);
            for (int i = 0; i < FeatureCount - 1; i++) {
                F[i][j] = Double.parseDouble(saux.substring(0, saux.indexOf(',')));
                saux = saux.substring(saux.indexOf(',') + 1);
            }
            F[FeatureCount - 1][j] = Double.parseDouble(saux);
            stmp = stmp.substring(stmp.indexOf('$') + 1);
        }

    }

    private List<Integer> selectBestFeatureIndexes(int numberOfFeaturesToSelect) {
        if (boxSelection.getSelectionModel().getSelectedIndex() == 0) {
            return selectBestFeatureIndexesUsingFisherLinearDiscriminant(numberOfFeaturesToSelect);
        } else if (boxSelection.getSelectionModel().getSelectedIndex() == 1) {
            return selectBestFeatureIndexesUsingSFS(numberOfFeaturesToSelect);
        } else {
            throw new IllegalArgumentException("Selected method is not implemented");
        }
    }

    private List<Integer> selectBestFeatureIndexesUsingFisherLinearDiscriminant(int numberOfFeaturesToSelect) {
        if (numberOfFeaturesToSelect == 1) {
            double FLD = 0, tmp;
            int bestFeatureIndex = -1;
            for (int i = 0; i < FeatureCount; i++) {
                if ((tmp = computeFisherLD(F[i])) > FLD) {
                    FLD = tmp;
                    bestFeatureIndex = i;
                }
            }

            selectionResults.appendText("Best features id: " + bestFeatureIndex + "\n");
            selectionResults.appendText("Val: " + FLD + "\n");

            return asList(bestFeatureIndex);
        } else if (numberOfFeaturesToSelect > 1) {
            int[] bestFeatureIndexes = null;
            double fisherDiscriminant = Double.MIN_VALUE;

            Combinations combinations = new Combinations(F.length, numberOfFeaturesToSelect);
            for (int[] combination : combinations) {
                double tmp = computeFisherLD(F, combination);
                if (tmp > fisherDiscriminant) {
                    fisherDiscriminant = tmp;
                    bestFeatureIndexes = combination;
                }
            }

            List<Integer> listOfBestFeatureIndexes = IntStream.of(bestFeatureIndexes)
                    .boxed()
                    .collect(toList());


            selectionResults.appendText("Best features id: " + listOfBestFeatureIndexes + "\n");
            selectionResults.appendText("Val: " + fisherDiscriminant + "\n");
            return listOfBestFeatureIndexes;
        } else {
            throw new IllegalArgumentException("Illegal number of features <" + numberOfFeaturesToSelect + "> to select");
        }
    }

    private double computeFisherLD(double[] vec) {
        // 1D, 2-classes
        double mA = 0, mB = 0, sA = 0, sB = 0;
        for (int i = 0; i < vec.length; i++) {
            if (ClassLabels[i] == 0) {
                mA += vec[i];
                sA += vec[i] * vec[i];
            } else {
                mB += vec[i];
                sB += vec[i] * vec[i];
            }
        }
        mA /= SampleCount[0];
        mB /= SampleCount[1];
        sA = sA / SampleCount[0] - mA * mA;
        sB = sB / SampleCount[1] - mB * mB;
        return Math.abs(mA - mB) / (Math.sqrt(sA) + Math.sqrt(sB));
    }

    private double computeFisherLD(double[][] featureMatrix, int[] featureIndexes) {
        List<Sample> samplesOfFirstClass = new ArrayList<>();
        List<Sample> samplesOfSecondClass = new ArrayList<>();

        for (int i = 0; i < F[0].length; i++) {
            List<Double> features = new ArrayList<>();
            for (int featureIndex : featureIndexes) {
                features.add(F[featureIndex][i]);
            }

            if (ClassLabels[i] == 0) {
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

    private boolean useSFSOptionIsSelected() {
        return boxSelection.getSelectionModel().getSelectedIndex() == 1;
    }

    private List<Integer> selectBestFeatureIndexesUsingSFS(int numberOfFeaturesToSelect) {
        List<Integer> bestFeatureIndexes = new ArrayList<>(numberOfFeaturesToSelect);

        double FLD = 0, tmp;
        int bestFeatureIndex = -1;
        for (int i = 0; i < FeatureCount; i++) {
            if ((tmp = computeFisherLD(F[i])) > FLD) {
                FLD = tmp;
                bestFeatureIndex = i;
            }
        }
        bestFeatureIndexes.add(bestFeatureIndex);

        for (int i = 1; i < numberOfFeaturesToSelect; i++) {
            double fisherDiscriminant = Double.MIN_VALUE;
            bestFeatureIndexes.add(-1);

            for (int j = 0; j < F.length; j++) {
                if (bestFeatureIndexes.contains(j)) {
                    continue;
                }

                int[] featureIndexes = new int[i + 1];
                for (int k = 0; k < i; k++) {
                    featureIndexes[k] = bestFeatureIndexes.get(k);
                }
                featureIndexes[i] = j;

                tmp = computeFisherLD(F, featureIndexes);
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
