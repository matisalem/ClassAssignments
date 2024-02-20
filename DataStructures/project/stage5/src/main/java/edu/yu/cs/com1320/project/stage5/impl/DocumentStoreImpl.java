package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage5.*;
import javax.print.Doc;
import java.io.*;
import java.net.URI;
import java.util.*;


public class DocumentStoreImpl implements DocumentStore {

  //  private HashTableImpl<URI,DocumentImpl> tabla = new HashTableImpl<>();
    private BTreeImpl<URI, Document> bTree = new BTreeImpl<>();
    private StackImpl<Undoable> stack = new StackImpl<>();
    private TrieImpl<URI> trie = new TrieImpl<>();
    private MinHeapImpl<Node> heap = new MinHeapImpl<>();
    private int countLimit = -1;
    private int bytesLimit = -1;
    private int countNumber = 0;
    private int bytesNumber = 0;
    private HashMap<URI, Node> allNodes = new HashMap<>();
    private File file;
    private DocumentPersistenceManager pm;


    public DocumentStoreImpl(File baseDir) {
        this.file = baseDir;
        pm = new DocumentPersistenceManager(baseDir);
        bTree.setPersistenceManager(pm);
    }
    public DocumentStoreImpl() {
        this.file = new File(System.getProperty("user.dir"));
        pm = new DocumentPersistenceManager(file);
        bTree.setPersistenceManager(pm);
    }

    @Override
    public int put(InputStream input, URI uri, DocumentStore.DocumentFormat format) throws IOException{
        if (uri == null || format == null)
            throw new IllegalArgumentException();

        Document old;
        Document document;

        if (input == null){
            old = deleteDocumentWithInputNull(uri);
        } else if (format == DocumentStore.DocumentFormat.BINARY){
            document = new DocumentImpl(uri, readBinaryData(input));
            document.setLastUseTime(System.nanoTime());
            old = this.bTree.put(uri, document);
            Node node = getNode(uri);
            stack.push(genericComandPut(uri, document, old));
            dontKnowNameV2(document, old);

        } else {
            Scanner s = new Scanner(input).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";
            document = new DocumentImpl(uri, result, null);
            document.setLastUseTime(System.nanoTime());
            old = this.bTree.put(uri, document);
            Node node = getNode(uri);
            stack.push(genericComandPut(uri, document, old));
            alFinalDelPut(document, old);
        }
        if (old == null)
            return 0;
        return old.hashCode();
    }

    // region PRIVATE METHODS 1
    // for PUT method
    private GenericCommand genericComandTxt(URI uri, Document document, Document old){
        GenericCommand comand = new GenericCommand(uri, undo -> {
            deleteSpecificDocumentFromSystem2(document, true);
            this.bTree.put(uri, old);
            deleteDocPutOld(document, old);
            return true;
        });
        return comand;
    }
    private GenericCommand genericComandPut(URI uri, Document document, Document old){
        GenericCommand comand = new GenericCommand(uri, undo -> {
            deleteSpecificDocumentFromSystem2(document, true);
            this.bTree.put(uri, old);
            deleteDocPutOld(document, old);
            return true;
        });
        return comand;
    }
    private void alFinalDelPut(Document document, Document old){
        if(old != null){
            for (var i : old.getWords()) {
                trie.delete(i, old.getKey());
            }
        }
        for(var i : document.getWords()) {
            trie.put(i, document.getKey());
        }
        dontKnowNameV2(document, old);
    }
    private void deleteDocPutOld(Document document, Document old){
        for(var i : document.getWords()){
            trie.delete(i, document.getKey());
        }
        if (old != null){
            for(var i : old.getWords()) {
                trie.put(i, old.getKey());
            }
        }
    }
    private Node getNode(URI uri){
        Node node;
        if (!allNodes.containsKey(uri)) {
            node = new Node(uri);
            allNodes.put(uri, node);
        } else {
            node = allNodes.get(uri);
        }
        return node;
    }
    private Document deleteDocumentWithInputNull(URI uri){
        Document old;
        old = this.bTree.put(uri, null);

        if(old != null){
            for (var i : old.getWords()) {
                trie.delete(i, old.getKey());
            }
        }

        GenericCommand comand = new GenericCommand(uri, undo -> {
            bTree.put(uri, old);
            for(var i : old.getWords()) {
                trie.put(i, old.getKey());
            }
            return true;
        });
        stack.push(comand);
        deleteSpecificDocumentFromSystem2(old, false);
        return old;
    }
    private void deleteSpecificDocumentFromSystem(Document de, boolean isReplacement) {
        if (de == null)
            return;

        Integer number;
        Document d = bTree.get(de.getKey());
        if(d.getDocumentTxt() != null){
            number = d.getDocumentTxt().getBytes().length;
        } else {
            number = d.getDocumentBinaryData().length;
        }
        Node node = allNodes.get(d.getKey());

        if (node.isOnBtree()) {
            d.setLastUseTime(0);
            heap.reHeapify(node);
            heap.remove();
            countNumber--;
            bytesNumber = bytesNumber - number;
        }
        if (!isReplacement) {
            try {
                bTree.put(d.getKey(), null);
                allNodes.remove(d.getKey());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void deleteSpecificDocumentFromSystem2(Document de, boolean isReplacement) {
        if (de == null)
            return;

        Integer number;

        if(de.getDocumentTxt() != null){
            number = de.getDocumentTxt().getBytes().length;
        } else {
            number = de.getDocumentBinaryData().length;
        }
        Node node = allNodes.get(de.getKey());

        if (node.isOnBtree()) {
            bTree.get(node.getUri()).setLastUseTime(0);
            heap.reHeapify(node);
            heap.remove();
            countNumber--;
            bytesNumber = bytesNumber - number;
        }
        if (!isReplacement) {
            try {
                bTree.put(de.getKey(), null);
                allNodes.remove(de.getKey());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    private byte[] readBinaryData (InputStream input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int nextByte;
        while ((nextByte = input.read()) != -1) {
            outputStream.write(nextByte);
        }

        return outputStream.toByteArray();
    }
    private void dontKnowNameV2(Document document, Document old){
        Integer nuevosBytes;
        if(document.getDocumentTxt() != null){
            nuevosBytes = document.getDocumentTxt().getBytes().length;
        } else {
            nuevosBytes = document.getDocumentBinaryData().length;
        }
        deleteSpecificDocumentFromSystem2(old, true);
      //  this.bTree.put(document.getKey(), document);
        bTree.get(document.getKey()).setLastUseTime(System.nanoTime());
        Node node;
        if (allNodes.containsKey(document.getKey())) {
            node = allNodes.get(document.getKey());
        } else {
            node = new Node(document.getKey());
            allNodes.put(document.getKey(), node);
        }
        node.setOnBtree(true);
        heap.insert(node);
        heap.reHeapify(node);
        makeHeapBelowLimit(countNumber + 1, bytesNumber + nuevosBytes);
        countNumber++;
        bytesNumber = bytesNumber + nuevosBytes;
        if ((countNumber > countLimit && countLimit != -1) || (bytesLimit < bytesNumber && bytesLimit != -1))
            throw new IllegalStateException();
        makeHeapBelowLimit(countNumber, bytesNumber);
    }

    private void putNodeInHeap(Document d){
        Integer nuevosBytes;
        if(d.getDocumentTxt() != null){
            nuevosBytes = d.getDocumentTxt().getBytes().length;
        } else {
            nuevosBytes = d.getDocumentBinaryData().length;
        }
        allNodes.put(d.getKey(), new Node(d.getKey()));
        Node n = allNodes.get(d.getKey());
        n.setOnBtree(true);
        heap.insert(n);
        heap.reHeapify(n);
        makeHeapBelowLimit(countNumber + 1, bytesNumber + nuevosBytes);
        countNumber++;
        bytesNumber = bytesNumber + nuevosBytes;
        if ((countNumber > countLimit && countLimit != -1) || (bytesLimit < bytesNumber && bytesLimit != -1))
            throw new IllegalStateException();
        makeHeapBelowLimit(countNumber, bytesNumber);
    }

    // endregion


    @Override
    public Document get(URI uri){

        if (uri == null)
            return null;

        Document document = this.bTree.get(uri);
        if (document == null)
            return null;

        document.setLastUseTime(System.nanoTime());
        Node node = allNodes.get(uri);
        if (node.isOnBtree()) {
            heap.reHeapify(node);
        } else {
            putNodeInHeap(document);
        }
        return document;
    }

    @Override
    public boolean delete(URI uri){
        Document eliminado = this.bTree.get(uri);
        Node nodo = allNodes.get(uri);

        if (eliminado == null)
            return false;

        for(var i : eliminado.getWords()) {
            trie.delete(i, eliminado.getKey());
        }

        if (nodo.isOnBtree()){
            try {
                deleteSpecificDocumentFromSystem2(eliminado, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        GenericCommand comand = new GenericCommand(uri, undo -> {
            bTree.put(uri, eliminado);
            for(var i : eliminado.getWords()) {
                trie.put(i, eliminado.getKey());
            }
            allNodes.put(uri, new Node(uri));
          //  putNodeInHeap(eliminado);
            return true;
        });
        stack.push(comand);
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
            for (var i : comandSet){
                GenericCommand gc = (GenericCommand) i;
                URI uri = (URI) gc.getTarget();
                gc.undo();
                Document documento = bTree.get(uri);
                documento.setLastUseTime(tiempo);
                putNodeInHeap(documento);
            }

        } else {
            GenericCommand gc = (GenericCommand) stack.pop();
            URI uri = (URI) gc.getTarget();
            gc.undo();
            Document documento = bTree.get(uri);
            if (documento != null) {
                documento.setLastUseTime(tiempo);
                //  dontKnowNameForThis(documento);
                putNodeInHeap(documento);
            }
        }
    }


    @Override
    public void undo(URI uri) throws IllegalStateException {
        if(stack.size() == 0)
            throw new IllegalStateException();
        StackImpl<Undoable> temporaryStack = passToTemporaryStack(uri);
        if (this.stack.peek() instanceof GenericCommand) {
            if ((((GenericCommand<?>) this.stack.peek()).getTarget() == uri)) {
                undo();
                while (temporaryStack.size() != 0) {
                    this.stack.push(temporaryStack.pop());
                }
            } else { caseOfException(temporaryStack); }
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
                miniMethod(uri);
            } else { caseOfException(temporaryStack); }
        } else { caseOfException(temporaryStack); }
    }

    private void miniMethod(URI uri){
        Document documento = bTree.get(uri);
        documento.setLastUseTime(System.nanoTime());
        putNodeInHeap(documento);
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
        List<URI> lista = new ArrayList<>();
        List<Document> listaFinal = new ArrayList<>();

        lista = ordenarLista(keyword, false);

        if (lista == null)
            return new ArrayList<>();

        long tiempo = System.nanoTime();
        for (var i : lista){
            bTree.get(i).setLastUseTime(tiempo);
            listaFinal.add(bTree.get(i));
            Node n = allNodes.get(i);
            if (n.isOnBtree()) {
                heap.reHeapify(n);
            } else {
                putNodeInHeap(bTree.get(i));
            }
        }
        return listaFinal;
    }

    private List<URI> ordenarLista(String k, boolean isWithPrefix){
        List<URI> lista = new ArrayList<>();

        if (!isWithPrefix) {
            lista = trie.getAllSorted(k, (u1, u2) -> {
                if (bTree.get(u1).wordCount(k) == bTree.get(u2).wordCount(k)) {
                    return 0;
                } else if (bTree.get(u1).wordCount(k) > bTree.get(u2).wordCount(k)) {
                    return -1;
                } else {
                    return 1;
                }});
        } else {
            lista = trie.getAllWithPrefixSorted(k, (u1, u2) -> {
                if (bTree.get(u1).wordCount(k) == bTree.get(u2).wordCount(k)) {
                    return 0;
                } else if(bTree.get(u1).wordCount(k) > bTree.get(u2).wordCount(k)) {
                    return -1;
                } else {
                    return 1;
                }});
        }
        return lista;
    }
    @Override
    public List<Document> searchByPrefix(String keywordPrefix){
        List<Document> listaDocs = new ArrayList<>();
        HashMap<Document, Integer> wordsAmount = new HashMap<>();
        List<URI> lista = ordenarLista(keywordPrefix, true);
        long tiempo = System.nanoTime();

        for(var i : lista){
            listaDocs.add(cosaDelSearch(i, keywordPrefix, tiempo, wordsAmount));
        }
        listaDocs.sort((d1, d2) -> {
            if (wordsAmount.get(d1) == wordsAmount.get(d2)) {
                return 0;
            } else if(wordsAmount.get(d1) > wordsAmount.get(d2)) {
                return -1;
            } else {
                return 1;
            }});
        return listaDocs;
    }

    private Document cosaDelSearch(URI i, String keywordPrefix, long tiempo, HashMap<Document, Integer> wordsAmount){
        Document documento = bTree.get(i);
        documento.setLastUseTime(tiempo);
        Node n = allNodes.get(i);

        if (n.isOnBtree()) {
            heap.reHeapify(n);
        } else {
            putNodeInHeap(bTree.get(i));
        }
        String[] palabras = documento.getDocumentTxt().split(" ");
        for (var w : palabras){
            if (w.startsWith(keywordPrefix)) {
                if (wordsAmount.containsKey(documento)) {
                    wordsAmount.put(documento, wordsAmount.get(documento) + 1);
                } else {
                    wordsAmount.put(documento, 1);
                }
            }
        }
        return documento;
    }


    @Override
    public Set<URI> deleteAll(String keyword){
        Set<URI> set = trie.deleteAll(keyword);
        Set<URI> uris = new HashSet<>();
        if (set == null)
            return null;

        for(var i : set){
            uris.add(i);
        }

        CommandSet commandSet = new CommandSet();

        for(var i : set) {
            Document eliminado = this.bTree.get(i);
            for(var w : eliminado.getWords()) {
                trie.delete(w, eliminado.getKey());
            }
            commandSet.addCommand(new GenericCommand(i, undo -> {

                bTree.put(i, eliminado);
                for(var w : eliminado.getWords()) {
                    trie.put(w, eliminado.getKey());
                }
                return true;
            }));
            deleteSpecificDocumentFromSystem2(eliminado, false);
        }
        stack.push(commandSet);
        eraseNullSpaces(keyword, 1);

        return uris;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix){
        Set<URI> set = trie.deleteAllWithPrefix(keywordPrefix);
        Set<URI> uris = new HashSet<>();
        for(var i : set){
            uris.add(i);
        }

        CommandSet commandSet = new CommandSet();

        for(var i : set) {
            Document eliminado = this.bTree.get(i);
            for(var w : eliminado.getWords()) {
                trie.delete(w, eliminado.getKey());
            }
            commandSet.addCommand(new GenericCommand(i, undo -> {
                bTree.put(i, eliminado);
                for(var w : eliminado.getWords()) {
                    trie.put(w, eliminado.getKey());
                }
                return true;
            }));
            deleteSpecificDocumentFromSystem2(eliminado, false);
        }
        stack.push(commandSet);
        eraseNullSpaces(keywordPrefix, 1);
        return uris;
    }

    private void eraseNullSpaces(String prefix, int length){
        List<URI> lista = new ArrayList<>();
        lista = trie.getAllWithPrefixSorted(prefix.substring(0, length), (u1, u2) -> {
            if (bTree.get(u1).wordCount(prefix) == bTree.get(u2).wordCount(prefix)) {
                return 0;
            } else if(bTree.get(u1).wordCount(prefix) > bTree.get(u2).wordCount(prefix)) {
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
                moveDocumentToMemory();
                Integer newBytes = bytesNumber;
                cn--;
                bn = bn - (olderBytes - newBytes);
            }
        }
    }

    private void moveDocumentToMemory(){

        Node eliminado = heap.remove();
        URI uri = eliminado.getUri();
        // eliminar de la tabla
      //  Document documentEliminado = bTree.put(uri, null);
        Document documentEliminado = bTree.get(uri);
        try {
             bTree.moveToDisk(uri);
             eliminado.setOnBtree(false);
             allNodes.put(uri, eliminado);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        /*
        // eliminar trie
        // A CHEQUEAR SI VA O NO VA
        for (var w : documentEliminado.getWords()) {
            trie.delete(w, uri);
        }

         */
        // eliminar stack
       // deleteDocOfAllStack(uri);
        countNumber--;
        bytesNumber = bytesNumber - getBinaryLength(documentEliminado);
    }
/*
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

 */

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

    private class Node implements Comparable<Node> {
        private URI uri;
        private boolean onBtree;
        public Node(URI uri){
            this.uri = uri;
            onBtree = true;
        }
        private URI getUri(){
            return uri;
        }

        private boolean isOnBtree(){
            return onBtree;
        }
        private void setOnBtree(boolean isBtree){
            onBtree = isBtree;
        }

        @Override
        public int compareTo(Node o) {
            // ESTE ES MAS GRANDE 1
            if (bTree.get(uri).getLastUseTime() > bTree.get(o.getUri()).getLastUseTime())
                return 1;

            // OTRO ES MAS GRANDE -1
            if (bTree.get(uri).getLastUseTime() < bTree.get(o.getUri()).getLastUseTime())
                return -1;

            // IGUALES 0
            return 0;
        }

        @Override
        public int hashCode() {
            return bTree.get(uri).hashCode();
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

            Node algo = (Node)obj;

            if(this.hashCode() == algo.hashCode()){
                return true;
            }

            return false;
        }
    }
}