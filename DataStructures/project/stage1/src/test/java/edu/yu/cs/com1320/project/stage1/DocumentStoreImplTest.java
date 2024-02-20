package edu.yu.cs.com1320.project.stage1;

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

public class DocumentStoreImplTest {
    @Test
    public void firstTry(){
        var i = 1;
        var j = 2;
        Integer w = i + j;
    }

    @Test
    public void begin() throws URISyntaxException, IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = new URI("uri1");
        String string1 = "String1";
        InputStream is = new ByteArrayInputStream(string1.getBytes());

     /*   Scanner s = new Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
       System.out.println(result);

      */

        store.put(is,uri1,TXT);
        Document get = store.get(uri1);

        assertEquals(string1, get.getDocumentTxt());
    }

    @Test
    public void putGet10() throws URISyntaxException, IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = new URI("uri1");
        URI uri2 = new URI("uri2");
        URI uri3 = new URI("uri3");
        URI uri4 = new URI("uri4");
        URI uri5 = new URI("uri5");
        URI uri6 = new URI("uri6");
        URI uri7 = new URI("uri7");
        URI uri8 = new URI("uri8");
        URI uri9 = new URI("uri9");
        URI uri10 = new URI("uri10");

        String string1 = "String1";
        String string2 = "String2";
        String string3 = "String3";
        String string4 = "String4";
        String string5 = "String5";


        byte[] bytes1 = {0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 0};
        byte[] bytes2 = {0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 1, 1, 0};
        byte[] bytes3 = {1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0};
        byte[] bytes4 = {0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 1, 1, 0};
        byte[] bytes5 = {0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0};


        InputStream is1 = new ByteArrayInputStream(string1.getBytes());
        InputStream is2 = new ByteArrayInputStream(string2.getBytes());
        InputStream is3 = new ByteArrayInputStream(string3.getBytes());
        InputStream is4 = new ByteArrayInputStream(string4.getBytes());
        InputStream is5 = new ByteArrayInputStream(string5.getBytes());
        InputStream is11 = new ByteArrayInputStream("matusalem".getBytes());
        InputStream is6 = new ByteArrayInputStream(bytes1);
        InputStream is7 = new ByteArrayInputStream(bytes2);
        InputStream is8 = new ByteArrayInputStream(bytes3);
        InputStream is9 = new ByteArrayInputStream(bytes4);
        InputStream is10 = new ByteArrayInputStream(bytes5);


        store.put(is1, uri1, TXT);
        store.put(is2, uri2, TXT);
        store.put(is3, uri3, TXT);
        store.put(is4, uri4, TXT);
        store.put(is5, uri5, TXT);
        store.put(is6, uri6, BINARY);
        store.put(is7, uri7, BINARY);
        store.put(is8, uri8, BINARY);
        store.put(is9, uri9, BINARY);
        store.put(is10, uri10, BINARY);

        Document documento1 = store.get(uri1);
        Document documento2 = store.get(uri2);
        Document documento3 = store.get(uri3);
        Document documento4 = store.get(uri4);
        Document documento5 = store.get(uri5);
        Document documento6 = store.get(uri6);
        Document documento7 = store.get(uri7);
        Document documento8 = store.get(uri8);
        Document documento9 = store.get(uri9);
        Document documento10 = store.get(uri10);

         assertEquals(string5, documento5.getDocumentTxt());

         int in = store.put(is11, uri5, TXT);

        assertEquals(string5.hashCode(),in);

        store.put(is11, uri5, TXT);

      //  Document documento11 = store.get(uri5);

     //   assertEquals("matusalem", documento11.getDocumentTxt());


        assertEquals(string1, documento1.getDocumentTxt());
        assertEquals(string2, documento2.getDocumentTxt());
        assertEquals(string3, documento3.getDocumentTxt());
        assertEquals(string4, documento4.getDocumentTxt());
        assert Arrays.equals(bytes1, documento6.getDocumentBinaryData()) : "bytes 1 and document 6 are not equals";
        assert Arrays.equals(bytes2, documento7.getDocumentBinaryData()) : "bytes 2 and document 7 are not equals";
        assert Arrays.equals(bytes3, documento8.getDocumentBinaryData()) : "bytes 3 and document 8 are not equals";
        assert Arrays.equals(bytes4, documento9.getDocumentBinaryData()) : "bytes 4 and document 9 are not equals";
        assert Arrays.equals(bytes5, documento10.getDocumentBinaryData()) : "bytes 5 and document 0 are not equals";

    }

    @Test
    public void delete() throws URISyntaxException, IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = new URI("uri1");
        URI uri2 = new URI("uri2");

        String string1 = "String1";

        byte[] bytes1 = {0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 0};

        InputStream is1 = new ByteArrayInputStream(string1.getBytes());
        InputStream is2 = new ByteArrayInputStream(bytes1);

        store.put(is1, uri1, TXT);
        store.put(is2, uri2, BINARY);

        store.delete(uri1);
        store.delete(uri2);

        assert null == store.get(uri1) : "document 1 exists";
        assert null == store.get(uri2) : "document 2 exists";

        assert false == store.delete(uri1) : "document 1 exists";
        assert false == store.delete(uri2) : "document 2 exists";
    }

    /*
    @Test
    public void containsLLave() throws URISyntaxException, IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = new URI("uri1");
        URI uri2 = new URI("uri2");

        String string1 = "String1";

        byte[] bytes1 = {0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 0};

        InputStream is1 = new ByteArrayInputStream(string1.getBytes());
        InputStream is2 = new ByteArrayInputStream(bytes1);

        assert false == store.containsKey(uri1) : "document 1 exists";
        assert false == store.containsKey(uri2) : "document 2 exists";

        store.put(is1, uri1, TXT);
        store.put(is2, uri2, BINARY);

        assert true == store.containsKey(uri1) : "document 1 doesn't exist";
        assert true == store.containsKey(uri2) : "document 2 doesn't exist";

        store.delete(uri1);
        store.delete(uri2);

        assert false == store.containsKey(uri1) : "document 1 exists";
        assert false == store.containsKey(uri2) : "document 2 exists";
    }

     */

    @Test
    public void brokenStuffInStore() throws URISyntaxException, IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = new URI("uri1");
        URI uri2 = new URI("uri2");
        URI uriNull = null;

        String string1 = "String1";
        String string2 = "String2";

        byte[] bytes1 = {0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 0};
        byte[] bytes2 = {0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 1, 1, 0};

        InputStream is1 = new ByteArrayInputStream(string1.getBytes());
        InputStream is2 = new ByteArrayInputStream(string2.getBytes());
        InputStream is6 = new ByteArrayInputStream(bytes1);
        InputStream is7 = new ByteArrayInputStream(bytes2);

        store.put(is1, uri1, TXT);
        store.put(is2, uri2, TXT);


        try {
            store.put(is6, uriNull, BINARY);
            fail("No hubo exception NULL uri");
        } catch (IllegalArgumentException e) {
        }

        try {
            store.put(is6, uri1, null);
            fail("No hubo exception NULL format");
        } catch (IllegalArgumentException e) {
        }
        assert null == store.get(null) : "get NULL is not null";

    }
     }
