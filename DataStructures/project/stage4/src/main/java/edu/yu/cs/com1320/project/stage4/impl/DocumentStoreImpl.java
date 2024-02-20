package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.stage4.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.*;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class DocumentStoreImpl implements DocumentStore {

    private HashTableImpl<URI,DocumentImpl> tabla = new HashTableImpl<>();
    private StackImpl<Undoable> stack = new StackImpl<>();
    private TrieImpl<Document> trie = new TrieImpl<>();
    private MinHeapImpl<Document> heap = new MinHeapImpl<>();
    private int countLimit = -1;
    private int bytesLimit = -1;
    private int countNumber = 0;
    private int bytesNumber = 0;


    @Override
    public int put(InputStream input, URI uri, DocumentStore.DocumentFormat format) throws IOException{

        if (uri == null || format == null)
            throw new IllegalArgumentException();

        DocumentImpl old;
        DocumentImpl document;

        if (input == null){

            old = deleteDocumentWithInputNull(uri);

        } else if (format == DocumentStore.DocumentFormat.BINARY){

            byte[] result = readBinaryData(input);
            document = new DocumentImpl(uri, result);
            document.setLastUseTime(System.nanoTime());
            old = this.tabla.put(uri, document);

            GenericCommand comand = new GenericCommand(uri, undo -> {
                this.tabla.put(uri, old);
                for(var i : document.getWords()) {
                    trie.delete(i, document);
                }
                // CHEQUEAR
                deleteSpecificDocumentFromHeap(document);
                if (old != null){
                    for(var i : old.getWords()) {
                        trie.put(i, old);
                    }
                }

                return true;
            });
            stack.push(comand);
            dontKnowNameV2(document, old);

        } else {
            Scanner s = new Scanner(input).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";


            document = new DocumentImpl(uri, result);
            document.setLastUseTime(System.nanoTime());
            old = this.tabla.put(uri, document);
            GenericCommand comand = new GenericCommand(uri, undo -> {
                this.tabla.put(uri, old);
                for(var i : document.getWords()) {
                    trie.delete(i, document);
                }
                // revisar
                deleteSpecificDocumentFromHeap(document);
                if (old != null){
                    for(var i : old.getWords()) {
                        trie.put(i, old);
                    }
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
            dontKnowNameV2(document, old);
        }
        if (old == null)
            return 0;
        return old.hashCode();
    }

    // region PRIVATE METHODS 1
    private void dontKnowNameForThis(Document documento){
        Integer nuevosBytes;
        if (documento.getDocumentTxt() != null) {
            nuevosBytes = documento.getDocumentTxt().getBytes().length;
        } else {
            nuevosBytes = documento.getDocumentBinaryData().length;
        }
        heap.insert(documento);
        heap.reHeapify(documento);
        makeHeapBelowLimit(countNumber + 1, bytesNumber + nuevosBytes);
        countNumber++;
        bytesNumber = bytesNumber + nuevosBytes;
        if ((countNumber > countLimit && countLimit != -1) || (bytesLimit < bytesNumber && bytesLimit != -1))
            throw new IllegalStateException();
        makeHeapBelowLimit(countNumber, bytesNumber);
    }
    private void dontKnowNameV2(Document document, Document old){
        Integer nuevosBytes;
        if(document.getDocumentTxt() != null){
            nuevosBytes = document.getDocumentTxt().getBytes().length;
        } else {
            nuevosBytes = document.getDocumentBinaryData().length;
        }
        deleteSpecificDocumentFromHeap(old);
        heap.insert(document);
        heap.reHeapify(document);
        makeHeapBelowLimit(countNumber + 1, bytesNumber + nuevosBytes);
        countNumber++;
        bytesNumber = bytesNumber + nuevosBytes;
        if ((countNumber > countLimit && countLimit != -1) || (bytesLimit < bytesNumber && bytesLimit != -1))
            throw new IllegalStateException();
        makeHeapBelowLimit(countNumber, bytesNumber);
    }
    private byte[] readBinaryData (InputStream input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int nextByte;
        while ((nextByte = input.read()) != -1) {
            outputStream.write(nextByte);
        }

        return outputStream.toByteArray();
    }
    private DocumentImpl deleteDocumentWithInputNull(URI uri){
        DocumentImpl old;
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
        deleteSpecificDocumentFromHeap(old);
        return old;
    }
    private void deleteSpecificDocumentFromHeap(Document d){
        if (d == null)
            return;

        Integer number;
        if(d.getDocumentTxt() != null){
            number = d.getDocumentTxt().getBytes().length;
        } else {
            number = d.getDocumentBinaryData().length;
        }
        d.setLastUseTime(0);
        heap.reHeapify(d);
        heap.remove();
        countNumber--;
        bytesNumber = bytesNumber - number;
    }
    private List<Document> ordenarLista(String k, boolean isWithPrefix){
        List<Document> lista = new ArrayList<>();

        if (!isWithPrefix) {
            lista = trie.getAllSorted(k, (d1, d2) -> {
                if (d1.wordCount(k) == d2.wordCount(k)) {
                    return 0;
                } else if (d1.wordCount(k) > d2.wordCount(k)) {
                    return -1;
                } else {
                    return 1;
                }});
        } else {
            lista = trie.getAllWithPrefixSorted(k, (d1, d2) -> {
                if (d1.wordCount(k) == d2.wordCount(k)) {
                    return 0;
                } else if(d1.wordCount(k) > d2.wordCount(k)) {
                    return -1;
                } else {
                    return 1;
                }});
        }
        return lista;
    }
    // endregion

    @Override
    public Document get(URI uri){
        Document document = this.tabla.get(uri);
        if (document == null)
            return null;

        document.setLastUseTime(System.nanoTime());
        heap.reHeapify(document);
        return document;
    }

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
        deleteSpecificDocumentFromHeap(eliminado);

        return true;
    }

    // region UNDO

    @Override
    public void undo() throws IllegalStateException {
        if (stack.size() == 0)
            throw new IllegalStateException();

        Undoable comand = stack.peek();
        long tiempo = System.nanoTime();

        if (comand instanceof CommandSet<?>){
            CommandSet<URI> comandSet = (CommandSet<URI>) stack.pop();
            Iterator it = comandSet.iterator();
            for (var i : comandSet){
                GenericCommand gc = (GenericCommand) i;
                URI uri = (URI) gc.getTarget();
                gc.undo();
                Document documento = tabla.get(uri);
                    documento.setLastUseTime(tiempo);
                    /*
                        makeHeapBelowLimit(countNumber + 1, bytesNumber + nuevosBytes);
                        countNumber++;
                        bytesNumber = bytesNumber + nuevosBytes;
                        heap.insert(documento);
                        heap.reHeapify(documento);

                     */
               dontKnowNameForThis(documento);
            }

        } else {
            GenericCommand gc = (GenericCommand) stack.pop();
            URI uri = (URI) gc.getTarget();
            gc.undo();
            Document documento = tabla.get(uri);
            if (documento != null) {
                documento.setLastUseTime(tiempo);
                dontKnowNameForThis(documento);
            }
        }
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

                Document documento = tabla.get(uri);
                documento.setLastUseTime(System.nanoTime());
                dontKnowNameForThis(documento);
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

    // endregion

    // region SEARCHES & DELETES
    @Override
    public List<Document> search(String keyword) {
        List<Document> lista = new ArrayList<>();
        lista = ordenarLista(keyword, false);

        if (lista == null)
            return new ArrayList<>();

        long tiempo = System.nanoTime();
        for (var i : lista){
            i.setLastUseTime(tiempo);
            heap.reHeapify(i);
        }
        return lista;
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix){
        List<Document> lista = new ArrayList<>();
        HashMap<Document, Integer> wordsAmount = new HashMap<>();

        lista = ordenarLista(keywordPrefix, true);
        long tiempo = System.nanoTime();

        for(var i : lista){
                i.setLastUseTime(tiempo);
                heap.reHeapify(i);
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

    @Override
    public Set<URI> deleteAll(String keyword){
        Set<Document> set = trie.deleteAll(keyword);
        Set<URI> uris = new HashSet<>();
        for(var i : set){
            uris.add(i.getKey());
        }

        CommandSet commandSet = new CommandSet();

        for(var i : set) {
            deleteSpecificDocumentFromHeap(i);
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

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix){
        Set<Document> set = trie.deleteAllWithPrefix(keywordPrefix);
        Set<URI> uris = new HashSet<>();
        for(var i : set){
            uris.add(i.getKey());
        }

        CommandSet commandSet = new CommandSet();

        for(var i : set) {
            deleteSpecificDocumentFromHeap(i);
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

    // endregion

    @Override
    public void setMaxDocumentCount(int limit){
        this.countLimit = limit;
        makeHeapBelowLimit(countNumber, bytesNumber);
    }

    @Override
    public void setMaxDocumentBytes(int limit){
        this.bytesLimit = limit;
        makeHeapBelowLimit(countNumber, bytesNumber);
    }

    // region PRIVATE METHODS 2

    private void makeHeapBelowLimit(Integer cn, Integer bn){

        if ((cn > countLimit && countLimit != -1) || (bn > bytesLimit && bytesLimit != -1)){
            while ((cn > countLimit && countLimit != -1) || (bn > bytesLimit && bytesLimit != -1)) {
                Integer olderBytes = bytesNumber;
                deleteOneDocument();
                Integer newBytes = bytesNumber;
                cn--;
                bn = bn - (olderBytes - newBytes);
            }
        }
    }

    private void deleteOneDocument(){
       // if (countNumber == 0)
       //     return;
        // eliminar del heap
        Document eliminado = heap.remove();
        // eliminar de la tabla
        tabla.put(eliminado.getKey(), null);
        // eliminar trie
        for (var w : eliminado.getWords()) {
            trie.delete(w, eliminado);
        }
        // eliminar stack
        deleteDocOfAllStack(eliminado.getKey());
        countNumber--;
        bytesNumber = bytesNumber - getBinaryLength(eliminado);
    }

    private void deleteDocOfAllStack(URI uri){
        StackImpl<Undoable> temporaryStack = new StackImpl<>();

        while(stack.size() != 0){
            StackImpl<Undoable> ts2 = passToTemporaryStack(uri);
            StackImpl<Undoable> ts3 = new StackImpl<>();
            while (ts2.size() != 0){
                ts3.push(ts2.pop());
            }
            while (ts3.size() != 0){
                temporaryStack.push(ts3.pop());
            }

            deleteDocumentFromStack(uri);

            if (stack.size() == 1){
                sizeStackIs1(uri);
                break;
            }
        }
        while(temporaryStack.size() != 0){
            stack.push(temporaryStack.pop());
        }
    }

    private void sizeStackIs1(URI uri){
        if (stack.peek() instanceof GenericCommand) {
            if ((((GenericCommand<?>) stack.peek()).getTarget() == uri)){
                deleteDocumentFromStack(uri);
            }
        } else if (stack.peek() instanceof CommandSet) {
            if ((((CommandSet<URI>) stack.peek()).containsTarget(uri))){
                deleteDocumentFromStack(uri);
            }
        }
    }

    private void deleteDocumentFromStack(URI uri){
        if (stack.peek() instanceof GenericCommand) {
            if ((((GenericCommand<?>) stack.peek()).getTarget() == uri)){
                stack.pop();
            }
        } else if (stack.peek() instanceof CommandSet) {
            if ((((CommandSet<URI>) stack.peek()).containsTarget(uri))){
                CommandSet comandSetWithUri = (CommandSet<URI>) this.stack.pop();
                if ((comandSetWithUri.size() == 1)) {
                    stack.pop();
                } else {
                    CommandSet comandset = (CommandSet<URI>) stack.pop();
                    for(var i : (CommandSet<URI>) stack.peek()){
                        if (i.getTarget() == uri) {
                            comandset.remove(i);
                            break;
                        }
                    }
                    stack.push(comandset);
                }
            }
        }
    }

    private int getBinaryLength(Document document){
        Integer thisBytes;
        if (document.getDocumentTxt()== null){
            thisBytes = document.getDocumentBinaryData().length;
        } else {
            thisBytes = document.getDocumentTxt().getBytes().length;
        }
        return thisBytes;
    }
    // endregion
}
