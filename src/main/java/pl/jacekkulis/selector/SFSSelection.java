package pl.jacekkulis.selector;

import pl.jacekkulis.database.Database;

import java.util.ArrayList;
import java.util.List;

public class SFSSelection implements ISelector {

    private Database db;
    private FischerSelection fischerSelector;

    public SFSSelection(Database database) {
        db = database;
        fischerSelector = new FischerSelection(db);

    }

    @Override
    public List<Integer> select(int dim) {
        return selectBestFeatureIndexesUsingSFS(dim);
    }

    private List<Integer> selectBestFeatureIndexesUsingSFS(int dimension) {
        List<Integer> bestFeatureIndexes = new ArrayList<>(dimension);
        double FLD = 0, tmp;
        int bestFeatureIndex = -1;
        for (int i = 0; i < db.getFeatureCount(); i++) {
            if ((tmp = fischerSelector.selectBestFeatureUsingFischerFor1D(db.getFeatures()[i])) > FLD) {
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

                tmp = fischerSelector.calculateFischerFor2DOrMore(featureIndexes);
                if (tmp > fisherDiscriminant) {
                    fisherDiscriminant = tmp;
                    bestFeatureIndexes.set(i, j);
                }
            }
        }

        return bestFeatureIndexes;
    }
}
