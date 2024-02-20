package edu.yu.introtoalgs;

import org.junit.Test;
import static org.junit.Assert.*;

public class BigOIt2Test {

    private static final double DELTA = 1;

    @Test
    public void cuadratica() {
        BigOMeasurable algo = new Cuadratica();
        final BigOIt2Base it = new BigOIt2();
        final String bigOMeasurable = algo.getClass().getName();

        for (int i = 1; i <= 100; i *= 10) {
            it.doublingRatio(bigOMeasurable, 1100);
        }

        final double actual = it.doublingRatio(bigOMeasurable, 12000);
        System.out.println("Ratio: " + actual);
        assertEquals(4.0, actual, DELTA);
    }

    public static class Cuadratica extends BigOMeasurable {
        @Override
        public void setup(final int n) {
            this.n = n;
        }

        @Override
        public void execute() {
            int counted = 0;
            long sum = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    sum += i + j;
                }
            }
        }
    }


    @Test
    public void cubica() {
        BigOMeasurable algo = new Cubica();
        final BigOIt2Base it = new BigOIt2();
        final String bigOMeasurable = algo.getClass().getName();
        for (int i = 1; i <= 100; i *= 10) {
            it.doublingRatio(bigOMeasurable, 1100);
        }
        final double actual = it.doublingRatio(bigOMeasurable, 10000);  // 40 seconds timeout
        System.out.println(actual + "sss");
        assertEquals(8.0, actual, DELTA);
    }

    public static class Cubica extends BigOMeasurable {
        @Override
        public void setup(final int n) {
            this.n = n;
        }

        @Override
        public void execute() {
            long sum = 0;
            for(int i = 0; i < n; i++) {
                for(int j = 0; j < n; j++) {
                    for(int w = 0; w < n; w++){
                        sum += i + j + w;
                    }
                }
            }
        }
    }

    @Test
    public void claseNoValida() {
        BigOIt2 tester = new BigOIt2();
        try {
            tester.doublingRatio("edu.yu.introtoalgs.Aaa", 1000);
            fail("Tendria que fallar");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void malArgumento() {
        BigOIt2 tester = new BigOIt2();
        try {
            tester.doublingRatio("xacsdd", 1000);
            fail("Tendria que fallar");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void primera() {
        var n = 1;
        var na = 3;
        assertEquals(4, na + n);
    }
}
