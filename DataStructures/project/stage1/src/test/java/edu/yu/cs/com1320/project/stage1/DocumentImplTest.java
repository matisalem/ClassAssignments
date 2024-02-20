package edu.yu.cs.com1320.project.stage1;

import edu.yu.cs.com1320.project.stage1.impl.DocumentImpl;
import org.junit.Test;
import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DocumentImplTest {

    @Test
    public void firstTry(){
        var i = 1;
        var j = 2;
        Integer w = i + j;
    }

    @Test
    public void secondTry() throws URISyntaxException{
        URI uri1 = new URI("first");
        URI uri2 = new URI("second");
        String texto1 = "text1";
        String texto2 = "text2";
        DocumentImpl documento1 = new DocumentImpl(uri1, texto1);
        DocumentImpl documento3 = new DocumentImpl(uri2, texto2);
        byte[] bytes1 = {0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0};
        byte[] bytes2 = {0, 1, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0};
        DocumentImpl documento2 = new DocumentImpl(uri1, bytes1);
        DocumentImpl documento4 = new DocumentImpl(uri1, bytes2);
    }

    @Test
    public void beginMoving() throws URISyntaxException{
        URI uri1 = new URI("first");
        String texto1 = "text1";
        DocumentImpl documento1 = new DocumentImpl(uri1, texto1);
        byte[] bytes1 = {0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0};
        DocumentImpl documento2 = new DocumentImpl(uri1, bytes1);
        DocumentImpl documento3 = new DocumentImpl(uri1, bytes1);

        assertEquals(texto1, documento1.getDocumentTxt());
        assertEquals(bytes1, documento2.getDocumentBinaryData());
        assertEquals(uri1, documento1.getKey());
        assertEquals(uri1, documento1.getKey());
        assert documento2.equals(documento3) : "2 and 3 are not equals";
        assert !documento1.equals(documento3) : "1 and 3 are equals!";
    }

    @Test
    public void brokenStuff() throws URISyntaxException, IllegalArgumentException{
        URI uri1 = new URI("first");
        URI uriNull = null;
        URI uriEmpty = new URI("");
        String texto1 = "text1";
        String falseText = "";
        String nullText = null;
        DocumentImpl documento1 = new DocumentImpl(uri1, texto1);
        byte[] bytes1 = {0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0};
        byte[] emptyByte = {};
        byte[] nullByte = null;

        // HOW TO TEST EXCEPTIONS

        try {
            // call the method that might throw an exception
            DocumentImpl documento4 = new DocumentImpl(uri1, falseText);
            // if the method did not throw an exception, fail the test
            fail("No hubo exception EMPTY TEXT");
        } catch (IllegalArgumentException e) {
        }

        try {
              DocumentImpl documento5 = new DocumentImpl(uri1, nullText);
            fail("No hubo exception NULL TEXT");
        } catch (IllegalArgumentException e) {
        }

        try {
            DocumentImpl documento5 = new DocumentImpl(uri1, emptyByte);
            fail("No hubo exception EMPTY BYTE");
        } catch (IllegalArgumentException e) {
        }

        try {
            DocumentImpl documento5 = new DocumentImpl(uri1, nullByte);
            fail("No hubo exception NULL BYTE");
        } catch (IllegalArgumentException e) {
        }

        try {
            DocumentImpl documento5 = new DocumentImpl(uriNull, bytes1);
            fail("No hubo exception NULL URI");
        } catch (IllegalArgumentException e) {
        }

        try {
            DocumentImpl documento5 = new DocumentImpl(uriEmpty, "hoho");
            fail("No hubo exception EMPTY URI");
        } catch (IllegalArgumentException e) {
        }

      //  DocumentImpl documento3 = new DocumentImpl(uri1, bytes1);
      //  DocumentImpl documento4 = new DocumentImpl(uri1, falseText);
      //  DocumentImpl documento5 = new DocumentImpl(uri1, nullText);
      //  DocumentImpl documento6 = new DocumentImpl(uriNull, bytes1);

    }

}
