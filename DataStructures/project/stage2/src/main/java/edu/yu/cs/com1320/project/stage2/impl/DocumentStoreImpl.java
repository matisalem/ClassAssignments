package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.stage2.DocumentStore;

import edu.yu.cs.com1320.project.stage2.DocumentStore;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.impl.DocumentImpl;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.impl.StackImpl;


import java.io.IOException;
import java.io.*;
import java.net.URI;
import java.util.Scanner;

public class DocumentStoreImpl implements DocumentStore {

    private HashTableImpl<URI,DocumentImpl> tabla = new HashTableImpl<>();
    private StackImpl<Command> stack = new StackImpl<>();


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

            // COMAND LOGIC
            Command comand = new Command(uri, undo -> {
            //    DocumentImpl eliminate = this.tabla.put(uri, null);
                tabla.put(uri, old);
                return true;
            });
            stack.push(comand);
            // HAY QUE ELIMINARLO O YA ESTA ELIMIADO??
        } else if (format == DocumentStore.DocumentFormat.BINARY){

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int nextByte;
            while ((nextByte = input.read()) != -1) {
                outputStream.write(nextByte);
            }

            byte[] result = outputStream.toByteArray();

            document = new DocumentImpl(uri, result);
            old = this.tabla.put(uri, document);
            Command comand = new Command(uri, undo -> {
                this.tabla.put(uri, old);
                return true;
            });
            stack.push(comand);
        } else {
            Scanner s = new Scanner(input).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";

            document = new DocumentImpl(uri, result);
            old = this.tabla.put(uri, document);
            Command comand = new Command(uri, undo -> {
                this.tabla.put(uri, old);
                return true;
            });
            stack.push(comand);
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

        Command comand = new Command(uri, undo -> {
            tabla.put(uri, eliminado);
            return true;
        });
        stack.push(comand);


        return true;
    }


    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {
        if (stack.size() == 0)
            throw new IllegalStateException();

     //   stack.pop();
        Command comand = stack.peek();
        comand.undo();
        stack.pop();

    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI uri) throws IllegalStateException{
        StackImpl<Command> temporaryStack = new StackImpl<>();

        if (stack.size() == 0){
            throw new IllegalStateException();
        }

        // paso todo a un stack temporary
        while(stack.peek().getUri() != uri && stack.size() > 1){
            Command comand = stack.pop();
            temporaryStack.push(comand);
        }

        // si no lo encontro, mando la excepcion
        if (stack.size() == 0 || stack.peek().getUri() != uri)
            throw new IllegalStateException("Error");

        // si lo encontro, undo, y despues lo mando todo devuelta
        if (stack.peek().getUri() == uri){
            Command comand = stack.peek();
            undo();
            while(temporaryStack.size() != 0){
                Command comando = temporaryStack.pop();
                stack.push(comando);
            }

        }

    }

}
