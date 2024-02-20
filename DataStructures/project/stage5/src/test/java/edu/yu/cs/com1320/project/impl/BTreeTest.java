
package edu.yu.cs.com1320.project.impl;

import org.junit.Test;

import java.net.Socket;
import java.net.URISyntaxException;
import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.stage5.impl.*;

import javax.print.Doc;

import static edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat.BINARY;
import static edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat.TXT;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.net.URI;
import static org.junit.Assert.*;

public class BTreeTest {

    // 3 TESTS

    @Test
    public void putGet() {
        BTreeImpl<Integer, String> bTree = new BTreeImpl<>();
        Integer[] keys = {1, 2, 3};
        String[] values = {"uno", "dos", "tres"};

        for (var i = 0; i <3; i++){
            bTree.put(keys[i], values[i]);
        }

        String result1 = bTree.get(1);
        String result2 = bTree.get(2);
        String result3 = bTree.get(3);

        assertEquals(values[0], result1);
        assertEquals(values[1], result2);
        assertEquals(values[2], result3);

    }

    @Test
    public void delete() {
        BTreeImpl<Integer, String> bTree = new BTreeImpl<>();
        Integer[] keys = {1, 2, 3};
        String[] values = {"uno", "dos", "tres"};

        for (var i = 0; i <3; i++){
            bTree.put(keys[i], values[i]);
        }

        bTree.put(1, null);
        String result = bTree.get(1);
        assertEquals(null, result);
    }

    @Test
    public void moverDisk() throws URISyntaxException {
        BTreeImpl<URI, Document> bTree = new BTreeImpl<>();
        File file = new File(System.getProperty("user.dir"));
        PersistenceManager pm = new DocumentPersistenceManager(file);
        bTree.setPersistenceManager(pm);
        URI[] keys = {new URI ("urik/matusamatusa1"), new URI("urik/2"), new URI("urik/3")};
        String[] values = {"uno", "dos", "tres"};

        byte[] bytes1 = {0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0};
        String text1 = "Hola aaaaaaaq profesor, si estas traduciendo esto, hakuna matata";
        URI uri1 = new URI("comidaCotur/maude");

        DocumentImpl dd = new DocumentImpl(uri1, text1, null);
        DocumentImpl df = new DocumentImpl(new URI("comidaCotur/hiebra"), bytes1);

        bTree.put(keys[0], dd);
        bTree.put(keys[2], df);

        try {
            bTree.moveToDisk(keys[0]);
            bTree.moveToDisk(keys[2]);
        } catch (Exception e) {
            System.out.println("REVISAR QUE ESTEN EN LA COMPU");
        }
    }
}
