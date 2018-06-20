package pl.jacekkulis.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pl.jacekkulis.classifier.IClassifier;
import pl.jacekkulis.classifier.KNearestNeighbor;
import pl.jacekkulis.classifier.NearestMean;
import pl.jacekkulis.classifier.NearestNeighbor;
import pl.jacekkulis.database.Database;
import pl.jacekkulis.selector.FischerSelection;
import pl.jacekkulis.selector.ISelector;
import pl.jacekkulis.selector.SFSSelection;
import pl.jacekkulis.validator.BootstrapValidator;
import pl.jacekkulis.validator.IValidator;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class SmpdController {

    /*Selection*/
    @FXML
    private ComboBox<Integer> boxDimension;
    @FXML
    private ComboBox<String> boxSelection;
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

    private Database db;

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

        boxClassification.setItems(FXCollections.observableArrayList(new String[]{"NN - Nearest Neighbor", "NM - Nearest Mean", "k-NN - k-Nearest Neighbor"}));
        boxClassification.getSelectionModel().select(0);

        boxValidation.setItems(FXCollections.observableArrayList(new String[]{"Bootstrap"}));
        boxValidation.getSelectionModel().select(0);
    }

    @FXML
    protected  void clearAction(){
        selectionResults.clear();
        classificationResults.clear();
    }

    @FXML
    protected void selectionExecuteAction() {
        Instant start = Instant.now();

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

        selector.setClasses(db, bestFeatureIndexes);

        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        selectionResults.appendText("Time of computing: " + duration + " ms\n");
    }


    IClassifier classifier;
    IValidator validator;

    private void initClassifier(){
        int classificationMethod = (int)boxClassification.getSelectionModel().getSelectedIndex();

        if (classificationMethod == 0){
            classifier = new NearestNeighbor();
        } else if (classificationMethod == 1){
            classifier = new NearestMean();
        } else if (classificationMethod == 2){
            classifier = new KNearestNeighbor();
        } else {
            throw new IllegalStateException("Unsupported index " + classificationMethod);
        }


    }

    private void initValidator(){
        int validationMethod = (int)boxValidation.getSelectionModel().getSelectedIndex();
        if (validationMethod == 0){
            validator = new BootstrapValidator();
        } else if (validationMethod == 1){
            throw new IllegalStateException("Unsupported validator.");
        } else {
            throw new IllegalStateException("Unsupported index " + validationMethod);
        }
    }

    @FXML
    protected void classificiationTrainAction() {
        initValidator();

        int percent = Integer.parseInt(fieldTrainingPart.getText());

        ((BootstrapValidator) validator).setSamples(db.fetchAllSamples());
        validator.splitSamplesIntoTrainingAndTestSets(percent);
    }


    @FXML
    protected void classificationExecuteAction() {
        initClassifier();

        int iterations = Integer.parseInt(fieldValidation.getText());
        int percent = Integer.parseInt(fieldTrainingPart.getText());
        double result = validator.validate(classifier, iterations, percent) * 100;
        classificationResults.appendText(new DecimalFormat("#.#").format(result) + " %\n");
    }

    @FXML
    protected void validationMethodChange() {
        if(boxValidation.getSelectionModel().getSelectedIndex() == 0){
            labValidation.setText("Number of sets:");
        } else if (boxValidation.getSelectionModel().getSelectedIndex() == 1){
            labValidation.setText("No. of iterations:");
        }

    }
}