package pl.jacekkulis.selector;

import java.util.List;

public interface ISelector {
    List<Integer> select(int method, int dim);
}
