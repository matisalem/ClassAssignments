package edu.yu.introtoalgs;

import java.util.*;
import java.util.concurrent.*;
public class TxSortFJ extends TxSortFJBase{

    List<TxBase> transacciones;
    Tx[] tr;

    public TxSortFJ(List<TxBase> transactions){
        super(transactions);

        if (transactions == null) throw new IllegalArgumentException();

        transacciones = transactions;

        tr = new Tx[transactions.size()];

        int counter = 0;

        for (var i : transactions){
            tr[counter] = (Tx) i;
            counter++;
        }
    }


    /*
    public TxBase[] arraySort() {
        Tx[] copiedArray = Arrays.copyOf(tr, tr.length);
        Arrays.sort(copiedArray);
        return copiedArray;
    }
     */


    /** Returns an array of transactions, sorted in ascending order of
     * TxBase.time() values: any instances with null TxBase.time() values precede
     * all other transaction instances in the sort results.
     *
     * @return the transaction instances passed to the constructor, returned as
     * an array, and sorted as specified above.  Students MAY ONLY use the
     * ForkJoin and their own code in their implementation.
     */
    @Override
    public TxBase[] sort() {

        ForkJoinPool pool = new ForkJoinPool();
        try {
            Sortear task = new Sortear(tr, 0, tr.length);
            return pool.invoke(task);
        } finally {
            pool.shutdown();
        }
    }


    private static class Sortear extends RecursiveTask<Tx[]> {
        private final Tx[] array;
        private final int low;
        private final int high;

        public Sortear(Tx[] array, int low, int high) {
            this.array = array;
            this.low = low;
            this.high = high;
        }

        @Override
        protected Tx[] compute() {
            if (high - low <= 8) {
                    Tx[] copia = Arrays.copyOfRange(array, low, high);
                    Arrays.sort(copia);
                    return copia;
           ///    return Arrays.copyOfRange(array, low, high);
            }

            int mid = low + (high - low) / 2;
            Sortear izq = new Sortear(array, low, mid);
            Sortear der = new Sortear(array, mid, high);

            izq.fork();
            Tx[] derR = der.compute();
            Tx[] izqR = izq.join();

            return merge(izqR, derR);
        }

        private Tx[] merge(Tx[] izq, Tx[] der) {
            Tx[] resultado = new Tx[izq.length + der.length];
            int i = 0, j = 0, k = 0;
            while (i < izq.length && j < der.length) {

                if (izq[i].compareTo(der[j]) <= 0) {
                    resultado[k++] = izq[i++];
                } else {
                    resultado[k++] = der[j++];
                }
            }
            while (i < izq.length) {
                resultado[k++] = izq[i++];
            }
            while (j < der.length) {
                resultado[k++] = der[j++];
            }

            return resultado;
        }
    }

}