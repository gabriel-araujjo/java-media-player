package br.imd.mediaplayer.dao;

import br.imd.mediaplayer.model.AbstractModel;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * Created by gabriel on 06/12/16.
 */
class ObservableWrapper<T extends AbstractModel> extends AbstractList<T> implements ObservableList<T>,DataObserver<T> {

    private DAO<T> dao;
    private ArrayList<Long> ids = new ArrayList<>();
    private List<ListChangeListener<? super T>> listChangeListeners = new ArrayList<>();
    private List<InvalidationListener> invalidationListeners = new ArrayList<>();

    ObservableWrapper(DAO<T> dao) {
        this.dao = dao;
        this.dao.addObserver(this);
        recreateIds();
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        if (!invalidationListeners.contains(invalidationListener)) invalidationListeners.add(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        invalidationListeners.remove(invalidationListener);
    }

    @Override
    public T get(int i) {
        return dao.get(ids.get(i));
    }

    @Override
    public int size() {
        return dao.list().size();
    }

    @Override
    public void addListener(ListChangeListener<? super T> listChangeListener) {
        if (!listChangeListeners.contains(listChangeListener)) listChangeListeners.add(listChangeListener);
    }

    @Override
    public void removeListener(ListChangeListener<? super T> listChangeListener) {
        listChangeListeners.remove(listChangeListener);
    }

    @Override
    public void onInsert(T model) {
        recreateIds();
        int addedPosition = ids.indexOf(model.getId());
        System.out.println("On insert " + addedPosition + " " + model);
        notifyChange(new AddChange(model, addedPosition));
    }

    @Override
    public void onRemove(T model) {
        int removedPosition = ids.indexOf(model.getId());
        ids.remove(model.getId());
        notifyChange(new RemoveChange(model, removedPosition));
    }

    @Override
    public void onUpdate(T model) {
        int changePostion = ids.indexOf(model.getId());
        notifyChange(new UpdateChange(changePostion));
    }

    private void notifyChange(Change change) {
        for (ListChangeListener<? super T> listener : listChangeListeners) {
            listener.onChanged(change);
        }
    }

    private void recreateIds() {
        ids.clear();
        for (T model : dao.list()) {
            ids.add(model.getId());
        }
    }

    @Override
    public boolean addAll(T[] ts) {
        return false;
    }

    @Override
    public boolean setAll(T[] ts) {
        return false;
    }

    @Override
    public boolean setAll(Collection<? extends T> collection) {
        return false;
    }

    @Override
    public boolean removeAll(T[] ts) {
        return false;
    }

    @Override
    public boolean retainAll(T[] ts) {
        return false;
    }

    @Override
    public void remove(int i, int i1) {

    }

    private abstract class Change extends ListChangeListener.Change<T> {
        boolean accessed;
        int position;
        Change(int position) {
            super(ObservableWrapper.this);
            this.position = position;
        }

        @Override
        public int getFrom() {
            return position;
        }

        @Override
        public int getTo() {
            return position + 1;
        }

        @Override
        public boolean next() {
            if (accessed) {
                return false;
            } else {
                accessed = true;
                return true;
            }
        }

        @Override
        public void reset() {
            accessed = false;
        }

        @Override
        protected int[] getPermutation() {
            return new int[0];
        }

        @Override
        public List<T> getRemoved() {
            return Collections.emptyList();
        }
    }

    private class AddChange extends Change {

        List<T> added;

        AddChange(T element, int position) {
            super(position);
            List<T> list = new ArrayList<>(1);
            list.add(element);
            added = Collections.unmodifiableList(list);
        }

        @Override
        public List<T> getAddedSubList() {
            return added;
        }
    }


    private class RemoveChange extends Change {

        List<T> removed;

        RemoveChange(T element, int position) {
            super(position);
            List<T> list = new ArrayList<>(1);
            list.add(element);
            removed = Collections.unmodifiableList(list);
        }

        @Override
        public List<T> getRemoved() {
            return removed;
        }
    }

    private class UpdateChange extends Change {

        UpdateChange(int position) {
            super(position);
        }

        @Override
        public boolean wasUpdated() {
            return true;
        }
    }
}
