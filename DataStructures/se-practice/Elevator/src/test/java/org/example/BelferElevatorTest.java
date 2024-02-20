
package org.example;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.*;
import java.util.Comparator;

import org.example.Belfer;

import static org.junit.Assert.*;


public class BelferElevatorTest {


    @Test
    void getClosestElevator() {

        BelferElevator asensor = new BelferElevator();

        asensor.setStatus(0);
        assertEquals(asensor.getStatus(), BelferElevator.ElevatorState.NOT_MOVING);

        asensor.setStatus_helper(20);
        asensor.setStatus_helper(asensor.getStatus_helper() + 2);
        assertEquals(22, asensor.getStatus_helper());

        asensor.setStatus_helper(10);
        assertEquals(10, asensor.getStatus_helper());

    }

    @Test
    void callFloors() {

        BelferElevator asensor = new BelferElevator();

        assertEquals(0, asensor.getCalledDownFloors().size());
        asensor.setCalledDownFloor(1);
        asensor.setCalledDownFloor(11);
        asensor.setCalledDownFloor(9);
        assertEquals(3, asensor.getCalledDownFloors().size());
        assertEquals(0, asensor.getCalledUpperFloors().size());

        asensor.setCalledUpperFloor(11);
        asensor.setCalledUpperFloor(7);
        asensor.setCalledUpperFloor(3);
        assertEquals(3, asensor.getCalledDownFloors().size());
        assertEquals(3, asensor.getCalledUpperFloors().size());

    }

}
