package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.stage3.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.*;

import java.io.*;
import java.net.URI;
import java.util.*;


public class DocumentStoreImpl implements DocumentStore {

    private HashTableImpl<URI,DocumentImpl> tabla = new HashTableImpl<>();
    private StackImpl<Undoable> stack = new StackImpl<>();
    private TrieImpl<Document> trie = new TrieImpl<>();



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

            if(old != null){
                for (var i : old.getWords()) {
                    trie.delete(i, old);
                }
            }

            GenericCommand comand = new GenericCommand(uri, undo -> {
                tabla.put(uri, old);
                for(var i : old.getWords()) {
                    trie.put(i, old);
                }
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
            GenericCommand comand = new GenericCommand(uri, undo -> {
                this.tabla.put(uri, old);
                for(var i : document.getWords()) {
                    trie.delete(i, document);
                }
                return true;
            });
            stack.push(comand);

        } else {
            Scanner s = new Scanner(input).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";

            document = new DocumentImpl(uri, result);
            old = this.tabla.put(uri, document);
            GenericCommand comand = new GenericCommand(uri, undo -> {
                this.tabla.put(uri, old);
                for(var i : document.getWords()) {
                    trie.delete(i, document);
                }
                return true;
            });
            stack.push(comand);
            if(old != null){
                for (var i : old.getWords()) {
                    trie.delete(i, old);
                }
            }
            for(var i : document.getWords()) {
                trie.put(i, document);
            }
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

        GenericCommand comand = new GenericCommand(uri, undo -> {
            tabla.put(uri, eliminado);
            for(var i : eliminado.getWords()) {
                trie.put(i, eliminado);
            }
            return true;
        });
        stack.push(comand);
        for(var i : eliminado.getWords()) {
            trie.delete(i, eliminado);
        }

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

        Undoable comand = stack.peek();

        if (comand instanceof CommandSet<?>){
            ((CommandSet<?>) comand).undoAll();
            stack.pop();
        } else {
            comand.undo();
            stack.pop();
        }
        //   stack.pop();
        //   Command comand = stack.peek();
        //   comand.undo();
        //   stack.pop();
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI uri) throws IllegalStateException {
        StackImpl<Undoable> temporaryStack = new StackImpl<>();
        if(stack.size() == 0)
            throw new IllegalStateException();

        temporaryStack = passToTemporaryStack(uri);

        if (this.stack.peek() instanceof GenericCommand) {
            if ((((GenericCommand<?>) this.stack.peek()).getTarget() == uri)) {
                undo();
                while (temporaryStack.size() != 0) {
                    this.stack.push(temporaryStack.pop());
                }
            } else {
                caseOfException(temporaryStack);
            }
        } else if (this.stack.peek() instanceof CommandSet) {
            if (((CommandSet<URI>) this.stack.peek()).containsTarget(uri)) {
                CommandSet comandSetWithUri = (CommandSet<URI>) this.stack.pop();
                if ((comandSetWithUri.size() == 1)) {
                    comandSetWithUri.undoAll();
                } else {
                    comandSetWithUri.undo(uri);
                    this.stack.push((Undoable) comandSetWithUri);
                }
                while (temporaryStack.size() != 0) {
                    this.stack.push(temporaryStack.pop());
                }
            } else {
                caseOfException(temporaryStack);
            }
        } else {
            caseOfException(temporaryStack);
        }
    }

    private void caseOfException(StackImpl<Undoable> ts){
        while (ts.size() != 0) {
            this.stack.push(ts.pop());
        }
        throw new IllegalStateException();
    }

    private StackImpl<Undoable> passToTemporaryStack(URI uri){
        StackImpl<Undoable> temporaryStack = new StackImpl<>();
        while (stack.size() > 1) {
            if (stack.peek() instanceof GenericCommand) {
                if (!(((GenericCommand<?>) stack.peek()).getTarget() == uri)) {
                    temporaryStack.push(stack.pop());
                } else {
                    break;
                }
            } else if (stack.peek() instanceof CommandSet) {
                if (!(((CommandSet<URI>) stack.peek()).containsTarget(uri))) {
                    temporaryStack.push(stack.pop());
                } else {
                    break;
                }
            }
        }
        if (temporaryStack == null)
            temporaryStack = new StackImpl<>();

        return temporaryStack;
    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) {
        List<Document> lista = new ArrayList<>();
        lista = trie.getAllSorted(keyword, (d1, d2) -> {
            if (d1.wordCount(keyword) == d2.wordCount(keyword)) {
                return 0;
            } else if(d1.wordCount(keyword) > d2.wordCount(keyword)) {
                return -1;
            } else {
                return 1;
            }});

        if (lista == null)
            lista = new ArrayList<>();

        return lista;
    }

        /*
        private int comparador(String key, DocumentImpl d1, DocumentImpl d2){
            if(d1.wordCount(key) == d2.wordCount(key)){
                return 0;
            } else if(d1.wordCount(key) > d2.wordCount(key)){
                return -1;
            } else {
                return 1;
            }
        }

         */

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix){
        List<Document> lista = new ArrayList<>();
        HashMap<Document, Integer> wordsAmount = new HashMap<>();

        lista = trie.getAllWithPrefixSorted(keywordPrefix, (d1, d2) -> {
            if (d1.wordCount(keywordPrefix) == d2.wordCount(keywordPrefix)) {
                return 0;
            } else if(d1.wordCount(keywordPrefix) > d2.wordCount(keywordPrefix)) {
                return -1;
            } else {
                return 1;
            }});

        // REVISO CADA DOCUMENTO EN LISTA A VER CUANTAS PALABRAS EN CADA DOCUMENTO EMPIEZAN CON ESO
        for(var i : lista){
            String[] palabras = i.getDocumentTxt().split(" ");
            for (var w : palabras){
                if (w.startsWith(keywordPrefix)) {
                    if (wordsAmount.containsKey(i)) {
                        wordsAmount.put(i, wordsAmount.get(i) + 1);
                    } else {
                        wordsAmount.put(i, 1);
                    }
                }
            }
        }

        lista.sort((d1, d2) -> {
            if (wordsAmount.get(d1) == wordsAmount.get(d2)) {
                return 0;
            } else if(wordsAmount.get(d1) > wordsAmount.get(d2)) {
                return -1;
            } else {
                return 1;
            }});

        return lista;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword){
        Set<Document> set = trie.deleteAll(keyword);
        Set<URI> uris = new HashSet<>();
        for(var i : set){
            uris.add(i.getKey());
        }

        CommandSet commandSet = new CommandSet();

        for(var i : set) {
            DocumentImpl eliminado = this.tabla.put(i.getKey(), null);
            for(var w : eliminado.getWords()) {
                trie.delete(w, eliminado);
            }
            commandSet.addCommand(new GenericCommand(i.getKey(), undo -> {
                tabla.put(i.getKey(), eliminado);
                for(var w : eliminado.getWords()) {
                    trie.put(w, eliminado);
                }
                return true;
            }));
        }
        stack.push(commandSet);
        eraseNullSpaces(keyword, 1);

        return uris;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix){
        Set<Document> set = trie.deleteAllWithPrefix(keywordPrefix);
        Set<URI> uris = new HashSet<>();
        for(var i : set){
            uris.add(i.getKey());
        }

        CommandSet commandSet = new CommandSet();

        for(var i : set) {
            DocumentImpl eliminado = this.tabla.put(i.getKey(), null);
            for(var w : eliminado.getWords()) {
                trie.delete(w, eliminado);
            }
            commandSet.addCommand(new GenericCommand(i.getKey(), undo -> {
                tabla.put(i.getKey(), eliminado);
                for(var w : eliminado.getWords()) {
                    trie.put(w, eliminado);
                }
                return true;
            }));
        }
        stack.push(commandSet);
        eraseNullSpaces(keywordPrefix, 1);
        return uris;
    }

    private void eraseNullSpaces(String prefix, int length){
        List<Document> lista = new ArrayList<>();
        lista = trie.getAllWithPrefixSorted(prefix.substring(0, length), (d1, d2) -> {
            if (d1.wordCount(prefix) == d2.wordCount(prefix)) {
                return 0;
            } else if(d1.wordCount(prefix) > d2.wordCount(prefix)) {
                return -1;
            } else {
                return 1;
            }});

        if (lista.size() == 0){
            trie.deleteAllWithPrefix(prefix.substring(0, length));
            return;
        } else if (prefix.length() == length) {
            return;
        } else {
            eraseNullSpaces(prefix, length + 1);
        }


    }

}