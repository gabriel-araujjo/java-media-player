package br.imd.mediaplayer.model;

import br.imd.mediaplayer.model.annotation.Persist;

/**
 * Created by gabriel on 02/12/16.
 */
public abstract class AbstractModel implements Comparable<AbstractModel> {

    private static long ID = 0;
    private static final Object lock = new Object();

    @Persist
    private long id;

    public long getId() {
        if (id == 0) {
            synchronized (lock) {
                id = ++ID;
            }
            return id;
        }
        return id;
    }

    public void setId(long id) {
        synchronized (lock) {
            if (id > ID) {
                ID = id;
            }
        }
        this.id = id;
    }

    public boolean isFresh() {
        return id == 0;
    }

    @Override
    public int compareTo(AbstractModel abstractModel) {
        if (abstractModel == null) return 1;
        return (int) (getId() - abstractModel.getId());
    }
}
