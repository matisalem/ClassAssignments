package edu.yu.introtoalgs;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Tx extends TxBase {

    Account remitente;
    Account receptor;
    int cantidad;
    long id;
    private LocalDateTime time;
    private static AtomicLong nextId = new AtomicLong(1);


    /** Constructor.
     *
     * @param sender non-null initiator of the transaction
     * @param receiver non-null recipient
     * @param amount positive-integer-valued amount transfered in the
     * transaction.
     */
    public Tx(Account sender, Account receiver, int amount) {
        super(sender, receiver, amount);

        if (sender == null || receiver == null || amount < 1) throw new IllegalArgumentException();

        remitente = sender;
        receptor = receiver;
        cantidad = amount;
        this.id = nextId.getAndIncrement();
        this.time = LocalDateTime.now();

    }

    @Override
    public Account receiver() {
        return receptor;
    }

    @Override
    public Account sender() {
        return remitente;
    }

    @Override
    public int amount() {
        return cantidad;
    }

    @Override
    public long id() {
        return id;
    }


    /**
     * Returns the time that the Tx was created or null.
     */
    @Override
    public LocalDateTime time() {
        return time;
    }


    /**
     * Returns the time that the Tx was created or null.
     */
    @Override
    public void setTimeToNull() {
        this.time = null;
    }

    @Override
    public String toString() {
        return "Tx{" +
                "sender=" + sender() +
                ", receiver=" + receiver() +
                ", amount=" + amount() +
                ", id=" + id() +
                ", time=" + time() +
                '}';
    }

    @Override
    public int compareTo(TxBase other) {
        // Example: Compare based on time; you can change this logic as needed.

        if (this.time == null && other.time() == null) return 0;
        if (this.time == null) return -1;
        if (other.time() == null) return 1;

        int tiempo = this.time.compareTo(other.time());
        if (tiempo != 0){
            return tiempo;
        } else {
            if (this.id < other.id()) return -1;
            return 1;
        }
    }

}
