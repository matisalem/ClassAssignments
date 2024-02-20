package edu.yu.da;


import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Test;
import java.net.URISyntaxException;
import static org.junit.Assert.*;
import edu.yu.da.ThereAndBackAgain.*;
import edu.yu.da.ThereAndBackAgainBase.*;
import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.net.URI;
import static org.junit.Assert.*;

public class ThereAndBackAgainTest {


    @Test
    public void primera() {
        var n = 1;
        var na = 3;
        assertEquals(4, na + n);
    }


    @Test
    public void test(){

        ThereAndBackAgain cycle = new ThereAndBackAgain("c");
        cycle.addEdge("a","b",2);
        cycle.addEdge("b","c",31);
        cycle.addEdge("a","c",5);
        cycle.addEdge("b","d",7);
        cycle.addEdge("c","d",13);
        cycle.addEdge("a","e",81);
        cycle.addEdge("b","e",27);
        cycle.addEdge("c","e",111);


        //Edge f = new Edge(4, 5, 3);
        cycle.doIt();
    }

    @Test
    public void testew(){

        ThereAndBackAgain cycle = new ThereAndBackAgain("a");
        cycle.addEdge("a","b",1);
        cycle.addEdge("b","c",1);
        cycle.addEdge("a","d",3);
        cycle.addEdge("c","e",3);
        cycle.addEdge("d","e",2);
        cycle.addEdge("e","f",2);

        cycle.doIt();

        assertEquals("f", cycle.goalVertex());
        assertEquals(7, cycle.goalCost(), 1);

        System.out.println(cycle.primero);
        System.out.println(cycle.segundo);
    }

    @Test
    public void testLargeGraphWithDiversePaths() {
        ThereAndBackAgain graph = new ThereAndBackAgain("a");
        // Create a graph with diverse path options
        graph.addEdge("a", "b", 3);
        graph.addEdge("b", "c", 7);
        graph.addEdge("c", "d", 11);
        graph.addEdge("d", "e", 4);
        graph.addEdge("e", "g", 3);
        graph.addEdge("a", "f", 1);
        graph.addEdge("f", "g", 27);

        graph.doIt();

        assertEquals("g", graph.goalVertex()); // Expected goal vertex
        assertEquals(28, graph.goalCost(), 0.01); // Expected cost

        System.out.println(graph.primero);
        System.out.println(
                graph.segundo);
    }

    @Test
    public void testLargeGraphWithTwoOptions() {
        ThereAndBackAgain graph = new ThereAndBackAgain("a");
        // Create a graph with diverse path options
        graph.addEdge("a", "b", 3);
        graph.addEdge("b", "c", 7);
        graph.addEdge("c", "d", 11);
        graph.addEdge("d", "e", 4);
        graph.addEdge("e", "g", 3);
        graph.addEdge("a", "f", 1);
        graph.addEdge("f", "g", 27);
        graph.addEdge("g", "h", 1);
        graph.addEdge("g", "l", 1);

        graph.doIt();

        assertEquals("h", graph.goalVertex()); // Expected goal vertex
        assertEquals(29, graph.goalCost(), 0.01); // Expected cost

        System.out.println(graph.primero);
        System.out.println(
                graph.segundo);
    }

    @Test
    public void testOnePath(){

        ThereAndBackAgain cycle = new ThereAndBackAgain("a");
        cycle.addEdge("a","b",1);
        cycle.addEdge("b","c",1);
        cycle.addEdge("a","d",3);
        cycle.addEdge("c","e",3);
        cycle.addEdge("d","e",1);
        cycle.addEdge("e","f",1);

        cycle.doIt();

        assertNull(cycle.goalVertex());
        assertEquals(0, cycle.goalCost(), 0.1);

        assertNull(cycle.primero);
        assertNull(cycle.segundo);
    }


    @Test
    public void exepciones(){

        ThereAndBackAgain cycle = new ThereAndBackAgain("a");
        ThereAndBackAgain cycla = new ThereAndBackAgain("a");
        cycle.addEdge("a","b",1);
        cycle.addEdge("b","c",1);
        cycle.addEdge("a","d",3);
        cycle.addEdge("c","e",3);
        cycle.addEdge("d","e",2);
        cycle.addEdge("e","f",2);

        cycle.doIt();

        try {
            cycle.doIt();
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalStateException e) {}
        try {
            ThereAndBackAgain cycleq = new ThereAndBackAgain("");
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            ThereAndBackAgain cycleq = new ThereAndBackAgain(null);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            cycle.addEdge("s", "w", 11);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalStateException e) {}
        try {
            cycla.addEdge("", "w", 11);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            cycla.addEdge("s", "", 11);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            cycla.addEdge(null, "w", 11);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            cycla.addEdge("s", null, 11);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}
        try {
            cycla.addEdge("s", null, 0);
            throw new RuntimeException("Anda mal burro");
        } catch (IllegalArgumentException e) {}



    }


    @Test
    public void noHayValidPath(){
        ThereAndBackAgain cycle = new ThereAndBackAgain("a");
        cycle.addEdge("a","b",1);
        cycle.addEdge("b","c",1);
        cycle.addEdge("a","d",3);
        cycle.addEdge("c","e",7);
        cycle.addEdge("d","e",2);
        cycle.addEdge("e","f",2);

        cycle.doIt();

        assertEquals(null, cycle.goalVertex());
        assertEquals(0.0, cycle.goalCost(), 1);
    }

    @Test
    public void mixDeTodo(){

        ThereAndBackAgain cycle = new ThereAndBackAgain("a");
        cycle.addEdge("a","b",2);
        cycle.addEdge("b","c",4);
        cycle.addEdge("a","d",4);
        cycle.addEdge("d","e",2);
        cycle.addEdge("e","f",1);
        cycle.addEdge("f","g",3);
        cycle.addEdge("c","h",1);
        cycle.addEdge("h","i",3);
        cycle.addEdge("i","q",22);
        cycle.addEdge("g","r",17);
        cycle.addEdge("r","q",5);


        cycle.doIt();

        assertEquals("q", cycle.goalVertex());
        assertEquals(32, cycle.goalCost(), 1);

    }

    @Test
    public void leff1(){

        ThereAndBackAgain cycle = new ThereAndBackAgain("S");
        cycle.addEdge("S","B",5);
        cycle.addEdge("S","A",6);
        cycle.addEdge("S","G",1);
        cycle.addEdge("A","B",2);
        cycle.addEdge("A","D",7);
        cycle.addEdge("B","C",7);
        cycle.addEdge("B","D",8);
        cycle.addEdge("B","G",2);
        cycle.addEdge("C","E",5);
        cycle.addEdge("E","F",3);
        cycle.addEdge("E","G",4);
        cycle.addEdge("F","G",7);


        cycle.doIt();

        assertEquals("C", cycle.goalVertex());
        assertEquals(10, cycle.goalCost(), 0);

    }

    @Test
    public void leff2(){

        ThereAndBackAgain cycle = new ThereAndBackAgain("C");
        cycle.addEdge("S","A",6);
        cycle.addEdge("S","B",5);
        cycle.addEdge("S","G",1);
        cycle.addEdge("A","B",2);
        cycle.addEdge("A","D",7);
        cycle.addEdge("B","C",7);
        cycle.addEdge("B","D",8);
        cycle.addEdge("B","G",2);
        cycle.addEdge("C","E",5);
        cycle.addEdge("E","F",3);
        cycle.addEdge("E","G",4);
        cycle.addEdge("F","G",7);


        cycle.doIt();

        assertEquals("S", cycle.goalVertex());
        assertEquals(10, cycle.goalCost(), 0);

    }


    @Test
    public void dosValidPaths(){

        ThereAndBackAgain cycle = new ThereAndBackAgain("a");
        cycle.addEdge("a","b",2);
        cycle.addEdge("b","c",4);
        cycle.addEdge("a","d",4);
        cycle.addEdge("d","e",2);
        cycle.addEdge("e","f",1);
        cycle.addEdge("f","g",3);
        cycle.addEdge("c","h",1);
        cycle.addEdge("h","i",3);
        cycle.addEdge("i","q",22);
        cycle.addEdge("g","r",17);
        cycle.addEdge("r","q",5);
        cycle.addEdge("a", "w", 44);
        cycle.addEdge("w", "x", 5);
        cycle.addEdge("a", "x", 49);
        cycle.addEdge("x", "q", 3.1);
        

        cycle.doIt();

        assertEquals("w", cycle.goalVertex());
          assertEquals(40.1, cycle.goalCost(), 0);

        System.out.println(cycle.primero);
        System.out.println();

        System.out.println(cycle.segundo);


    }

    @Test
    public void leffTest(){

    }

}
