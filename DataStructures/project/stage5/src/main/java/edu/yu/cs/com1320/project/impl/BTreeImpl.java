package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.stage5.impl.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;


public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {

    // region INITIAL VALUES
    private static final int MAX = 4;
    private Node root; //root of the B-tree
    private Node leftMostExternalNode;
    private int height; //height of the B-tree
    private int n; //number of key-value pairs in the B-tree
    private PersistenceManager pm = null;
    private HashSet<Key> disked = new HashSet<>();

    public BTreeImpl(){
        this.root = new Node(0);
        this.leftMostExternalNode = this.root;
    }

    // endregion

    @Override
    public Value get(Key k){
        if (k == null)
            throw new IllegalArgumentException("argument to get() is null");
        Entry entry = this.get(this.root, k, this.height);
        if(entry != null) {
            if ((Value) entry.getValue() != null){
                return (Value) entry.getValue();
            } else if (pm == null) {
                return (Value) entry.getValue();
            }else {
                Object objeto;
                try {
                    objeto = pm.deserialize(entry.getKey());
                    disked.remove(k);
                } catch (IOException e) {
                    return null;
                }
                if (objeto == null)
                    return null;
                entry.val = objeto;
                try {
                    pm.delete(entry.getKey());
                } catch (IOException e) {
                    return null;
                }
                return (Value) objeto;
            }
        }
        return null;
    }

    private Entry get(Node currentNode, Key key, int height){
        Entry[] entries = currentNode.entries;

        if (height == 0){
            for (int j = 0; j < currentNode.entryCount; j++){
                if(isEqual(key, entries[j].key)) {
                    return entries[j];
                }
            }
            return null;
        } else{
            for (int j = 0; j < currentNode.entryCount; j++){

                if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key)){
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            return null;
        }
    }

    @Override
    public void moveToDisk(Key k) throws Exception{
        pm.serialize(k, get(k));
        disked.add(k);
        put(k, null);
    }
    @Override
    public void setPersistenceManager(PersistenceManager<Key,Value> pm){
        this.pm = pm;
    }

    @Override
    public Value put(Key k, Value v){
        if (k == null)
            throw new IllegalArgumentException("argument key to put() is null");

        Value old = null;
        Entry alreadyThere = this.get(this.root, k, this.height);
        if(alreadyThere != null) {
            if (alreadyThere.getValue() != null){
                old = (Value) alreadyThere.getValue();
                alreadyThere.val = v;
                return old;
            } else {
                try {
                    old = (Value) pm.deserialize(k);
                    disked.remove(k);
                    if (old == null)
                        pm.delete(k);
                } catch (IOException e) {
                    return null;
                }
            }
                alreadyThere.val = v;
                return old;
        }
        caseOfNewRoot(k, v);
        return null;
    }
    private void caseOfNewRoot (Key k, Value v){
        Node newNode = this.put(this.root, k, v, this.height);
        this.n++;
        if (newNode == null)
            return;

        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        this.height++;
    }

    private Node put(Node currentNode, Key key, Value val, int height){
        int j;
        Entry newEntry = new Entry(key, val, null);
        if (height == 0){
            for (j = 0; j < currentNode.entryCount; j++){
                if (less(key, currentNode.entries[j].key))
                    break;
            }
        } else {
            for (j = 0; j < currentNode.entryCount; j++){
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key)) {
                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null)
                        return null;
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        for (int i = currentNode.entryCount; i > j; i--){
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < MAX)
            return null;
        return this.split(currentNode, height);
    }

// region METODOS AGARRADOS
    private static final class Node {
        private int entryCount; // number of entries
        private Entry[] entries = new Entry[MAX]; // the array of children
        private Node next;
        private Node previous;

        // region PRIVATE NODE METHODS
        // create a node with k entries
        private Node(int k) {
            this.entryCount = k;
        }
        private void setNext(Node next) {
            this.next = next;
        }
        private Node getNext() {
            return this.next;
        }
        private void setPrevious(Node previous) {
            this.previous = previous;
        }
        private Node getPrevious() {
            return this.previous;
        }
        private Entry[] getEntries() {
            return Arrays.copyOf(this.entries, this.entryCount);
        }

        // endregion
    }

    private static class Entry{
        private Comparable key;
        private Object val;
        private Node child;

        public Entry(Comparable key, Object val, Node child){
            this.key = key;
            this.val = val;
            this.child = child;
        }
        public Object getValue() {
            return this.val;
        }
        public Comparable getKey() {
            return this.key;
        }
    }

    // comparison functions - make Comparable instead of Key to avoid casts
    private static boolean less(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
    }

    private static boolean isEqual(Comparable k1, Comparable k2){
        return k1.compareTo(k2) == 0;
    }

    private Node split(Node currentNode, int height){
        Node newNode = new Node(MAX / 2);
        //by changing currentNode.entryCount, we will treat any value
        //at index higher than the new currentNode.entryCount as if
        //it doesn't exist
        currentNode.entryCount = MAX / 2;
        //copy top half of h into t
        for (int j = 0; j < MAX / 2; j++){
            newNode.entries[j] = currentNode.entries[MAX / 2 + j];
        }
        //external node
        if (height == 0){
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }

    // endregion

}
