package br.imd.mediaplayer.dao;

import br.imd.mediaplayer.model.AbstractModel;

/**
 * Created by gabriel on 05/12/16.
 */
public interface DataObserver<T extends AbstractModel> {
    void onInsert(T model);
    void onRemove(T model);
    void onUpdate(T model);
}
