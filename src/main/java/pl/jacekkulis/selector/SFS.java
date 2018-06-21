package pl.jacekkulis.selector;

import pl.jacekkulis.database.Database;
import pl.jacekkulis.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class SFS implements ISelector {

    private Database db;
    private Fischer fischerSelector;

    public SFS(Database database) {
        db = database;
        fischerSelector = new Fischer(db);
    }

    @Override
    public List<Integer> select(int dim) {
        List<Integer> bestFeatureIndexes = new ArrayList<>(dim);
        Pair pair = findBestFeatureIndexAndValue();

        bestFeatureIndexes.add(pair.getI());
        double tmp = pair.getD();

        for (int i = 1; i < dim; i++) {
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

    private Pair findBestFeatureIndexAndValue(){
        double FLD = 0, tmp = 0;
        int bestFeatureIndex = -1;
        for (int i = 0; i < db.getFeatureCount(); i++) {
            if ((tmp = fischerSelector.selectBestFeatureUsingFischerFor1D(db.getFeatures()[i])) > FLD) {
                FLD = tmp;
                bestFeatureIndex = i;
            }
        }

        return new Pair(bestFeatureIndex, tmp);
    }
}
