package edu.yu.introtoalgs;

import org.junit.Rule;
import org.junit.Test;
import java.net.URISyntaxException;

import static edu.yu.introtoalgs.OctopusCountI.ArmColor.*;
import static edu.yu.introtoalgs.OctopusCountI.ArmTexture.*;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.net.URI;
import static org.junit.Assert.*;
import edu.yu.introtoalgs.OctopusCount;
import edu.yu.introtoalgs.OctopusCountI.*;


public class OctopusCountTest {

    @Test
    public void primera() throws URISyntaxException, IOException {
        var n = 1;
        var na = 3;
        assertEquals(4, na + n);
    }

    @Test
    public void todasIguales() throws URISyntaxException, IOException {
        final OctopusCountI oc = new OctopusCount ();
        int id = 1;
        final ArmColor [ ] colors1 = {GRAY, GRAY, GRAY, RED, RED, RED, BLACK, BLACK };
        final int [ ] lengthInCM1 = { 1,2,3,4,5,6,7,8};
        final ArmTexture [] textures1 = {SMOOTH, SMOOTH, SMOOTH, SLIMY , SLIMY , SLIMY , STICKY , STICKY };
        final ArmTexture [] textures2 = {SMOOTH, SMOOTH, SMOOTH, SLIMY , SLIMY , SLIMY , STICKY , SLIMY };


        for (var i = 0; i < 10000000; i++){
            oc.addObservation(i, colors1, lengthInCM1, textures1);
            System.out.println(i);
        }
        oc.addObservation(111, colors1, lengthInCM1, textures2);
        assertEquals(oc.countThem(), 2);
    }

    @Test
    public void todasDiferentes() throws URISyntaxException, IOException {
        final OctopusCountI oc = new OctopusCount ();
        int id = 1;
        final ArmColor [ ] colors1 = {GRAY, GRAY, GRAY, RED, RED, RED, BLACK, BLACK };
        final int [ ] lengthInCM1 = {1,2,3,4,5,6,7,8};
        final ArmTexture [] textures1 = {SMOOTH, SMOOTH, SMOOTH, SLIMY , SLIMY , SLIMY , STICKY , STICKY };
        final ArmTexture [] textures2 = {SMOOTH, SMOOTH, SMOOTH, SLIMY , SLIMY , SLIMY , STICKY , SLIMY };


        for (int i = 1; i < 10000001; i++){
            int [ ] lengthInCM = {i,2,3,4,5,6,7,8};
            oc.addObservation(i, colors1, lengthInCM, textures1);
            System.out.println(i);
        }
        assertEquals(10000000, oc.countThem());
    }

    @Test
    public void mismoEnOtroOrden() throws URISyntaxException, IOException {
        final OctopusCountI oc = new OctopusCount ();
        int id = 1;
        final ArmColor [ ] colors1 = {GRAY, GRAY, RED, RED, RED, BLACK, BLACK, GRAY};
        final int [ ] lengthInCM1 = {2,3,4,5,6,7,8,1};
        final ArmTexture [] textures1 = {SMOOTH, SMOOTH, SLIMY , SLIMY , SLIMY , STICKY , STICKY, SMOOTH};
        final ArmColor [ ] colors2 = {GRAY, GRAY, GRAY, RED, RED, RED, BLACK, BLACK };
        final int [ ] lengthInCM2 = {1,2,3,4,5,6,7,8};
        final ArmTexture [] textures2 = {SMOOTH, SMOOTH, SMOOTH, SLIMY , SLIMY , SLIMY , STICKY , STICKY };


            oc.addObservation(1, colors1, lengthInCM1, textures1);
        oc.addObservation(2, colors2, lengthInCM2, textures2);


        assertEquals(1, oc.countThem());
    }

    @Test
    public void mismaPiernaDiferenteCantidad() throws URISyntaxException, IOException {
        final OctopusCountI oc = new OctopusCount ();
        int id = 1;
        final int [ ] lengthInCM1 = {3,3,4,5,6,7,8,2};
        final ArmColor [ ] colors1 = {GRAY, GRAY, RED, RED, RED, BLACK, BLACK, GRAY};
        final ArmTexture [] textures1 = {SMOOTH, SMOOTH, SLIMY , SLIMY , SLIMY , STICKY , STICKY, SMOOTH};

        final int [ ] lengthInCM2 = {3,3,4,5,6,7,8,3};
        final ArmColor [ ] colors2 = {GRAY, GRAY, RED, RED, RED, BLACK, BLACK, GRAY};
        final ArmTexture [] textures2 = {SMOOTH, SMOOTH, SLIMY , SLIMY , SLIMY , STICKY , STICKY, SMOOTH};



        oc.addObservation(1, colors1, lengthInCM1, textures1);
        oc.addObservation(2, colors2, lengthInCM2, textures2);


        assertEquals(2, oc.countThem());
    }
}
