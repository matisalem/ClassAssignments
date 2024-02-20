
package org.example;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;

import org.example.Belfer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class BelferTest {

Belfer Belfer = new Belfer();

//Belfer.getThree_Elevators()
    @Test
    void getClosestElevator() {
        Belfer belfer = new Belfer();
        BelferElevator e1 = belfer.getSpecificElevator(1);
        BelferElevator e2 = belfer.getSpecificElevator(2);
        BelferElevator e3 = belfer.getSpecificElevator(3);
        System.out.println("got here");

        belfer.getClosestElevator(10, true);
        System.out.println(belfer.getSpecificElevator(1).getFloor());
        System.out.println(belfer.getSpecificElevator(2).getFloor());
        System.out.println(belfer.getSpecificElevator(3).getFloor());

        assertEquals(10, belfer.getSpecificElevator(1).getFloor());
        belfer.getClosestElevator(3, true);
        assertEquals(3, belfer.getSpecificElevator(2).getFloor());
        belfer.getClosestElevator(7, false);
        assertEquals(7, belfer.getSpecificElevator(3).getFloor());



    }


}


