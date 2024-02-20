package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document{
    final URI uri;
    String txt;
    byte[] binaryData;


    public DocumentImpl(URI uri, String txt){

        if (uri == null ||  uri.toString().equals("") /* || uri == uri.EMPTY*/ || txt == null || txt.equals("")){
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.txt = txt;

    }

    public DocumentImpl(URI uri, byte[] binaryData){
        if (uri == null ||  uri.toString().equals("")/* || uri == uri.EMPTY */|| binaryData == null || binaryData.length == 0) {
            throw new IllegalArgumentException();
        }

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
        return result;
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
}
