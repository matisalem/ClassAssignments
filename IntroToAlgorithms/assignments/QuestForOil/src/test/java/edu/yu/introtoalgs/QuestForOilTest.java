package edu.yu.introtoalgs;

import org.junit.*;
import java.net.*;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import edu.yu.introtoalgs.QuestForOilBase;
import edu.yu.introtoalgs.QuestForOil;

public class QuestForOilTest {

    @Test
    public void primera() throws URISyntaxException, IOException {
        var n = 1;
        var na = 3;
        assertEquals(4, na + n);
    }


    @Test
    public void segunda() {
        char[][] map = {{'S', 'S'}, {'S', 'S'}};
        QuestForOil qfo = new QuestForOil(map);
        int retval = qfo.nContiguous(0, 1);
        Assert.assertEquals("Mismatch on nContiguous", 4, retval);
    }


    @Test
    public void tercera() {
        char[][] map = {{'S', 'S', 'U'}, {'S', 'S', 'U'}, {'U', 'S', 'U'}};
        QuestForOil qfo = new QuestForOil(map);
        int retval = qfo.nContiguous(0, 1);
        System.out.println(retval);
        Assert.assertEquals("Mismatch on nContiguous", 5, retval);
    }

    @Test
    public void solos() {
        char[][] map = new char[10][10];
        for (char[] row : map) {
            Arrays.fill(row, 'U');
        }
        map[1][1] = 'S';
        map[1][2] = 'S';
        map[8][8] = 'S';
        QuestForOil qfo = new QuestForOil(map);
        int retval = qfo.nContiguous(1, 1);
        Assert.assertEquals("Mismatch on nContiguous", 2, retval);
        int retval2 = qfo.nContiguous(1, 2);
        Assert.assertEquals("Mismatch on nContiguous", 2, retval2);
        int retval3 = qfo.nContiguous(8, 8);
        Assert.assertEquals("Mismatch on nContiguous", 1, retval3);
    }

    @Test
    public void seguidos10() {
        char[][] map = new char[10][10];
        for (int i = 0; i < map.length; i++) {
            Arrays.fill(map[i], 'U');
            map[i][i] = 'S';
        }
        QuestForOil qfo = new QuestForOil(map);
        int retval = qfo.nContiguous(0, 0);
        Assert.assertEquals("Mismatch on nContiguous", 10, retval);
    }

    @Test
    public void probando() {
        char[][] map = {
                {'S', 'U', 'S', 'U', 'S'},
                {'S', 'S', 'S', 'U', 'U'},
                {'U', 'S', 'U', 'U', 'S'},
                {'S', 'U', 'S', 'S', 'S'},
                {'U', 'U', 'S', 'U', 'U'}
        };
        QuestForOil qfo = new QuestForOil(map);
        int retval = qfo.nContiguous(1, 1);
        Assert.assertEquals("Mismatch on nContiguous", 12, retval);

        int retval2 = qfo.nContiguous(3, 3);
        Assert.assertEquals("Mismatch on nContiguous", 12, retval2);
        int retval3 = qfo.nContiguous(2, 2);
        Assert.assertEquals("Mismatch on nContiguous", 0, retval3);
    }

    @Test
    public void aDormir() {
        char[][] map = {
                {'S', 'U', 'U', 'U', 'U'},
                {'S', 'S', 'U', 'U', 'U'},
                {'U', 'S', 'U', 'U', 'S'},
                {'U', 'U', 'U', 'S', 'S'},
                {'U', 'U', 'S', 'U', 'U'}
        };
        QuestForOil qfo = new QuestForOil(map);
        int retval = qfo.nContiguous(1, 1);
        Assert.assertEquals("Mismatch on nContiguous", 4, retval);
        int retval2 = qfo.nContiguous(2, 1);
        Assert.assertEquals("Mismatch on nContiguous", 4, retval2);
        int retval3 = qfo.nContiguous(0, 0);
        Assert.assertEquals("Mismatch on nContiguous", 4, retval3);
        int retval4 = qfo.nContiguous(4, 2);
        Assert.assertEquals("Mismatch on nContiguous", 4, retval4);
        int retval5 = qfo.nContiguous(2, 4);
        Assert.assertEquals("Mismatch on nContiguous", 4, retval5);
        int retval6 = qfo.nContiguous(3, 3);
        Assert.assertEquals("Mismatch on nContiguous", 4, retval6);
        int retval7 = qfo.nContiguous(4, 4);
        Assert.assertEquals("Mismatch on nContiguous", 0, retval7);
    }
    @Test
    public void probando2() {
        char[][] map = {
                {'S', 'U', 'S', 'U', 'S', 'S'},
                {'S', 'S', 'S', 'U', 'U', 'U'},
                {'U', 'S', 'U', 'U', 'S', 'S'},
                {'S', 'U', 'S', 'S', 'S', 'S'},
                {'U', 'U', 'S', 'U', 'U', 'S'},
                {'U', 'U', 'S', 'U', 'U', 'S'}
        };
        QuestForOil qfo = new QuestForOil(map);
        int retval = qfo.nContiguous(1, 1);
        Assert.assertEquals("Mismatch on nContiguous", 17, retval);

        int retval2 = qfo.nContiguous(0, 4);
        Assert.assertEquals("Mismatch on nContiguous", 2, retval2);

        int retval3 = qfo.nContiguous(2, 2);
        Assert.assertEquals("Mismatch on nContiguous", 0, retval3);
    }


    @Test
    public void excepciones() {
        char[][] map = {
                {'S', 'U', 'S', 'U', 'S', 'S'},
                {'S', 'S', 'S', 'U', 'U', 'U'},
                {'U', 'S', 'U', 'U', 'S', 'S'},
                {'S', 'U', 'S', 'S', 'S', 'S'},
                {'U', 'U', 'S', 'U', 'U', 'S'},
                {'U', 'U', 'S', 'U', 'U', 'S'}
        };

        QuestForOil qfo = new QuestForOil(map);

        try {
            int aaa = qfo.nContiguous(-1, 1);
            throw new IllegalArgumentException();
        } catch (Exception e){
            System.out.println("Bien");
        }

        try {
            int aaa = qfo.nContiguous(6, 3);
            throw new IllegalArgumentException();
        } catch (Exception e){
            System.out.println("Bien");
        }

        try {
            int aaa = qfo.nContiguous(1, 6);
            throw new IllegalArgumentException();
        } catch (Exception e){
            System.out.println("Bien");
        }

        try {
            int aaa = qfo.nContiguous(1, -1);
            throw new IllegalArgumentException();
        } catch (Exception e){
            System.out.println("Bien");
        }
        try {
            QuestForOil aaaa = new QuestForOil(null);
            throw new IllegalArgumentException();
        } catch (Exception e){
            System.out.println("Bien");
        }
    }




    @Test
    public void probando5000() {
        char[][] largeMap = new char[5000][5000];
        Random rand = new Random();
        int safeCellsCount = 1;

        for (int i = 0; i < 5000; i++) {
            for (int j = 0; j < 5000; j++) {
                largeMap[i][j] = rand.nextBoolean() ? 'S' : 'U';
                if (largeMap[i][j] == 'S') {
                    safeCellsCount++;
                }
            }
        }

        largeMap[0][0] = 'S';

        QuestForOil qfo = new QuestForOil(largeMap);
        int retval = qfo.nContiguous(0, 0);

        Assert.assertTrue("Result should be at least 1", retval >= 1);
        Assert.assertTrue("Result should not exceed total number of safe cells", retval <= safeCellsCount);
    }



}
