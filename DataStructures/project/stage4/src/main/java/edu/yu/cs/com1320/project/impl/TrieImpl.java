package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {

    private static final int alphabetSize = 62; // extended ASCII
    private Node root; // root of trie

    public TrieImpl(){
    }

    /**
     * add the given value at the given key
     * @param key
     * @param val
     */
    @Override
    public void put(String key, Value val){
        if(key == null)
            throw new IllegalArgumentException();

        if(key.isEmpty())
            return;

        if (val == null) {
            // PREGUNTAR
            return;
        } else {
            this.root = put(this.root, key, val, 0);
        }
    }

    private Node put(Node x, String key, Value val, int d)
    {
        //create a new node
        if (x == null)
        {
            x = new Node();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length())
        {
            x.values.add(val);
            return x;
        }

        char caracter = key.charAt(d);
        int place = charPlaceInLink(caracter);

        if (place != -1) {
            x.links[place] = this.put(x.links[place], key, val, d + 1);
        } /* else {
            x = this.put(x, key, val, d + 1);
        }
        */



        return x;
    }

    /**
     * get all exact matches for the given key, sorted in descending order.
     * Search is CASE SENSITIVE.
     * @param key
     * @param comparator used to sort  values
     * @return a List of matching Values, in descending order
     */
    @Override
    public List<Value> getAllSorted(String key, Comparator<Value> comparator){
        if(key == null || comparator == null)
            throw new IllegalArgumentException();

        List<Value> lista = new ArrayList<>();
        Node x = this.get(this.root, key, 0);
        if (x == null)
            return null;

        if (!(x.values.isEmpty())){
            lista.addAll(x.values);
        }

        lista.sort(comparator);
        return lista;
    }

    private Node get(Node x, String key, int d){
        //link was null - return null, indicating a miss
        if (x == null)
            return null;

        //we've reached the last node in the key,
        //return the node
        if (d == key.length())
            return x;

        char caracter = key.charAt(d);
        int place = charPlaceInLink(caracter);

        if (place != -1)
            return this.get(x.links[place], key, d + 1);

        return null;


        //   return this.get(x.links[place], key, d + 1);
    }

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE SENSITIVE.
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order
     */
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator){
        if(prefix == null || comparator == null)
            throw new IllegalArgumentException();

        List<Value> lista = new ArrayList<>();

        Node x = this.get(this.root, prefix, 0);

        if (x == null)
            return lista;

        if (prefix == "")
            return lista;

        // AGARRAR VALORES X
        if (!(x.values.isEmpty()))
            lista.addAll(x.values);

        // AGARRO TODOS LOS SUBSET NODES DE X
        List<Node> subsetsList = new ArrayList<>();
        subsetsList = subsetNodes(subsetsList, x);

        // AGREGO TODOS VALUES DE NODES A LA LISTA
        for (var i : subsetsList){
            lista.addAll(i.values);
        }

        List<Value> finalList = new ArrayList<>();
        Set<Value> set = new HashSet<>();
        set.addAll(lista);
        finalList.addAll(set);
        finalList.sort(comparator);

        return finalList;
    }

    private List<Node> subsetNodes(List<Node> lista, Node x){
        List<Node> secondList = new ArrayList<>();
        for(var i : x.links) {
            if (i != null) {
                secondList.add(i);
                lista.add(i);
            }
        }
        for(var w : secondList){
            lista = subsetNodes(lista, w);
        }

        return lista;

    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE SENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix){
        if(prefix == null)
            throw new IllegalArgumentException();

        Set<Value> set = new HashSet<>();

        Node x = this.get(this.root, prefix, 0);

        if (prefix == "")
            return set;

        if (x == null)
            return set;

        // AGARRAR VALORES X
        if (!(x.values.isEmpty()))
            set.addAll(x.values);

        x.values.clear();

        // AGARRO TODOS LOS SUBSET NODES DE X
        List<Node> subsetsList = new ArrayList<>();
        subsetsList = subsetNodes(subsetsList, x);

        // AGREGO TODOS VALUES DE NODES A LA LISTA
        for (var i : subsetsList){
            set.addAll(i.values);
        }

        x.links = new Node[alphabetSize];
        return set;
    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     * @param key
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAll(String key){
        if(key == null)
            throw new IllegalArgumentException();

        Set<Value> old = new HashSet<>();
        Node x = this.get(this.root, key, 0);

        if (x == null)
            return null;

        if (!(x.values.isEmpty())){
            old.addAll(x.values);
        }

        x.values.removeAll(old);
        return old;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    @Override
    public Value delete(String key, Value val){
        if(key == null || val == null)
            throw new IllegalArgumentException();

        Node x = this.get(this.root, key, 0);

        if (x == null)
            return null;

        if (x.values.isEmpty())
            return null;

        boolean v = x.values.remove(val);

        if (v)
            return val;

        return null;
    }

    private int charPlaceInLink(Character caracter){
        int place;
        if (Character.isDigit(caracter)){
            place = Character.getNumericValue(caracter);
        } else if (Character.isUpperCase(caracter)) {
            place = caracter - 55;
        } else if (Character.isLowerCase(caracter)) {
            place = caracter - 61;
        } else {
            return -1;
        }
        return place;
    }

    protected class Node<Value>
    {
        protected Set<Value> values;
        protected Node[] links;

        protected Node(){
            values = new HashSet<>();
            links = new Node[alphabetSize];
        }
    }
}

