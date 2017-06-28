package camp.computer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

public class List<T> implements java.util.List<T> {

    private java.util.List<T> elements = null;

    public List(T... elements) {

        this.elements = new ArrayList<>();

        for (int valueIndex = 0; valueIndex < elements.length; valueIndex++) {
            if (!this.elements.contains(elements[valueIndex])) {
                this.elements.add(elements[valueIndex]);
            }
        }

    }

    public List() {
        elements = new ArrayList<>();
    }

    public List(Collection<? extends T> collection) {
        elements = new ArrayList(collection);
    }

    public List(int initialCapacity) {
        elements = new ArrayList<T>(initialCapacity);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }

    @Override
    public Object[] toArray() {
        return elements.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return elements.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return elements.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return elements.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return elements.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return elements.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return elements.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return elements.retainAll(c);
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
    public T get(int index) {
        return elements.get(index);
    }

    @Override
    public T set(int index, T element) {
        return elements.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        elements.add(index, element);
    }

    @Override
    public T remove(int index) {
        return elements.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return elements.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return elements.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return elements.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return elements.listIterator(index);
    }

    @Override
    public java.util.List<T> subList(int fromIndex, int toIndex) {
        return elements.subList(fromIndex, toIndex);
    }
}
