package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.DocumentStore;
import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.impl.DocumentImpl;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.HashTable;

import java.io.IOException;
import java.io.*;
import java.net.URI;
import java.util.Scanner;


public class DocumentStoreImpl implements DocumentStore {

    HashTableImpl<URI,DocumentImpl> tabla = new HashTableImpl<>();

    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given , return 0. If there is a previous doc, return the hashCode of the previous doc.
     * If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException if there is an issue reading input
     * @throws IllegalArgumentException if uri or format are null
     */
    @Override
    public int put(InputStream input, URI uri, DocumentStore.DocumentFormat format) throws IOException{

        if (uri == null || format == null)
            throw new IllegalArgumentException();

        DocumentImpl old;
        DocumentImpl document;

        if (input == null){
           old = this.tabla.put(uri, null);

        } else if (format == DocumentStore.DocumentFormat.BINARY){
          //  byte[] targetArray = new byte[input.available()];
         //   input.read(targetArray);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int nextByte;
            while ((nextByte = input.read()) != -1) {
                outputStream.write(nextByte);
            }

// convert ByteArrayOutputStream to byte array
            byte[] result = outputStream.toByteArray();


            //  byte[] array = input.readAllBytes();

            /*

            byte[] byteArray = null;

            try {
                byteArray = input.readAllBytes();
            } catch (IOException e) {
                System.out.println(e);
            }

             */

            document = new DocumentImpl(uri, result);
            old = this.tabla.put(uri, document);
        } else {

            Scanner s = new Scanner(input).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";

            document = new DocumentImpl(uri, result);
            old = this.tabla.put(uri, document);
        }

        if (old == null)
            return 0;
        return old.hashCode();

    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
    @Override
    public Document get(URI uri){
        return this.tabla.get(uri);
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean delete(URI uri){
        DocumentImpl eliminado = this.tabla.put(uri, null);

        if (eliminado == null)
            return false;

        return true;
    }
}
