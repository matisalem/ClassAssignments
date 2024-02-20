package edu.yu.introtoalgs;

import org.junit.Test;
import java.net.URISyntaxException;
import static edu.yu.introtoalgs.EQIQ.*;
import static org.junit.Assert.*;
import edu.yu.introtoalgs.EQIQBase.*;
import edu.yu.introtoalgs.*;
import java.io.*;
import java.util.*;
import java.net.URI;
import static org.junit.Assert.*;

public class EQIQTest {

    @Test
    public void primera() throws URISyntaxException, IOException {
        var n = 1;
        var na = 3;
        assertEquals(4, na + n);
    }

    @Test
    public void segunda() throws URISyntaxException, IOException {

        int totalQuestions = 2;
        double[] eqSuccessRate = { 10.0, 20.0 };
        double[] iqSuccessRate = { 40.0, 40.0 };
        int nepotismIndex = 1;
        final EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);

        System.out.println(eee.canNepotismSucceed());
        System.out.println(eee.getNumberEQQuestions());
        System.out.println(eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());
    }

    @Test
    public void infinito() throws URISyntaxException, IOException {

        int totalQuestions = 2;
        double[] eqSuccessRate = { 00.0, 20.0 };
        double[] iqSuccessRate = { 40.0, 40.0 };
        int nepotismIndex = 1;
        final EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);


        System.out.println(eee.canNepotismSucceed());
        System.out.println("eq: "  + eee.getNumberEQQuestions());
        System.out.println(eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());
        assertTrue(eee.getNumberOfSecondsSuccess() ==  Double.POSITIVE_INFINITY);
    }

    @Test
    public void infinito2() throws URISyntaxException, IOException {

        int totalQuestions = 2;
        double[] eqSuccessRate = { 20.0, 20.0 };
        double[] iqSuccessRate = { 0.0, 40.0 };
        int nepotismIndex = 1;
        final EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);


        System.out.println(eee.canNepotismSucceed());
        System.out.println("eq: "  + eee.getNumberEQQuestions());
        System.out.println(eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());
        assertTrue(eee.getNumberOfSecondsSuccess() ==  Double.POSITIVE_INFINITY);
    }

    @Test
    public void inf3() throws URISyntaxException, IOException {

        int totalQuestions = 2;
        double[] eqSuccessRate = { 20.0, 00.0 };
        double[] iqSuccessRate = { 40.0, 40.0 };
        int nepotismIndex = 1;
        final EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);


        System.out.println(eee.canNepotismSucceed());
        System.out.println("eq: "  + eee.getNumberEQQuestions());
        System.out.println(eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());
        assertTrue(eee.getNumberOfSecondsSuccess() ==  -1);
    }

    @Test
    public void inf4() throws URISyntaxException, IOException {

        int totalQuestions = 2;
        double[] eqSuccessRate = { 20.0, 00.0 };
        double[] iqSuccessRate = { 40.0, 40.0 };
        int nepotismIndex = 1;
        final EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);


        System.out.println(eee.canNepotismSucceed());
        System.out.println("eq: "  + eee.getNumberEQQuestions());
        System.out.println(eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());
        assertTrue(eee.getNumberOfSecondsSuccess() ==  -1);
    }

    @Test
    public void otraCosa() throws URISyntaxException, IOException {

        int totalQuestions = 2;
        double[] eqSuccessRate = { 20.0, 30.0, 0.0 };
        double[] iqSuccessRate = { 40.0, 40.0, 50.0 };
        int nepotismIndex = 1;
        final EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);


        System.out.println(eee.canNepotismSucceed());
        System.out.println("eq: "  + eee.getNumberEQQuestions());
        System.out.println(eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());
        assertTrue(eee.getNumberOfSecondsSuccess() !=  Double.POSITIVE_INFINITY && eee.getNumberOfSecondsSuccess() != - 1);
    }

    @Test
    public void otraCosa2() throws URISyntaxException, IOException {

        int totalQuestions = 2;
        double[] eqSuccessRate = { 20.0, 30.0, 50.0 };
        double[] iqSuccessRate = { 40.0, 40.0, 0.0 };
        int nepotismIndex = 1;
        final EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);


        System.out.println(eee.canNepotismSucceed());
        System.out.println("eq: "  + eee.getNumberEQQuestions());
        System.out.println(eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());
        assertTrue(eee.getNumberOfSecondsSuccess() !=  Double.POSITIVE_INFINITY && eee.getNumberOfSecondsSuccess() != - 1);
    }



    @Test
    public void tercera() throws URISyntaxException, IOException {

        int totalQuestions = 6;
        double[] eqSuccessRate = { 10.0, 20.0, 30.0 };
        double[] iqSuccessRate = { 40.0, 40.0, 20.0 };
        int nepotismIndex = 1;
        final EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);

        System.out.println(eee.canNepotismSucceed());
        System.out.println(eee.getNumberEQQuestions());
        System.out.println(eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());

    }

    @Test
    public void cuarta() throws URISyntaxException, IOException {

        int totalQuestions = 6;
        double[] eqSuccessRate = { 0.01, 10, 50.0 };
        double[] iqSuccessRate = { 100.0, 90.0, 50.0 };
        int nepotismIndex = 1;
        final EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);

        System.out.println(eee.canNepotismSucceed());
        System.out.println(eee.getNumberEQQuestions());
        System.out.println(eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());

    }

    @Test
    public void quinta() {
        int totalQuestions = 2;
        double[] eqSuccessRate = { 100.0, 1.0 };
        double[] iqSuccessRate = { 100.0, 1.0 };
        int nepotismIndex = 1;
        EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);

        assertFalse(eee.canNepotismSucceed());
    }

    @Test
    public void mismaCosa() {
        int totalQuestions = 2;
        double[] eqSuccessRate = { 30.0, 30.0 };
        double[] iqSuccessRate = { 30.0, 30.0 };
        int nepotismIndex = 1;
        EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);

        System.out.println(eee.canNepotismSucceed());
        System.out.println(eee.getNumberEQQuestions());
        System.out.println(eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());

        assertFalse(eee.canNepotismSucceed());
    }

    @Test
    public void muchasPreguntas() {
        int totalQuestions = 100;
        double[] eqSuccessRate = { 10.0, 20.0, 30.0 };
        double[] iqSuccessRate = { 40.0, 40.0, 20.0 };
        int nepotismIndex = 1;
        EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);

        System.out.println(eee.canNepotismSucceed());
        System.out.println("Preguntas EQ: " + eee.getNumberEQQuestions());
        System.out.println("Preguntas IQ: " + eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());

        assertTrue(eee.canNepotismSucceed());

    }

    @Test
    public void NoSeCruzan() {
        int totalQuestions = 3;
        double[] eqSuccessRate = { 50.0, 10.0, 20.0 };
        double[] iqSuccessRate = { 50.0, 10.0, 20.0 };
        int nepotismIndex = 1;
        EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);

        assertFalse(eee.canNepotismSucceed());
    }

    @Test
    public void leffQueNoPaso() {
        int totalQuestions = 100;
        double[] eqSuccessRate = {10.0, 20.0, 15.65, 11.0, 12.0, 13.0, 11.1, 12.1, 13.1, 11.2, 12.2, 13.2, 11.3, 12.2, 13.3, 11.4, 12.4, 13.4, 11.4, 12.4, 13.4};
        double[] iqSuccessRate = {40.0, 30.0, 35.0, 40.0, 30.0, 40.0, 30.0, 40.0, 30.0, 40.0, 30.0, 40.0, 30.0, 40.0, 30.0, 40.0, 30.0, 40.0, 30.0, 40.0, 30.0};
        int nepotismIndex = 2;
        EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);

        System.out.println(eee.canNepotismSucceed());
        System.out.println("Preguntas EQ: " + eee.getNumberEQQuestions());
        System.out.println("Preguntas IQ: " + eee.getNumberIQQuestions());
        System.out.println(eee.getNumberOfSecondsSuccess());    }

    @Test
    public void cienCandidatos() {
        int totalQuestions = 500;
        int nepotismIndex = 10;

        double[] eqSuccessRate = new double[500];
        double[] iqSuccessRate = new double[500];


        for (int i = 0; i < 20; i++) {
            eqSuccessRate[i] = Math.random() * 95;
            iqSuccessRate[i] = Math.random() * 95;
        //    System.out.println("Para candidato en posicion " +i+ ", eq: " +eqSuccessRate[i] + " ; y IQ: " + iqSuccessRate[i]);
        }

        eqSuccessRate[nepotismIndex] = 95.1;
        iqSuccessRate[nepotismIndex] = 50.0;

        EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);
        System.out.println(eee.getNumberOfSecondsSuccess());

        assertTrue(eee.canNepotismSucceed());
        assertTrue(eee.getNumberOfSecondsSuccess() > ((EQIQ) eee).tiempoMalo());
    }


    @Test
    public void test100MuchasVeces() {
        for (var i  = 0; i < 500; i++){
            cienCandidatos();
        }
    }

    @Test
    public void testingExceptions() throws URISyntaxException, IOException {
        int totalQuestions = 2;
        double[] eqSuccessRate = { 100.0, 1.0 };
        double[] iqSuccessRate = { 100.0, 1.0 };
        double[] eqSuccessRateF = { -1.0, 1.0 };
        double[] iqSuccessRateF = { 100.0, -1.0 };
        double[] eqSuccessRateM = { 100.0, 1.0, 2.0 };
        double[] iqSuccessRateM = { 100.0, 1.0, 3.0, 4.0};

        int nepotismIndex = 1;




        try {
            EQIQBase eee = new EQIQ(0, eqSuccessRate, iqSuccessRate, nepotismIndex);
            throw new RuntimeException();
        } catch (IllegalArgumentException e){}
        try {
            EQIQBase eee = new EQIQ(-2, eqSuccessRate, iqSuccessRate, nepotismIndex);
            throw new RuntimeException();
        } catch (IllegalArgumentException e){}
        try {
            EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRateM, iqSuccessRate, nepotismIndex);
            throw new RuntimeException();
        } catch (IllegalArgumentException e){}
        try {
            EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRateM, nepotismIndex);
            throw new RuntimeException();
        } catch (IllegalArgumentException e){}
        try {
            EQIQBase eee = new EQIQ(10, eqSuccessRate, iqSuccessRate, 2);
            throw new RuntimeException();
        } catch (IllegalArgumentException e){}
        try {
            EQIQBase eee = new EQIQ(10, null, iqSuccessRate, 1);
            throw new RuntimeException();
        } catch (IllegalArgumentException e){}
        try {
            EQIQBase eee = new EQIQ(10, eqSuccessRate, null, 1);
            throw new RuntimeException();
        } catch (IllegalArgumentException e){}




        try {
            EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRateF, iqSuccessRate, nepotismIndex);
            //    throw new RuntimeException();
        } catch (IllegalArgumentException e){}
        try {
            EQIQBase eee = new EQIQ(totalQuestions, eqSuccessRate, iqSuccessRateF, nepotismIndex);
            //   throw new RuntimeException();
        } catch (IllegalArgumentException e){}

    }
}
