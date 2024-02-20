package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document{
    final URI uri;
    String txt;
    byte[] binaryData;

    boolean isBinary = false;
    HashMap<String, Integer> words = new HashMap<>();
    HashSet<String> allWords = new HashSet<>();

    long lastTime;


    public DocumentImpl(URI uri, String text, Map<String, Integer> wordCountMap){

        if (uri == null ||  uri.toString().equals("") /* || uri == uri.EMPTY*/ || text == null || text.equals("")){
            throw new IllegalArgumentException();
        }

        this.lastTime = 0;

        String[] palabras = text.split(" ");
        for (var word : palabras){
            String fixedWord = removeAllSpecialSymbols(word);
            if (!(words.containsKey(fixedWord))){
                words.put(fixedWord, 1);
                allWords.add(fixedWord);
            } else {
                int cant = words.get(fixedWord);
                words.put(fixedWord, cant + 1);
            }
        }
        /*
        if (wordCountMap != null){
            this.words.clear();
            setWordMap(wordCountMap);
        }

         */

        this.uri = uri;
        this.txt = text;
    }

    public DocumentImpl(URI uri, byte[] binaryData){
        if (uri == null ||  uri.toString().equals("")/* || uri == uri.EMPTY */|| binaryData == null || binaryData.length == 0) {
            throw new IllegalArgumentException();
        }

        this.lastTime = 0;
        isBinary = true;
        allWords = new HashSet<>();
        this.uri = uri;
        this.binaryData = binaryData;
    }


    /**
     * @return content of text document
     */
    @Override
    public String getDocumentTxt(){
        return txt;
    }

    /**
     * @return content of binary data document
     */
    @Override
    public byte[] getDocumentBinaryData(){
        return binaryData;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey(){
        return this.uri;
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (txt != null ? txt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        //see if it's null
        if(obj == null){
            return false;
        }
        //see if they're from the same class
        if(getClass()!=obj.getClass()){
            return false;
        }

        Document algo = (Document)obj;

        if(this.hashCode() == algo.hashCode()){
            return true;
        }

        return false;
    }


    @Override
    public int wordCount(String word){
        if(isBinary)
            return 0;

        if (!(words.containsKey(word)))
            return 0;
        // NO SE SI ESTA BIEN ESTO DE ARRIBA

        return words.get(word);
    }
    @Override
    public Set<String> getWords(){
        return allWords;
    }
    private String removeAllSpecialSymbols(String s){
        String finalString = "";
        for(var i = 0; i < s.length(); i++) {
            Character caracter = s.charAt(i);
            if (Character.isDigit(caracter)) {
                finalString = finalString + s.charAt(i);
            } else if (Character.isUpperCase(caracter)) {
                finalString = finalString + s.charAt(i);
            } else if (Character.isLowerCase(caracter)) {
                finalString = finalString + s.charAt(i);
            }
        }
        return finalString;

    }
    @Override
    public long getLastUseTime(){
        return this.lastTime;
    }
    @Override
    public void setLastUseTime(long timeInNanoseconds){
        this.lastTime = timeInNanoseconds;
    }
    @Override
    public int compareTo(Document o) {
        // ESTE ES MAS GRANDE 1
        if (this.lastTime > o.getLastUseTime())
            return 1;

        // OTRO ES MAS GRANDE -1
        if (this.lastTime < o.getLastUseTime())
            return -1;

        // IGUALES 0
        return 0;
    }
    @Override
    public Map<String,Integer> getWordMap(){
        return words;
    }
    @Override
    public void setWordMap(Map<String,Integer> wordMap){
        this.words = new HashMap<>();
        words = (HashMap) wordMap;
    }
}
