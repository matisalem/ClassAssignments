package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;


public class StackImpl<T> implements Stack<T>{

    int size;
    Entry top;

    public StackImpl(){
        size = 0;
        top = null;
    }


    /**
     * @param element object to add to the Stack
     */
    @Override
    public void push(T element){
        if (element == null)
            throw new NullPointerException();

        Entry entrada = new Entry(element);

        if (size == 0)
            top = entrada;

        entrada.next = top;
        top = entrada;
        size++;
    }



    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop(){
        if (size == 0)
            return null;

        Entry entrada = top;
        top = top.next;
        size--;
        return (T) entrada.getValue();
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    @Override
    public T peek(){
        if (size == 0)
            return null;
        // A CHEQUEAR

        return (T) top.getValue();
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    @Override
    public int size(){
        return this.size;
    }

    private class Entry<T>{
        T value;
        Entry<T> next;
        private Entry(T value){
            if(value == null){
                throw new IllegalArgumentException();
            }
            this.value = value;
        }
        private T getValue(){
            return this.value;
        }
    }

}
