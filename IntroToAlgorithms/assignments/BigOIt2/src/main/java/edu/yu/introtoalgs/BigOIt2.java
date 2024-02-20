package edu.yu.introtoalgs;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

public class BigOIt2 extends BigOIt2Base {

    @Override
    public double doublingRatio(String bigOMeasurable, long timeOutInMs) {

        Class<?> clase;

        try {
            clase = Class.forName(bigOMeasurable);
            if (!BigOMeasurable.class.isAssignableFrom(clase)) {
                throw new IllegalArgumentException();
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException();
        }

        BigOMeasurable instancia;
        try {
            instancia = (BigOMeasurable) clase.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalArgumentException();
        }

        if (timeOutInMs < 500)
            return Double.NaN;

        List<Double> ratios = new ArrayList<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Double> future = executor.submit(() -> {
                    int n = 1000;
                    int counter = 0;

                    while (true) {
                        long n1 = runAlgorithm(instancia, n, timeOutInMs);
                        System.out.println(n1 + " n1");
                        if (n1 == -1) {
                            break;
                        }

                        long n2 = runAlgorithm(instancia, 2 * n, timeOutInMs);
                        System.out.println(n2 + " n2");
                        if (n2 == -1) {
                            break;
                        }

                        double ratio = (double) n2 / n1;
                        ratios.add(ratio);

                        if (counter == 10) {
                            break;
                        }

                        n *= 2;
                        counter++;
                    }
            return null;
        });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                future.cancel(true);
            }
        }, timeOutInMs - 1000);

        Double result;
        try {
            future.get(timeOutInMs, TimeUnit.MILLISECONDS);
        } catch (CancellationException e) {

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdownNow();
            timer.cancel();
        }

        if (ratios.isEmpty()) {
            return Double.NaN;
        }

        double sum = 0;
        for (var i : ratios) {
            sum += i;
        }

        return sum / ratios.size();
    }


    private long runAlgorithm(BigOMeasurable instancia, int n, long timeOutInMs) {
        instancia.setup(n);
        long startTime = System.nanoTime();
        instancia.execute();
        long endTime = System.nanoTime();
        long timeTakenNormal = (endTime - startTime);
        long timeTaken = timeTakenNormal / 1000000;

        if (timeTaken > timeOutInMs) {
            return -1;
        }
        return timeTakenNormal;
    }
}