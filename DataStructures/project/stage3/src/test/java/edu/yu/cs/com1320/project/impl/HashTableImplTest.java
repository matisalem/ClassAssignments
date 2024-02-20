package edu.yu.cs.com1320.project.impl;
import org.junit.Test;
import java.net.URISyntaxException;
import edu.yu.cs.com1320.project.stage3.*;
import edu.yu.cs.com1320.project.stage3.impl.*;
import static edu.yu.cs.com1320.project.stage3.DocumentStore.DocumentFormat.BINARY;
import static edu.yu.cs.com1320.project.stage3.DocumentStore.DocumentFormat.TXT;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.net.URI;
public class HashTableImplTest {
    @Test
    public void testeo(){
        System.out.println("de A es : " + Character.getNumericValue('A'));
        System.out.println("de a es : " + Character.getNumericValue('a'));
        System.out.println("de 0 es : " + Character.getNumericValue('0'));
    }

    @Test
    public void testeo2(){
        String[] array = {"stri","strin", "string", "str", "Stri", "stttstrt"};

        for (var i : array){
            System.out.println(i.startsWith("str") + " de: " + i);
        }
    }
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

    @Test
    public void resize() throws URISyntaxException, IOException {
        HashTableImpl<Integer,String> tabla = new HashTableImpl<>();

        for (int i = 1; i <900; i++){
            tabla.put(i,"numero " + i );
        }

        for (int i = 1; i <900; i++){
            assertEquals("numero " + i, tabla.get(i));
        }

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
        tabla.put(13, "trece");
        tabla.put(14, "catorce");
        tabla.put(15, "quince");
        tabla.put(16, "dieciseis");
        tabla.put(16, "dieciseis");
        tabla.put(17, "diecisiete");
        tabla.put(18, "dieciocho");
        tabla.put(19, "diecinueve");
        tabla.put(20, "veinte");
        tabla.put(21, "veintiuno");
        tabla.put(22, "veintidos");
        tabla.put(23, "veintitres");
        tabla.put(24, "veinticuatro");
        tabla.put(25, "veinticinco");

        assertEquals("seis", tabla.get(6));
        assertEquals("uno", tabla.get(1));
        assertEquals("dos", tabla.get(2));
        assertEquals("tres", tabla.get(3));
        assertEquals("cuatro", tabla.get(4));
        assertEquals("cinco", tabla.get(5));
        assertEquals("siete", tabla.get(7));
        assertEquals("ocho", tabla.get(8));
        assertEquals("nueve", tabla.get(9));
        assertEquals("diez", tabla.get(10));
        assertEquals("once", tabla.get(11));

        assertEquals("doce", tabla.get(12));
        assertEquals("trece", tabla.get(13));
        assertEquals("catorce", tabla.get(14));
        assertEquals("quince", tabla.get(15));

        assertEquals("dieciseis", tabla.get(16));
        assertEquals("diecisiete", tabla.get(17));
        assertEquals("dieciocho", tabla.get(18));
        assertEquals("diecinueve", tabla.get(19));
        assertEquals("veinte", tabla.get(20));
        assertEquals("veintiuno", tabla.get(21));
        assertEquals("veintidos", tabla.get(22));
        assertEquals("veintitres", tabla.get(23));
        assertEquals("veinticuatro", tabla.get(24));
        assertEquals("veinticinco", tabla.get(25));



    }

}
