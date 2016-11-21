package fr.lig.sigma.astral.gui.charge;

/**
 * @author Loic Petit
 */
public class CyclicBuffer<T extends Comparable> {
    private Comparable[] buffer;
    private int index = 0;
    private int count = 0;
    private T defaultValue;

    public CyclicBuffer(int capacity, T defaultValue) {
        this.defaultValue = defaultValue;
        buffer = new Comparable[capacity];

    }

    public void add(T value) {
        buffer[index++] = value;
        index %= buffer.length;
        if (count < buffer.length)
            count++;
    }

    public T max() {
        T value = defaultValue;
        for (int i = 0; i < count; i++) {
            if (buffer[i].compareTo(value) > 0)
                value = (T) buffer[i];
        }
        return value;
    }
}
