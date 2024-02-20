package edu.yu.introtoalgs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;


public class OctopusCount implements OctopusCountI {

    HashSet<String> allPulpos = new HashSet<>();

    @Override
    public void addObservation(int observationId, ArmColor[] colors, int[] lengthInCM, ArmTexture[] textures) {
        if (colors == null || lengthInCM == null || textures == null || colors.length != 8 || textures.length != 8 || observationId < 0)
            throw new IllegalArgumentException();

        double[] allNumeros = new double[8];

        for (var i = 0; i < 8; i++){
            allNumeros[i] = getNumber(textures[i], colors[i], lengthInCM[i]);
        }

        Arrays.sort(allNumeros);
        String pulpoEntero = "";

        for (var i = 0; i < 8; i++){
            pulpoEntero = pulpoEntero + allNumeros[i] + "-";
        }

        allPulpos.add(pulpoEntero);
    }

    private double getNumber(ArmTexture at, ArmColor ac, Integer cm){
        double a = 0.0;
        a += cm;

        if (cm < 1)
            throw new IllegalArgumentException();

        switch (at){
            case SMOOTH -> a += 0;
            case SLIMY -> a += 0.4;
            case STICKY ->  a += 0.8;
            default -> throw new IllegalArgumentException();
        }
        switch (ac){
            case GRAY -> a += 0.1;
            case RED -> a += 0.2;
            case BLACK -> a += 0.3;
            default -> throw new IllegalArgumentException();
        }

        return a;
    }

    @Override
    public int countThem() {
        return allPulpos.size();
    }
}
