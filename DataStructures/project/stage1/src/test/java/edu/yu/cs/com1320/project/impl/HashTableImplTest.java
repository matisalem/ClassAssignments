package edu.yu.cs.com1320.project.impl;
import org.junit.Test;
import java.net.URISyntaxException;
import edu.yu.cs.com1320.project.stage1.*;
import edu.yu.cs.com1320.project.stage1.impl.*;
import static edu.yu.cs.com1320.project.stage1.DocumentStore.DocumentFormat.BINARY;
import static edu.yu.cs.com1320.project.stage1.DocumentStore.DocumentFormat.TXT;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.net.URI;
public class HashTableImplTest {

    @Test
    public void putGet10() throws URISyntaxException, IOException {
        HashTableImpl<Integer,String> tabla = new HashTableImpl<>();

        tabla.put(1, "uno");
        tabla.put(2, "dos");
        tabla.put(3, "tres");
        tabla.put(4, "cuatro");
        tabla.put(5, "cinco");
        tabla.put(6, "seis");

        String prueba = tabla.get(6);





        assertEquals("seis", prueba);
        assertEquals("seis", tabla.put(6, "siete"));
        assertEquals("siete", tabla.get(6));







    }

    @Test
    public void putGet101() throws URISyntaxException, IOException {
        HashTableImpl<Integer,String> tabla = new HashTableImpl<>();

        tabla.put(1, "uno");
        tabla.put(2, "dos");
        tabla.put(3, "tres");
        tabla.put(4, "cuatro");
        tabla.put(5, "cinco");
        tabla.put(6, "seis");
        tabla.put(7, "siete");
        tabla.put(8, "ocho");
        tabla.put(9, "nueve");
        tabla.put(10, "diez");
        tabla.put(11, "once");
        tabla.put(12, "doce");



        assertEquals("seis", tabla.get(6));
        assertEquals("uno", tabla.get(1));
        assertEquals("dos", tabla.get(2));
        assertEquals("tres", tabla.get(3));
        assertEquals("cuatro", tabla.get(4));
        assertEquals("cinco", tabla.get(5));
        assertEquals("once", tabla.get(11));
        assertEquals("siete", tabla.get(7));
        assertEquals("ocho", tabla.get(8));
        assertEquals("nueve", tabla.get(9));
        assertEquals("diez", tabla.get(10));
        assertEquals("doce", tabla.get(12));






    }

}
