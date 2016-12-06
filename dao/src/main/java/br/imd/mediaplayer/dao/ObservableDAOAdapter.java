package br.imd.mediaplayer.dao;

import br.imd.mediaplayer.model.AbstractModel;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * Created by gabriel on 06/12/16.
 */
public class ObservableDAOAdapter<T extends AbstractModel> implements ObservableList<T>, DataObserver<T> {

    private List<ListChangeListener<? super T>> listeners = new ArrayList<>();

    @Override
    public void addListener(ListChangeListener<? super T> listChangeListener) {
        if (!listeners.contains(listChangeListener)) listeners.add(listChangeListener);
    }

    @Override
    public void removeListener(ListChangeListener<? super T> listChangeListener) {
        listeners.remove(listChangeListener);
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

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        return null;
    }

    @Override
    public boolean add(T t) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        return false;
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> collection) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public T get(int i) {
        return null;
    }

    @Override
    public T set(int i, T t) {
        return null;
    }

    @Override
    public void add(int i, T t) {

    }

    @Override
    public T remove(int i) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        return null;
    }

    @Override
    public List<T> subList(int i, int i1) {
        return null;
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {

    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {

    }

    @Override
    public void onInsert(T model) {

    }

    @Override
    public void onRemove(T model) {

    }

    @Override
    public void onUpdate(T model) {

    }
}
