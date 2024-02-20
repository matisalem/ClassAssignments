package edu.yu.da;


import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class WaterPressureTest {


    @Test
    public void primera() {
        var n = 1;
        var na = 3;
        assertEquals(4, na + n);
    }

    @Test
    public void testing1(){

    }


    @Test
    public void coso() {
        WaterPressure wp = new WaterPressure("a");

        wp.addBlockage("a", "b", 2);
        wp.addBlockage("a", "d", 5);
        wp.addBlockage("b", "c", 3);
        wp.addBlockage("c", "e", 4);
        wp.addBlockage("d", "e", 2);
        wp.addBlockage("d", "e", 6);
        wp.addBlockage("e", "f", 1);

    assertEquals(5, wp.minAmount(), 0);

    }

    @Test
    public void noHayDespuesSi() {
        WaterPressure wp = new WaterPressure("a");

        wp.addBlockage("a", "b", 2);
        wp.addBlockage("a", "d", 5);
        wp.addBlockage("b", "c", 3);
        wp.addBlockage("c", "e", 4);
        wp.addBlockage("d", "e", 2);
        wp.addBlockage("d", "e", 6);
        wp.addBlockage("e", "f", 1);
        wp.addBlockage("e", "g", 1);
        wp.addBlockage("h", "g", 1);

        assertEquals(-1, wp.minAmount(), 0);

        WaterPressure w2 = new WaterPressure("a");

        w2.addBlockage("a", "b", 2);
        w2.addBlockage("a", "d", 5);
        w2.addBlockage("b", "c", 3);
        w2.addBlockage("c", "e", 4);
        w2.addBlockage("d", "e", 2);
        w2.addBlockage("d", "e", 6);
        w2.addBlockage("e", "f", 1);
        w2.addBlockage("e", "g", 1);
        w2.addBlockage("h", "g", 1);
        w2.addSecondInputPump("h");

        assertEquals(5, w2.minAmount(), 0);
    }

    @Test
    public void raroProbando() {
        WaterPressure wp = new WaterPressure("a");

        wp.addBlockage("a", "c", 4);
        wp.addBlockage("c", "d", 2);
        wp.addBlockage("d", "e", 2);
        wp.addBlockage("e", "f", 2);
        wp.addBlockage("f", "c", 1);

        assertEquals(4, wp.minAmount(), 0);
    }

    @Test
    public void matusa1() {
        WaterPressure wp = new WaterPressure("a");

        wp.addBlockage("a", "b", 5);
        wp.addBlockage("a", "e", 2);
        wp.addBlockage("e", "b", 7);
        wp.addBlockage("b", "c", 3);
        wp.addBlockage("c", "d", 6);
        wp.addBlockage("d", "b", 2);
        wp.addBlockage("f", "b", 1);

        assertEquals(-1, wp.minAmount(), 0);
        wp.addSecondInputPump("f");
        assertEquals(6, wp.minAmount(), 0);
    }

    @Test
    public void matusa2() {
        WaterPressure wp = new WaterPressure("a");

        wp.addBlockage("a", "b", 5);
        wp.addBlockage("a", "e", 2);
        wp.addBlockage("e", "b", 7);
        wp.addBlockage("b", "c", 3);
        wp.addBlockage("c", "d", 6);
        wp.addBlockage("d", "b", 2);

        assertEquals(6, wp.minAmount(), 0);
        wp.addSecondInputPump("d");
        assertEquals(3, wp.minAmount(), 0);
    }

    @Test
    public void matusa3() {
        WaterPressure wp = new WaterPressure("a");

        wp.addBlockage("a", "b", 5);
        wp.addBlockage("b", "c", 3);
        wp.addBlockage("c", "d", 4);
        wp.addBlockage("d", "e", 4);
        wp.addBlockage("e", "a", 16);
        wp.addBlockage("a", "d", 4);
        wp.addBlockage("e", "b", 2);
        wp.addBlockage("e", "c", 1);


        assertEquals(4, wp.minAmount(), 0);
        wp.addSecondInputPump("d");
        assertEquals(4, wp.minAmount(), 0);
    }

    @Test
    public void matusa4() {
        WaterPressure wp = new WaterPressure("a");

        wp.addBlockage("b", "c", 3);
        wp.addBlockage("c", "d", 4);
        wp.addBlockage("d", "e", 4);
        wp.addBlockage("e", "a", 16);
        wp.addBlockage("e", "b", 2);
        wp.addBlockage("e", "c", 1);


        assertEquals(-1, wp.minAmount(), 0);
        wp.addSecondInputPump("d");
        assertEquals(4, wp.minAmount(), 0);
    }

    @Test
    public void matusa5() {
        WaterPressure wp = new WaterPressure("a");

        wp.addBlockage("b", "c", 3);
        wp.addBlockage("c", "d", 4);
        wp.addBlockage("d", "e", 4);
        wp.addBlockage("e", "a", 16);
        wp.addBlockage("e", "b", 2);
        wp.addBlockage("e", "c", 1);

        wp.addSecondInputPump("d");
        assertEquals(4, wp.minAmount(), 0);
    }

    @Test
    public void matusa6() {
        WaterPressure wp = new WaterPressure("a");

        wp.addBlockage("b", "c", 3);
        wp.addBlockage("c", "d", 4);
        wp.addBlockage("d", "e", 4);
        wp.addBlockage("e", "a", 16);
        wp.addBlockage("e", "b", 2);
        wp.addBlockage("e", "c", 1);
        wp.addBlockage("c", "a", 8);


        wp.addSecondInputPump("d");
        assertEquals(4, wp.minAmount(), 0);
    }

    @Test
    public void matusa7() {

        WaterPressure wp = new WaterPressure("a");

        wp.addBlockage("a", "b", 1);
        wp.addBlockage("b", "c", 1);

        wp.addBlockage("c", "d", 3);
        wp.addBlockage("d", "c", 6);

        wp.addBlockage("d", "e", 3);
        wp.addBlockage("e", "d", 5);

        wp.addBlockage("e", "f", 5);
        wp.addBlockage("f", "e", 3);

        wp.addBlockage("c", "f", 1);
        wp.addBlockage("f", "c", 8);


        assertEquals(3, wp.minAmount(), 0);
    }

    @Test
    public void exepciones(){

        WaterPressure w = new WaterPressure("a");
        w.addBlockage("a", "b", 1);
        w.addBlockage("b", "c", 1);

        WaterPressure w2 = new WaterPressure("a");
        w2.addBlockage("a", "b", 1);

        WaterPressure w3 = new WaterPressure("a");
        w3.addBlockage("a", "b", 1);

        WaterPressure w4 = new WaterPressure("a");
        w4.addBlockage("a", "b", 1);
        w4.addBlockage("a", "c", 1);
        WaterPressure w5 = new WaterPressure("a");
        w5.addBlockage("a", "b", 1);
        WaterPressure w6 = new WaterPressure("a");
        w6.addBlockage("a", "b", 1);
        WaterPressure w7 = new WaterPressure("a");
        w7.addBlockage("a", "b", 1);
        w7.addBlockage("a", "c", 1);

        WaterPressure w8 = new WaterPressure("a");
        w8.addBlockage("a", "b", 1);
        WaterPressure w9 = new WaterPressure("a");
        w9.addBlockage("a", "b", 1);
        WaterPressure w10 = new WaterPressure("a");
        w10.addBlockage("a", "b", 1);


        try {
            WaterPressure wp = new WaterPressure("");
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            WaterPressure wp = new WaterPressure(null);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            w.addSecondInputPump("q");
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            w.addSecondInputPump("a");
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            w.addSecondInputPump("");
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            w.addSecondInputPump(null);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            w7.addSecondInputPump("b");
            w7.addSecondInputPump("c");
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            w6.addBlockage("a", "e", 0);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            w6.addBlockage("", "e", 3);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            w6.addBlockage("a", "", 3);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            w6.addBlockage(null, "e", 3);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            w6.addBlockage("a", null, 3);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            w5.minAmount();
            w5.addBlockage("a", "e", 3);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalStateException e) {}
        try {
            w3.addSecondInputPump("c");
            w3.minAmount();
            w3.minAmount();
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}

        w4.minAmount();
        w4.addSecondInputPump("c");
        w4.minAmount();

        w10.addSecondInputPump("b");
        w10.minAmount();
        w10.minAmount();
    }



}
