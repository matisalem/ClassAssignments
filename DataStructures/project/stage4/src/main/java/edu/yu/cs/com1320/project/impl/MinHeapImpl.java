package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    protected E[] elements;
    protected int count = 0;

    public MinHeapImpl(){
        elements = (E[]) new Comparable[5];
        elements[0] = (E)"/";
    }


    // REVISAR
    public void reHeapify(E element){
        Integer index = getArrayIndex(element);
        if (index > 1 && isGreater(index/2, index)){
            upHeap(index);
        } else if (index * 2 <= this.count){
            if (isGreater(index, index * 2)){
                downHeap(index);
            }
            if (index * 2 + 1 <= this.count){
                if(isGreater(index, (index * 2) +1)){
                    downHeap(index);
                }
            }
        }
    }

    protected int getArrayIndex(E element){
        if (this.elements.length == 0 || element == null)
            throw new NoSuchElementException();

        for (var i = 0; i < this.elements.length; i++){

            if (this.elements[i] == null)
                throw new NoSuchElementException();

            if(this.elements[i].equals(element))
                return i;
        }
        throw new NoSuchElementException();
    }

    protected void doubleArraySize(){
        E[] array = this.elements;
        this.elements = (E[]) new Comparable[array.length * 2];
        for (var i = 0; i < array.length; i++){
            this.elements[i] = array[i];
        }
    }

    protected boolean isEmpty() {
        return this.count == 0;
    }

    /**
     * is elements[i] > elements[j]?
     */
    protected boolean isGreater(int i, int j) {
        return this.elements[i].compareTo(this.elements[j]) > 0;
    }

    /**
     * swap the values stored at elements[i] and elements[j]
     */
    protected void swap(int i, int j) {
        E temp = this.elements[i];
        this.elements[i] = this.elements[j];
        this.elements[j] = temp;
    }

    /**
     * while the key at index k is less than its
     * parent's key, swap its contents with its parent’s
     */
    protected void upHeap(int k) {
        while (k > 1 && this.isGreater(k / 2, k)) {
            this.swap(k, k / 2);
            k = k / 2;
        }
    }

    /**
     * move an element down the heap until it is less than
     * both its children or is at the bottom of the heap
     */
    protected void downHeap(int k) {
        while (2 * k <= this.count) {
            //identify which of the 2 children are smaller
            int j = 2 * k;
            if (j < this.count && this.isGreater(j, j + 1)) {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j)) {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }
    }

    public void insert(E x) {
        // double size of array if necessary
        if (this.count >= this.elements.length - 1) {
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        this.elements[++this.count] = x;
        //percolate it up to maintain heap order property
        this.upHeap(this.count);
    }

    public E remove() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];
        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elements[this.count + 1] = null; //null it to prepare for GC
        return min;
    }
}
