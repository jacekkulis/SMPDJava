package pl.jacekkulis.database;

import pl.jacekkulis.model.ModelClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private String dataFromFile;
    public int ClassCount = 0, FeatureCount = 0;
    public double[][] features, FNew; // original feature matrix and transformed feature matrix
    public int[] ClassLabels, SampleCount;
    public String[] ClassNames;
    public List<ModelClass> classes;

    public Database() {
        dataFromFile = readDataFromFile("Maple_Oak.txt");
        try {
            getDatasetParameters();
            fillFeatureMatrix();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public String readDataFromFile(String filename) {
        String s_tmp, s_out = "";
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(filename).getFile());
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            while ((s_tmp = br.readLine()) != null) s_out += s_tmp + '$';
            br.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return s_out;
    }


    private void getDatasetParameters() throws Exception {
        // based on data stored in dataFromFile determine: class count and names, number of samples
        // and number of features; set the corresponding variables
        String stmp = dataFromFile, saux = "";
        // analyze the first line and get feature count: assume that number of features
        // equals number of commas
        saux = dataFromFile.substring(dataFromFile.indexOf(',') + 1, dataFromFile.indexOf('$'));
        if (saux.length() == 0) throw new Exception("The first line is empty");
        // saux stores the first line beginning from the first comma
        int count = 0;
        while (saux.indexOf(',') > 0) {
            saux = saux.substring(saux.indexOf(',') + 1);
            count++;
        }
        FeatureCount = count + 1; // the first parameter
        // Determine number of classes, class names and number of samples per class
        boolean classExists;
        int index = -1;
        List<String> NameList = new ArrayList<String>();
        List<Integer> CountList = new ArrayList<Integer>();
        List<Integer> LabelList = new ArrayList<Integer>();
        while (stmp.length() > 1) {
            saux = stmp.substring(0, stmp.indexOf(' '));
            classExists = true;
            index++; // new class index
            for (int i = 0; i < NameList.size(); i++)
                if (saux.equals(NameList.get(i))) {
                    classExists = false;
                    index = i; // class index
                }
            if (classExists) {
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
        String saux, stmp = dataFromFile;
        for (int i = 0; i < SampleCount.length; i++)
            n += SampleCount[i];
        if (n <= 0) throw new Exception("no samples found");
        features = new double[FeatureCount][n]; // samples are placed column-wise
        for (int j = 0; j < n; j++) {
            saux = stmp.substring(0, stmp.indexOf('$'));
            saux = saux.substring(stmp.indexOf(',') + 1);
            for (int i = 0; i < FeatureCount - 1; i++) {
                features[i][j] = Double.parseDouble(saux.substring(0, saux.indexOf(',')));
                saux = saux.substring(saux.indexOf(',') + 1);
            }
            features[FeatureCount - 1][j] = Double.parseDouble(saux);
            stmp = stmp.substring(stmp.indexOf('$') + 1);
        }

    }

    public int getClassCount() {
        return ClassCount;
    }

    public void setClassCount(int classCount) {
        ClassCount = classCount;
    }

    public int getFeatureCount() {
        return FeatureCount;
    }

    public void setFeatureCount(int featureCount) {
        FeatureCount = featureCount;
    }

    public double[][] getFNew() {
        return FNew;
    }

    public void setFNew(double[][] FNew) {
        this.FNew = FNew;
    }

    public int[] getClassLabels() {
        return ClassLabels;
    }

    public void setClassLabels(int[] classLabels) {
        ClassLabels = classLabels;
    }

    public int[] getSampleCount() {
        return SampleCount;
    }

    public void setSampleCount(int[] sampleCount) {
        SampleCount = sampleCount;
    }

    public String[] getClassNames() {
        return ClassNames;
    }

    public void setClassNames(String[] classNames) {
        ClassNames = classNames;
    }

    public List<ModelClass> getClasses() {
        return classes;
    }

    public void setClasses(List<ModelClass> classes) {
        this.classes = classes;
    }

    public double[][] getFeatures() {
        return features;
    }

    public void setFeatures(double[][] features) {
        this.features = features;
    }
}
