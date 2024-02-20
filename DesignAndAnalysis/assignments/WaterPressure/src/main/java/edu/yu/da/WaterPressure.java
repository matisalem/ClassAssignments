package edu.yu.da;

import java.util.*;

public class WaterPressure extends WaterPressureBase{


    String inicial;
    String secondOutput;
    Graph graph;
    boolean seInvocoMinAmount;
    boolean esPosible;
    boolean isFirstCall;
    boolean seInvocoCon2;
    private Map<String, Double> distToVertices;
    Double valor;


    public WaterPressure(String initialInputPump) {
        super(initialInputPump);

        if (initialInputPump == null || initialInputPump.isEmpty()) throw new IllegalArgumentException();
        graph = new Graph();
        graph.addFirstVertex(initialInputPump);
        secondOutput = null;
        esPosible = false;
        isFirstCall = true;
        seInvocoCon2 = false;
        valor = -2.0;

        inicial = initialInputPump;
        seInvocoMinAmount = false;
    }



    @Override
    public void addSecondInputPump(String secondInputPump) {
        if (secondInputPump == null || secondInputPump.isEmpty() || secondInputPump.equals(inicial) || !(graph.vertices().contains(secondInputPump)) || secondOutput != null) throw  new IllegalArgumentException();

        secondOutput = secondInputPump;
    }



    /** Specifies a blockage on a channel running from pump station v to pump
     * station w.  The presence of a blockage implies that water can only flow on
     * the channel if a quantity of water greater or equal to "blockage" is
     * pumped by pump station v to w.
     *
     * The two pump stations must differ from one another, and no channel can
     * already exist between the two pump stations.
     *
     * @param v specifies a pump station, length must be > 0.
     * @param w specifies a pump station, length must be > 0.
     * @param blockage the magnitude of the blockage on the channel, must be > 0.
     * @throws IllegalStateException if minAmount() has previously been invoked.
     * @throws IllegalArgumentException if the other pre-conditions are violated.
     */
    @Override
    public void addBlockage(String v, String w, double blockage) {

        if (v == null || w == null | v.isEmpty()|| w.isEmpty()|| v.equals(w) || blockage <= 0) throw new IllegalArgumentException();
        if (seInvocoMinAmount) throw new IllegalStateException();

        if (graph.existsAPath(v, w)) throw new IllegalArgumentException();

        graph.addEdge(v, w, blockage);
    }


    /** Client asks implementation to determine the minimum amount of water that
     * must be supplied to the initial input pump to ensure that water reaches
     * every pump station in the existing channel system.  If a second input pump
     * has been added to the channel system, the sematics of "minimum amount" is
     * the "minimum amount of water that must be supplied to BOTH input pump
     * stations".
     *
     * @return the minimum amount of water that must be supplied to the input
     * pump(s) to ensure that water reaches every pump station.  If the channel
     * system has been misconfigured such that no amount of water pumped from the
     * input pump stations can get water to all the pump stations, returns -1.0
     * as as sentinel value.
     */

    @Override
    public double minAmount() {

        // PREGUNTAR ESTAS EXCEPCIONES
        if (seInvocoCon2 || seInvocoMinAmount && secondOutput == null) return valor;

        seInvocoMinAmount = true;

        Prim dsp = new Prim(graph, inicial);
        Prim dsp2;
        List<String> verticesInOrder = getVerticesByDistance();


        if (secondOutput != null){
            dsp2 = new Prim(graph, secondOutput);
            verticesInOrder = getVerticesByDistance();
            seInvocoCon2 = true;
        }

        valor = distToVertices.get(verticesInOrder.get(0));

        if (valor == Double.POSITIVE_INFINITY) return -1;


        return valor;
    }


    private List<String> getVerticesByDistance() {
        List<Map.Entry<String, Double>> list = new ArrayList<>(distToVertices.entrySet());
        list.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        List<String> sortedVertices = new ArrayList<>();
        for (var i : list) {
            sortedVertices.add(i.getKey());
        }
        return sortedVertices;
    }

    private class Prim {

        private Set<String> marked;
        private Map<String, Edge> edgeTo;
        private Map<String, Double> distTo;
        private IndexMinPQ<Double> pq;


        public Prim(Graph G, String empezar) {

            marked = new HashSet<>();
            edgeTo = new HashMap<>();
            distTo = new HashMap<>();
            pq = new IndexMinPQ<>(G.vertices().size());

            if (isFirstCall) {
                for (String vertex : G.vertices()) {
                    distTo.put(vertex, Double.POSITIVE_INFINITY);
                }
            } else {
                distTo = distToVertices;
            }


            distTo.put(empezar, 0.0);
            pq.insert(empezar, 0.0);

            while (!pq.isEmpty()) {
                String v = pq.delMin();
                marked.add(v);
                for (Edge e : G.adj(v)) {
                    relax(G, e);
                }
            }

            distToVertices = distTo;
            isFirstCall = false;
            }

        private void relax(Graph G, Edge e) {
            String v = e.from;
            String w = e.dondeVa();
            if (!marked.contains(w) && e.getWeight() < distTo.get(w)) {
                distTo.put(w, e.getWeight());
                edgeTo.put(w, e);

                if (pq.contains(w)) {
                    pq.changeKey(w, distTo.get(w));
                } else {
                    pq.insert(w, distTo.get(w));
                }
            }
        }

        }

    private class IndexMinPQ<Key extends Comparable<Key>> {

        private Map<String, Key> keys;
        private PriorityQueue<String> priorityQueue;

        public IndexMinPQ(int maxN) {
            this.keys = new HashMap<>();
            this.priorityQueue = new PriorityQueue<>(maxN, Comparator.comparing(keys::get));
        }

        public boolean isEmpty() {
            return priorityQueue.isEmpty();
        }

        public boolean contains(String key) {
            return keys.containsKey(key);
        }

        public void insert(String key, Key value) {
            if (contains(key)) {
                changeKey(key, value);
            } else {
                keys.put(key, value);
                priorityQueue.add(key);
            }
        }

        public void changeKey(String key, Key value) {
            if (!contains(key)) throw new NoSuchElementException();

            keys.put(key, value);
            priorityQueue.remove(key);
            priorityQueue.add(key);
        }

        public String delMin() {
            String minKey = priorityQueue.poll();
            keys.remove(minKey);
            return minKey;
        }
    }

    private class Graph {
        private Map<String, List<Edge>> adjacencyList;

        public Graph() {
            adjacencyList = new HashMap<>();
        }

        public void addFirstVertex(String v){
            adjacencyList.putIfAbsent(v, new ArrayList<>());
        }

        public void addEdge(String v1, String v2, double weight) {
            adjacencyList.putIfAbsent(v1, new ArrayList<>());
            adjacencyList.putIfAbsent(v2, new ArrayList<>());
            Edge edge = new Edge(v1, v2, weight);
            adjacencyList.get(v1).add(edge);
        }

        public boolean existsAPath(String from, String to){
            List lista = adjacencyList.get(from);

            if (lista == null) return false;

            if (lista.contains(to)) return true;
            return false;
        }

        public ArrayList<String> vertices() {
            return new ArrayList<>(adjacencyList.keySet());
        }

        public Iterable<Edge> adj(String vertex) {
            return adjacencyList.getOrDefault(vertex, Collections.emptyList());
        }

    }

    private class Edge {
        String from;
        String to;
        double weight;

        public Edge(String from, String to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        public double getWeight() {
            return weight;
        }

        public String dondeVa() {
            return to;
        }
    }

}
