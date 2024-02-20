package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;
import java.util.*;
public class HashTableImpl<Key,Value> implements HashTable<Key,Value> {


    private Entry<Key, Value>[] tabla = new Entry[5];


    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    @Override
    public Value get(Key k) {
        if (k == null)
            return null;

        int index = this.hashFunction(k);
        int keyHash = k.hashCode();
        Entry<Key, Value> current = this.tabla[index];

        while(current.next != null && current.key.hashCode() != keyHash) {
            current = current.next;
        }

        if(current.key.hashCode() == keyHash)
            return current.value;

        return null;

            /*

        for (Entry<Key, Value> entry : tabla[index]) {
            if (entry.key.equals(key)) {
                return entry.getValue();
            }
        }


            for (int i = hashFunction(k); tabla[i] != null; i = (i + 1) % this.tabla.length) {
                if (keys[i].equals(key)) {
                    return vals[i];
                }
            }
            return null;

             */
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store.
     * To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    @Override
    public Value put(Key k, Value v){
        int index = this.hashFunction(k);
        int keyHash = k.hashCode();

        if(this.tabla[index] == null){
            this.tabla[index] = new Entry<>(k,v);
            return null;
        }

        Entry<Key, Value> current = this.tabla[index];
        Entry<Key, Value> nueva = new Entry<>(k,v);

        Value old = get(k);

        if (old == null) {

            if (v == null)
                return null;

            nueva.next = current;
            this.tabla[index] = nueva;

            return null;

        } else {
            /*
            if(current.hashCode() == keyHash){
                current.value = v;
                return old;
            }

             */
            //replace
            while(current.next != null && current.key.hashCode() != keyHash){
                current = current.next;
            }

            if  (v == null){
                current.next = current.next.next;
                return old;
            }

            current.value = v;

            return old;
        }

        /*
        while(current.next.key.hashCode() != keyHash && current.next.next != null) {
            current = current.next;
        }

        if(current.next.next == null){
            current.next = new Entry<>(k,v);
            return null;
        } else {
    //    if (current.next.key.hashCode() == keyHash){
            if(v == null){
                Value old = get(current.next.key);
                current.next = current.next.next;
                return old;
            }else{
                Value old = get(current.next.key);
                current.next.value = v;
                return old;
            }
        }
         */
    }


    /**
     * @param key the key whose presence in the hashtabe we are inquiring about
     * @return true if the given key is present in the hashtable as a key, false if not
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public boolean containsKey(Key key){
        if (key == null)
            throw new NullPointerException();

        int index = this.hashFunction(key);
        Entry<Key, Value> current = this.tabla[index];

        while (current != null) {
            if (current.key.equals(key)){
                return true;
            }
            current = current.next;
        }
        return false;
    }


     private class Entry<Key, Value>{
        Key key;
        Value value;

        Entry<Key, Value> next;
        private Entry(Key k, Value v){
            if(k == null){
                throw new IllegalArgumentException();
            }
            this.key = k;
            this.value = v;
            // this.next = null;
        }

     }

    private int hashFunction(Key key){
        return (key.hashCode() & 0x7fffffff) % this.tabla.length;
    }
}

