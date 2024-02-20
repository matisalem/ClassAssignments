package edu.yu.introtoalgs;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Test;
import java.net.URISyntaxException;
import static edu.yu.introtoalgs.Account.*;
import static org.junit.Assert.*;
import edu.yu.introtoalgs.Tx.*;
import edu.yu.introtoalgs.TxSortFJ.*;
import edu.yu.introtoalgs.TxSortFJBase.*;
import edu.yu.introtoalgs.*;

import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.net.URI;
import static org.junit.Assert.*;
public class TxTest {


    @Test
    public void primera() {
        var n = 1;
        var na = 3;
        assertEquals(4, na + n);
    }

    @Test
    public void viendo() {
        Account a1 = new Account();
        Account a2 = new Account();

        TxBase t1 = new Tx(a1, a2, 100);
        TxBase t2 = new Tx(a1, a2, 200);
        TxBase t3 = new Tx(a1, a2, 500);
        TxBase t4 = new Tx(a1, a2, 800);
        TxBase t5 = new Tx(a1, a2, 1100);

        List<Tx> ss = new ArrayList<>();
        List<TxBase> aa = new ArrayList<>();


        for (var i = 10; i < 30000; i = i + 10){
            ss.add(new Tx(a1, a2, i));
        }

        Collections.shuffle(ss);

        for (var i : ss){
            aa.add(i);
        }

        aa.add(t1);
        aa.add(t2);
        t3.setTimeToNull();
        t5.setTimeToNull();
        aa.add(t3);
        aa.add(t4);
        aa.add(t5);

        TxSortFJ txSort = new TxSortFJ(aa);

        System.out.println();
        System.out.println(t1.compareTo(t2));
        System.out.println("\n");

        for (var i : txSort.tr) {
            System.out.println(i);
        }

        System.out.println();

        for (var i : txSort.sort()) {
            System.out.println(i);
        }

        TxBase[] sortedList = txSort.sort();

        for (var i = 0; i < sortedList.length - 1; i++) {
            Tx aaa = (Tx) sortedList[i];
            Tx bbb = (Tx) sortedList[i + 1];

            if (aaa.time() != null && bbb.time() == null) throw new IllegalArgumentException("Null order esta mal");
            if (aaa.time() == null || bbb.time() == null) continue;
            if (aaa.time().isAfter(bbb.time()) || aaa.id() > bbb.id()) throw new IllegalArgumentException("No esta ordenado boludo");
        }
    }


    @Test
    public void testeando10M() {
        Account a1 = new Account();
        Account a2 = new Account();

        List<TxBase> transactions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 10000000; i++) {
            TxBase tx = new Tx(a1, a2, i + 1);
            if (random.nextBoolean()) {
                tx.setTimeToNull();
            }
            transactions.add(tx);
        }

        TxSortFJ txSort = new TxSortFJ(transactions);

        TxBase[] sortedTransactions = txSort.sort();

        LocalDateTime prevTime = null;
        for (TxBase tx : sortedTransactions) {
            LocalDateTime currentTime = tx.time();
            if (prevTime != null && currentTime != null) {
                assertTrue(prevTime.compareTo(currentTime) <= 0);
            }
            prevTime = currentTime;
        }
    }

    @Test
    public void testeando1M() {
        Account a1 = new Account();
        Account a2 = new Account();

        List<TxBase> transactions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 1000000; i++) {
            TxBase tx = new Tx(a1, a2, i + 1);
            if (random.nextBoolean()) {
                tx.setTimeToNull();
            }
            transactions.add(tx);
        }

        TxSortFJ txSort = new TxSortFJ(transactions);
        TxBase[] sortedTransactions = txSort.sort();

        LocalDateTime prevTime = null;
        for (TxBase tx : sortedTransactions) {
            LocalDateTime currentTime = tx.time();
            if (prevTime != null && currentTime != null) {
                assertTrue(prevTime.compareTo(currentTime) <= 0);
            }
            prevTime = currentTime;
        }
    }

    @Test
    public void testeando01M() {
        Account a1 = new Account();
        Account a2 = new Account();

        List<TxBase> transactions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 100000; i++) {
            TxBase tx = new Tx(a1, a2, i + 1);
            if (random.nextBoolean()) {
                tx.setTimeToNull();
            }
            transactions.add(tx);
        }

        TxSortFJ txSort = new TxSortFJ(transactions);
        TxBase[] sortedTransactions = txSort.sort();

        LocalDateTime prevTime = null;
        for (TxBase tx : sortedTransactions) {
            LocalDateTime currentTime = tx.time();
            if (prevTime != null && currentTime != null) {
                assertTrue(prevTime.compareTo(currentTime) <= 0);
            }
            prevTime = currentTime;
        }
    }

    @Test
    public void testeando001M() {
        Account a1 = new Account();
        Account a2 = new Account();

        List<TxBase> transactions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 10000; i++) {
            TxBase tx = new Tx(a1, a2, i + 1);
            if (random.nextBoolean()) {
                tx.setTimeToNull();
            }
            transactions.add(tx);
        }

        TxSortFJ txSort = new TxSortFJ(transactions);
        TxBase[] sortedTransactions = txSort.sort();

        LocalDateTime prevTime = null;
        for (TxBase tx : sortedTransactions) {
            LocalDateTime currentTime = tx.time();
            if (prevTime != null && currentTime != null) {
                assertTrue(prevTime.compareTo(currentTime) <= 0);
            }
            prevTime = currentTime;
        }
    }


    /*
    @Test
    public void aaxfvdf() {
        Account a1 = new Account();
        Account a2 = new Account();

        List<TxBase> transactions = new ArrayList<>();
        Random random = new Random();

        // Generate 9,000,000 transactions
        for (int i = 0; i < 19000000; i++) {
            TxBase tx = new Tx(a1, a2, i + 1); // Random amount
            if (random.nextBoolean()) { // Randomly set some times to null
                tx.setTimeToNull();
            }
            transactions.add(tx);
        }

        TxSortFJ txSort = new TxSortFJ(transactions);

        long startTime;
        long endTime;

// Measuring time for Arrays.sort
        startTime = System.currentTimeMillis();
        txSort.arraySort();
         endTime = System.currentTimeMillis();
        System.out.println("Arrays.sort time: " + (endTime - startTime) + " ms");

// Measuring time for parallel sort
        startTime = System.currentTimeMillis();
        txSort.sort();
        endTime = System.currentTimeMillis();
        System.out.println("Parallel sort time: " + (endTime - startTime) + " ms");

    }

     */

}
