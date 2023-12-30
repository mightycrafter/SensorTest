package com.example.sensorcheck3;
public class RingBuffer<T> {

    private T[] buffer;
    private int size;
    private int head = 0;
    private int tail = 0;
    private boolean isFull = false;

    public RingBuffer(int size) {
        this.size = size;
        buffer = (T[]) new Object[size];
    }

    public synchronized void add(T element) {
        buffer[head] = element;
        head = (head + 1) % size;
        if (head == tail) {
            tail = (tail + 1) % size;
            isFull = true;
        }
    }

    public synchronized T get(int index) {
        if (index < 0 || index >= size || (!isFull && index >= head)) {
            throw new IndexOutOfBoundsException();
        }
        return buffer[(tail + index) % size];
    }

    public synchronized int size() {
        if (!isFull) {
            return head;
        }
        return size;
    }

    public synchronized boolean isFull() {
        return isFull;
    }

    public synchronized boolean isEmpty() {
        return !isFull && head == tail;
    }
}