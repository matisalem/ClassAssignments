package edu.yu.introtoalgs;

import java.util.*;


public class EQIQ extends EQIQBase{

    private double iQuestions = 0;
    private double eQuestions = 0;
    private boolean puedeGanar = false;
    private double tiempoOptimo = Double.NEGATIVE_INFINITY;

    private int totalQuestions = 0;
    private double eQuestionsPosta = -1;
    private double iQuestionsPosta = -1;
    private double tiempoOptimoPosta = Double.NEGATIVE_INFINITY;
    private boolean boca = false;
    private static final int SECONDS_PER_HOUR = 3600;



    public EQIQ(int totalQuestions, double[] eqSuccessRate, double[] iqSuccessRate, int nepotismIndex) {
        super(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);

        if (eqSuccessRate == null || iqSuccessRate == null || nepotismIndex < 0) throw new IllegalArgumentException();
        if (eqSuccessRate.length != iqSuccessRate.length || nepotismIndex >= eqSuccessRate.length || totalQuestions < 2 || eqSuccessRate.length < 2) throw new IllegalArgumentException();

        this.totalQuestions = totalQuestions;

        /*

        puedeGanar = false;
        final double THRESHOLD = 0.000001;

        double lowerBound = 0;
        double upperBound = totalQuestions;
    //    double tiempoOptimoPosta = Double.MAX_VALUE;
        Candidato[] candidatos = new Candidato[iqSuccessRate.length];

        for (var i = 0; i < eqSuccessRate.length; i++){
                candidatos[i] = new Candidato(eqSuccessRate[i], iqSuccessRate[i], i);
        }

        double masIQ = calculateTimeDifference(0.001, totalQuestions - 0.001, nepotismIndex, candidatos);
        double masEQ = calculateTimeDifference(totalQuestions - 0.001, 0.001, nepotismIndex, candidatos);

        while (upperBound - lowerBound > THRESHOLD) {
            double mid = lowerBound + (upperBound - lowerBound) / 2;
            double eqQuestions = mid;
            double iqQuestions = totalQuestions - mid;

            double medianValue = calculateTimeDifference(eqQuestions, iqQuestions, nepotismIndex, candidatos);
      //      System.out.println("value mejor: " + medianValue);
            if (masIQ >= masEQ) {

                upperBound = mid;
                masEQ = medianValue;

             //   System.out.printf("Optimal time is %f, And now is %f, with EQ questions: %f\n", medianValue, tiempoOptimo, eqQuestions);
                if (medianValue > tiempoOptimo) {
                    eQuestions = eqQuestions;
                    iQuestions = iqQuestions;
                    tiempoOptimo = medianValue;
                }

            } else {
                lowerBound = mid;
                masIQ = medianValue;

                if (medianValue > tiempoOptimo) {
                    eQuestions = eqQuestions;
                    iQuestions = iqQuestions;
                    tiempoOptimo = medianValue;
                }
            }
        }

        if (tiempoOptimo > 0) puedeGanar = true;


         */

        numerosOptimos(totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);

        updateNumerosOptimos(0.00000001, totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);
        updateNumerosOptimos(totalQuestions - 0.00000001, totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);
    }

    /*
    private double calculateTimeDifference(double eqQuestions, double iqQuestions, int nepotismIndex, Candidato[] candidatos) {
        double tiempoNepotism = candidatos[nepotismIndex].calculateTime(eqQuestions, iqQuestions);
        double secondBestTime = Double.MAX_VALUE;

        for (var i : candidatos) {
            if (i == null || i.index == nepotismIndex) continue;
            double candidateTime = i.calculateTime(eqQuestions, iqQuestions);
          //  if (candidateTime < secondBestTime && candidateTime > nepotismTime) {
            if (candidateTime < secondBestTime) {
                secondBestTime = candidateTime;
            }
        }

        double timeDifference = secondBestTime - tiempoNepotism;

        return timeDifference;
    }
     */



    private void numerosOptimos(int totalQuestions, double[] eqSuccessRate, double[] iqSuccessRate, int nepotismIndex) {
        // Loop through all pairs of candidates and find their intersection
        for (int i = 0; i < eqSuccessRate.length; i++) {
            for (int j = i + 1; j < iqSuccessRate.length; j++) {
                if (i == nepotismIndex || j == nepotismIndex) {
                    continue; // Skip the nepotism candidate
                }

                double intersection = encontrarInterseccion(eqSuccessRate[i], iqSuccessRate[i], eqSuccessRate[j], iqSuccessRate[j]);
                if (intersection != -1) updateNumerosOptimos(intersection, totalQuestions, eqSuccessRate, iqSuccessRate, nepotismIndex);
            }
        }
    }

    private double encontrarInterseccion(double eqRateA, double iqRateA, double eqRateB, double iqRateB) {
        double numerator = eqRateA * eqRateB * totalQuestions * (iqRateA - iqRateB);
        double denominator = eqRateA * eqRateB * iqRateA - eqRateA * eqRateB * iqRateB - eqRateA * iqRateA * iqRateB + eqRateB * iqRateA * iqRateB;

        if (denominator == 0) return -1;

        return numerator / denominator;
    }


    private void updateNumerosOptimos(double intersection, int totalQuestions, double[] eqSuccessRate, double[] iqSuccessRate, int nepotismIndex) {
        if (intersection < 0 || intersection > totalQuestions) {
            return; // Intersection is outside the valid range
        }

        // Calculate the time for nepotism candidate at this intersection
        double nepotismTime = calculateTime(intersection, eqSuccessRate[nepotismIndex], iqSuccessRate[nepotismIndex]);
        double bestOtherTime = Double.POSITIVE_INFINITY;

        for (int i = 0; i < eqSuccessRate.length; i++) {
            if (i != nepotismIndex) {
                double candidateTime = calculateTime(intersection, eqSuccessRate[i], iqSuccessRate[i]);
                bestOtherTime = Math.min(bestOtherTime, candidateTime);
            }
        }

        double timeDifference = bestOtherTime - nepotismTime;
        if (timeDifference > tiempoOptimoPosta) {
            tiempoOptimoPosta = timeDifference;
            eQuestionsPosta = intersection;
            iQuestionsPosta = totalQuestions - intersection;
            if (tiempoOptimoPosta > 0)  boca = true;
        }
    }

    private double calculateTime(double eqQuestions, double eqRate, double iqRate) {
        // Calculate the time for a candidate given the number of EQ and IQ questions and their rates
        double timeForEQ = eqQuestions / eqRate;
        double timeForIQ = (totalQuestions - eqQuestions) / iqRate;
        return (timeForEQ + timeForIQ) * 3600;
    }

    @Override
    public boolean canNepotismSucceed() {
        return boca;
    }

    @Override
    public double getNumberEQQuestions() {
        if (boca) {
       //    System.out.println("Forma 1 es: " + eQuestions + ";;; Y forma 2 es: " + eQuestionsPosta);
            return eQuestionsPosta;
        }
        return -1;
    }

    @Override
    public double getNumberIQQuestions() {
        if (boca) {
         //   System.out.println("Forma 1 es: " + iQuestions + ";;; Y forma 2 es: " + iQuestionsPosta);
            return iQuestionsPosta;
        }
        return -1;
    }

    public double tiempoMalo(){
        return tiempoOptimo;
    }

    @Override
    public double getNumberOfSecondsSuccess() {
        if (boca) {
        //    System.out.println("Forma 1 es: " + tiempoOptimo + ";;; Y forma 2 es: " + tiempoOptimoPosta);
            return tiempoOptimoPosta;
        }
        return -1;
    }

    private class Candidato{

        double eqRate;
        double iqRate;
        int index;

        public Candidato(double eQ, double iQ, int indexa){
            this.eqRate = eQ;
            this.iqRate = iQ;
            this.index = indexa;
        }

        public double calculateTime(double eqQuestions, double iqQuestions){
            double tiempo = 0;

            double timeEQ = eqQuestions/eqRate;
            double timeIQ = iqQuestions/iqRate;

            tiempo = (timeEQ + timeIQ) * 3600;

       //     System.out.println(tiempo);
            return tiempo;
        }
    }
}
