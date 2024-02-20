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
import static org.junit.Assert.*;


public class TrieImplTest{

    @Test
    public void primera() {
        TrieImpl trie = new TrieImpl<Integer>();
        trie.put("uno", 1);
        trie.put("uno", 1);
        trie.put("uNo", 1);
        trie.put("UNO", 1);
        trie.put("Uno", 1);
        trie.put("uno", 11);
        trie.put("uno", 111);
        trie.put("dos", 2);
        trie.put("dos", 22);
        trie.put("dos", 222);


        List<Integer> unos = trie.getAllSorted("uno", (d1, d2) -> {
            if ((int) d1 < (int) d2) {
                return -1;
            } else if ((int) d1 < (int) d2) {
                return 1;
            }
            return 0;});

        List<Integer> doses = trie.getAllSorted("uno", (d1, d2) -> {
            if ((int) d1 < (int) d2) {
                return -1;
            } else if ((int) d1 < (int) d2) {
                return 1;
            }
            return 0;});

        assertEquals(3, unos.size());
        assertEquals(3, doses.size());
    }
    @Test
    public void segunda() {
        TrieImpl trie = new TrieImpl<Integer>();
        trie.put("uno", 1);
        trie.put("uno", 1);
        trie.put("uNo", 1);
        trie.put("UNO", 1);
        trie.put("Uno", 1);
        trie.put("uno", 11);
        trie.put("uno", 111);
        trie.put("unosss", 121);
        trie.put("un", 3);
        trie.put("unAAA", 2221);


        trie.put("dos", 2);
        trie.put("dos", 22);
        trie.put("dos", 222);

        List<Integer> uns = trie.getAllWithPrefixSorted("un", (d1, d2) -> {
            if ((int) d1 < (int) d2) {
                return -1;
            } else if ((int) d1 < (int) d2) {
                return 1;
            }
            return 0;});

        assertEquals(6, uns.size());
    }

    @Test
    public void deletes() {
        TrieImpl trie = new TrieImpl<Integer>();

        for (var i = 1; i < 10; i++){
            trie.put("uno", i);
        }
        trie.put("unosss", 121);
        trie.put("un", 3);
        trie.put("unAAA", 2221);

        List<Integer> uns = trie.getAllSorted("uno", (d1, d2) -> {
            if ((int) d1 < (int) d2) {
                return -1;
            } else if ((int) d1 < (int) d2) {
                return 1;
            }
            return 0;});

        assertEquals(9, uns.size());
        trie.delete("uno", 1);
        uns = trie.getAllSorted("uno", (d1, d2) -> {
            if ((int) d1 < (int) d2) {
                return -1;
            } else if ((int) d1 < (int) d2) {
                return 1;
            }
            return 0;});
        assertEquals(8, uns.size());

        trie.deleteAll("uno");
        uns = trie.getAllSorted("uno", (d1, d2) -> {
            if ((int) d1 < (int) d2) {
                return -1;
            } else if ((int) d1 < (int) d2) {
                return 1;
            }
            return 0;});
        assertEquals(0, uns.size());

        uns = trie.getAllWithPrefixSorted("un", (d1, d2) -> {
            if ((int) d1 < (int) d2) {
                return -1;
            } else if ((int) d1 < (int) d2) {
                return 1;
            }
            return 0;});

        assertEquals(3, uns.size());

        trie.deleteAllWithPrefix("un");
        uns = trie.getAllWithPrefixSorted("un", (d1, d2) -> {
            if ((int) d1 < (int) d2) {
                return -1;
            } else if ((int) d1 < (int) d2) {
                return 1;
            }
            return 0;});
        assertEquals(0, uns.size());
    }
}
