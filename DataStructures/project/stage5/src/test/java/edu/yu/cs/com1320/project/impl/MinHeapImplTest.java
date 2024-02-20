package edu.yu.cs.com1320.project.impl;

import org.junit.Test;
import java.net.URISyntaxException;
import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.stage5.impl.*;
import static edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat.BINARY;
import static edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat.TXT;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.net.URI;
import static org.junit.Assert.*;

public class MinHeapImplTest {

    // 1 TEST

    @Test
    public void MinHeapImplTest() throws URISyntaxException {
        URI uri1 = new URI("first");
        URI uri2 = new URI("second");
        URI uri3 = new URI("third");
        URI uri4 = new URI("fourth");

        String texto1 = "text1";
        String texto2 = "text2";
        DocumentImpl documento1 = new DocumentImpl(uri1, texto1, null);
        DocumentImpl documento2 = new DocumentImpl(uri2, texto2, null);
        byte[] bytes1 = {0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0};
        byte[] bytes2 = {0, 1, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0};
        DocumentImpl documento3 = new DocumentImpl(uri3, bytes1);
        DocumentImpl documento4 = new DocumentImpl(uri4, bytes2);

        MinHeapImpl<Document> heap = new MinHeapImpl<>();
        heap.insert(documento1);
        heap.insert(documento2);
        heap.insert(documento3);
        heap.insert(documento4);
        assertEquals(heap.getArrayIndex(documento1), 1);
        documento2.setLastUseTime(System.nanoTime());
        documento3.setLastUseTime(System.nanoTime());
        documento4.setLastUseTime(System.nanoTime());
        documento1.setLastUseTime(System.nanoTime());

        heap.reHeapify(documento1);
        assertEquals(4, heap.getArrayIndex(documento1));

        System.out.println(documento1.getLastUseTime());

        documento1.setLastUseTime(0);
        heap.reHeapify(documento1);
        assertEquals(heap.remove(), documento1);
    }


}
