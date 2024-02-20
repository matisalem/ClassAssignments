package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;
import java.util.*;
public class HashTableImpl<Key,Value> implements HashTable<Key,Value> {


    private Entry<Key, Value>[] tabla;
    private int documentsCount = 0;
    public HashTableImpl(){
        tabla = new Entry[5];
    }

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

        if (current == null)
        return null;

        while(current.next != null && current.key.hashCode() != keyHash) {
            current = current.next;
        }

        if(current.key.hashCode() == keyHash)
            return current.value;

        return null;

    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store.
     * To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    @Override
    public Value put(Key k, Value v){

        // ?? PREGUNTAR POR NUMEROS
        if ((documentsCount / tabla.length) > 2.5)
            resize();

        int index = this.hashFunction(k);
        int keyHash = k.hashCode();

        if(this.tabla[index] == null){
            this.tabla[index] = new Entry<>(k,v);
            documentsCount++;
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
            documentsCount++;

            return null;

        } else {

            if (current.key.hashCode() == keyHash){
                if (v == null){
                    this.tabla[index] = current.next;
                } else {
                    current.value = v;
                }
                return old;
            }
            while(current.next != null && current.next.key.hashCode() != keyHash){
                current = current.next;
            }

            /*

            if  (v == null){
                if (current.next != null) {
                    current.next = current.next.next;
                    documentsCount--;
                    return old;
                } else {
                    if (this.tabla[index].value == )
                    Entry<Key, Value> tempCurrent = this.tabla[index];
                    while(tempCurrent.next.key.hashCode() != keyHash){
                        current = current.next;
                    }

                }
            }

             */

            if  (v == null){
                if (current.next != null) {
                    if (current.next.next == null) {
                        current.next = null;
                    } else {
                        current.next = current.next.next;
                    }
                    documentsCount--;
                    return old;

                } else {
                 //   return null;
                    // NUNCA DEBERIA LLEGAR ACA
                    throw new NullPointerException("NUNCA TENDRIA QUE LLEGAR ACA");
                }
            }
            if (current.next == null) {
                // NUNCA DEBERIA LLEGAR ACA
                throw new NullPointerException("TAMPOCO DEBERIA LLEGAR ACA");
               // return null;
            }

            current = current.next;
            current.value = v;

            return old;
        }
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

    private void resize(){
        Entry<Key, Value>[] old = tabla;
        tabla = new Entry[tabla.length * 2];
        documentsCount = 0;

        for (var i = 0; i < old.length; i++){
            Entry<Key, Value> current = old[i];

            // ?? CHEQUEAR QUE EL WHILE ESTE BIEN

            while(current != null){
                put(current.key, current.value);
                current = current.next;
            }
        }
    }
}

