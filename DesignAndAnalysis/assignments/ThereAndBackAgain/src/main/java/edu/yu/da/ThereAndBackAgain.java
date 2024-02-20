package edu.yu.da;

import java.util.*;


public class ThereAndBackAgain extends ThereAndBackAgainBase{

    private Graph graph;
    boolean seInvocoDoItParaTirarUnErrorEnElAlgoritmo = false;
    String startVertex;
    String goalVertex;
    List<String> primero = null;
    List<String> segundo = null;
    boolean esPosible = false;
    double tiempo = 0;
    private Map<String, Double> distToVertices;
    boolean existia = false;


    public ThereAndBackAgain(String startVertex){
        super(startVertex);
        if (startVertex == null || startVertex.isEmpty()) throw new IllegalArgumentException();
        this.graph = new Graph();
        this.startVertex = startVertex;
    }

    @Override
    public void addEdge(String v, String w, double weight) {
        if (v == null || w == null || v.isEmpty() || w.isEmpty() || v.equals(w) || weight <= 0) throw new IllegalArgumentException();
        if (seInvocoDoItParaTirarUnErrorEnElAlgoritmo) throw new IllegalStateException();

        List<Edge> edges = graph.adjacencyList.get(v);
        if (edges != null) {
            for (Edge edge : edges) {
                if (edge.other(v).equals(w)) throw new IllegalArgumentException();
            }
        }

        graph.addEdge(v, w, weight);
    }

    @Override
    public void doIt() {
        if (seInvocoDoItParaTirarUnErrorEnElAlgoritmo) throw new IllegalStateException();
        seInvocoDoItParaTirarUnErrorEnElAlgoritmo = true;

        DijkstraSP dsp = new DijkstraSP(graph, startVertex);
        List<String> verticesInOrder = dsp.getVerticesByDistance();

        if (!esPosible) return;

        for (var i : verticesInOrder) {
            goalVertex = i;
            primero = dsp.constructPath(dsp.edgeTo, startVertex, i);
            segundo = dsp.constructPath(dsp.secondEdgeTo, startVertex, i);

            if (primero.size() != segundo.size()) break;
            Set<String> pathSet = new HashSet<>(primero);
            for (var w : segundo) {
                if (!pathSet.contains(w)) existia = true;
            }
            if (existia) break;
        }

        tiempo = distToVertices.get(goalVertex);

    }

    @Override
    public String goalVertex() {
        if (!esPosible) return null;
        return goalVertex;
    }

    @Override
    public double goalCost() {
        if (!esPosible) return 0.0;
        return tiempo;
    }

    @Override
    public List<String> getOneLongestPath() {
        if (!esPosible) return Collections.emptyList();
        if (primero.hashCode() < segundo.hashCode()) return primero;
        return segundo;
    }

    @Override
    public List<String> getOtherLongestPath() {
        if (!esPosible) return Collections.emptyList();
        if (primero.hashCode() > segundo.hashCode()) return primero;
        return segundo;
    }

    private class DijkstraSP {
        private Map<String, Edge> edgeTo;
        private Map<String, Edge> secondEdgeTo;
        private Map<String, Double> distTo;
        private IndexMinPQ<Double> pq;

        public DijkstraSP(Graph G, String startVertex) {
            edgeTo = new HashMap<>();
            secondEdgeTo = new HashMap<>();
            distTo = new HashMap<>();
            pq = new IndexMinPQ<>(G.vertices().size());

            for (String vertex : G.vertices()) {
                distTo.put(vertex, Double.POSITIVE_INFINITY);
            }

            distTo.put(startVertex, 0.0);
            pq.insert(startVertex, 0.0);

            while (!pq.isEmpty()) {
                String v = pq.delMin();
                for (Edge e : G.adj(v)) {
                    relax(e, v);
                }
            }

            distToVertices = distTo;
        }

        private void relax(Edge e, String vertex) {
            String w = e.other(vertex);
            double weight = e.getWeight();
            if (distTo.get(w) > distTo.get(vertex) + weight) {
                distTo.put(w, distTo.get(vertex) + weight);
                edgeTo.put(w, e);
                secondEdgeTo.put(w, e);
                if (pq.contains(w)) {
                    pq.changeKey(w, distTo.get(w));
                } else {
                    pq.insert(w, distTo.get(w));
                }
                } else if (distTo.get(w) == distTo.get(vertex) + weight){
                esPosible = true;
                secondEdgeTo.put(w, e);
                pq.changeKey(w, distTo.get(w));
            }

            }
        public List<String> getVerticesByDistance() {
            List<Map.Entry<String, Double>> list = new ArrayList<>(distTo.entrySet());
            list.sort(Map.Entry.<String, Double>comparingByValue().reversed());

            List<String> sortedVertices = new ArrayList<>();
            for (var i : list) {
                sortedVertices.add(i.getKey());
            }
            return sortedVertices;
        }

        private List<String> constructPath(Map<String, Edge> edgeMap, String startVertex, String endVertex) {
            LinkedList<String> path = new LinkedList<>();
            for (String vertex = endVertex; !vertex.equals(startVertex); vertex = edgeMap.get(vertex).other(vertex)) {
                path.addFirst(vertex);
            }
            path.addFirst(startVertex);
            return path;
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

        public void addEdge(String v1, String v2, double weight) {

            adjacencyList.putIfAbsent(v1, new ArrayList<>());
            adjacencyList.putIfAbsent(v2, new ArrayList<>());

            Edge edge = new Edge(v1, v2, weight);

            adjacencyList.get(v1).add(edge);
            adjacencyList.get(v2).add(edge);
        }

        public ArrayList<String> vertices() {
            return new ArrayList<>(adjacencyList.keySet());
        }

        public Iterable<Edge> adj(String vertex) {
            return adjacencyList.getOrDefault(vertex, Collections.emptyList());
        }

    }

    private class Edge {
        String vertex1;
        String vertex2;
        double weight;

        public Edge(String vertex1, String vertex2, double weight) {
            this.vertex1 = vertex1;
            this.vertex2 = vertex2;
            this.weight = weight;
        }

        public String other(String vertex) {
            if (vertex.equals(vertex1)) {
                return vertex2;
            } else if (vertex.equals(vertex2)) {
                return vertex1;
            } else {
                throw new IllegalArgumentException();
            }
        }

        public double getWeight() {
            return weight;
        }
    }
}