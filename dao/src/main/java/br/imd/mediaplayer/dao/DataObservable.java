package br.imd.mediaplayer.dao;

import br.imd.mediaplayer.model.AbstractModel;

import java.util.ArrayList;

/**
 * Created by gabriel on 05/12/16.
 */
class DataObservable<T extends AbstractModel> {
    ArrayList<DataObserver<T>> observers = new ArrayList<>();

    boolean addObserver(DataObserver<T> observer) {
        return !observers.contains(observer) && observers.add(observer);
    }

    boolean removeObserver(DataObserver<T> observer) {
        return observers.remove(observer);
    }

    void notifyInsert(T model) {
        for (DataObserver<T> observer :
                observers) {
            observer.onInsert(model);
        }
    }

    void notifyRemove(T model) {
        for (DataObserver<T> observer :
                observers) {
            observer.onRemove(model);
        }
    }

    void notifyUpdate(T model) {
        for (DataObserver<T> observer :
                observers) {
            observer.onUpdate(model);
        }
    }
}
