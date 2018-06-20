package pl.jacekkulis.selector;

import pl.jacekkulis.database.Database;
import pl.jacekkulis.model.Class;
import pl.jacekkulis.model.Sample;

import java.util.ArrayList;
import java.util.List;

public interface ISelector {
    List<Integer> select(int dim);


    default void setClasses(Database db, List<Integer> bestFeatureIndexes){
        db.setClasses(new ArrayList<>());

        for (String className : db.getClassNames()) {
            db.getClasses().add(new Class(className));
        }

        for (int j = 0; j < db.getFeatures()[0].length; j++) {
            List<Double> features = new ArrayList<>();

            for (int featureIndex : bestFeatureIndexes) {
                features.add(db.getFeatures()[featureIndex][j]);
            }

            db.getClasses().get(db.getClassLabels()[j]).addSample(new Sample(features));
        }

    }
}
