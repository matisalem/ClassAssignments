
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

public class DPMTest {

    // 1 TEST

    @Test
    public void primera() throws URISyntaxException, IOException {
        byte[] bytes1 = {0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0};
        String text1 = "Hola aaaaaaaq profesor, si estas traduciendo esto, hakuna matata";
        URI uri1 = new URI("comidaCotur/maude");

        DocumentImpl dd = new DocumentImpl(uri1, text1, null);
        DocumentImpl df = new DocumentImpl(new URI("comidaCotur/hiebra"), bytes1);

        DocumentPersistenceManager doc =  new DocumentPersistenceManager(null);
        doc.serialize(uri1, dd);
        doc.serialize(new URI("comidaCotur/hiebra"), df);
        Document doce = doc.deserialize(uri1);
        assertEquals(doce.getDocumentTxt(), dd.getDocumentTxt());
        assertEquals(doce.getKey(), dd.getKey());
    }

}
