package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.io.*;
import java.net.*;

public class RoundRobinLeader extends Thread {
    private PeerServer servidor;
    private Map<Long, InetSocketAddress> peerToAdd;
    private List<Long> workers;
    private Logger logger;
    private ExecutorService pool;
    private ConcurrentLinkedQueue<RequestInfo> im;
    private ServerSocket serverSocket;
    private int turno;
    private long requests;
    private volatile boolean isShutdown = false;
    private HashMap<Long, HashSet<RequestInfo>> workAssignments;
    CountDownLatch latch;
    HashMap<Long, Message> oldWork;
    private List<Long> failedWorkers;
    private volatile boolean workerFailed = false;

    private static class RequestInfo {
        final Socket clientSocket;
        final Message mensaje;

        RequestInfo(Socket socket, Message mensaje) {
            try {
                socket.setSoTimeout(20000);
            } catch (SocketException e) {
            }
            this.clientSocket = socket;
            this.mensaje = mensaje;
        }

        public Message getMessage() {
            return mensaje;
        }

        public Socket getClientConnection() {
            return clientSocket;
        }
    }

    public RoundRobinLeader(PeerServer server, int udpPort, Map<Long, InetSocketAddress> peerIDtoAddress, List<Long> workers, CountDownLatch latch, Logger log) {
        this.setDaemon(true);
        this.servidor = server;
        this.peerToAdd = peerIDtoAddress;
        this.workers = new ArrayList<>(workers);
        this.latch = latch;
        this.turno = 0;
        this.requests = 0;
        this.pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3);
        this.im = new ConcurrentLinkedQueue<>();
        this.workAssignments = new HashMap<>();
        this.oldWork = new HashMap<>();
        this.failedWorkers = new ArrayList<>();
        this.logger = log;
        startTCPServer(udpPort + 2);
    }

    public RoundRobinLeader(PeerServer server, int udpPort, Map<Long, InetSocketAddress> peerIDtoAddress, List<Long> workers, CountDownLatch latch, HashMap<Long, Message> oldWork, Logger log) {
        this(server, udpPort, peerIDtoAddress, workers, latch, log);
        this.oldWork = oldWork;
        this.logger = log;
        logger.info("Initialized with old work: " + oldWork.keySet());
    }


    private void startTCPServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            logger.info("TCP Server started on port " + port);

            Thread tcpServerThread = new Thread(() -> {
                while (!isShutdown && !Thread.currentThread().isInterrupted()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        logger.info("Accepted new connection.");
                        handleConnection(clientSocket);
                    } catch (IOException e) {
                        if (isShutdown) break;
                        logger.warning("Error accepting connection: " + e.getMessage());
                    }
                }
            });
            tcpServerThread.setDaemon(true);
            tcpServerThread.start();
        } catch (IOException e) {
            logger.severe("Failed to start TCP server on port " + port + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void handleConnection(Socket clientSocket) {
        try {
            DataInputStream is = new DataInputStream(clientSocket.getInputStream());
            int length = is.readInt();
            byte[] bytes = new byte[length];
            is.readFully(bytes);
            Message mensaje = new Message(bytes);

            if (mensaje.getMessageType() == Message.MessageType.NEW_LEADER_GETTING_LAST_WORK) {
                handleOldWorkRequest(clientSocket);
            } else {
                logger.info("Nuevo ID: " + mensaje.getRequestID());
                im.offer(new RequestInfo(clientSocket, mensaje));
            }
        } catch (IOException e) {
            logger.info("Error in connection: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ee) {}
        }
    }

    @Override
    public void run() {
        while (!isShutdown && !isInterrupted()) {
            if (latch.getCount() == 1) {
                getAllWork();
                latch.countDown();
            }

            if (workerFailed) {
                try {
                    Thread.sleep(1000);
                    workerFailed = false;
                } catch (InterruptedException e) {
                    break;
                }
            }

            RequestInfo requestInfo = im.poll();
            if (requestInfo == null) {
                try {
                    Thread.sleep(100);
                    continue;
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (workers.isEmpty()) {
                logger.warning("No workers available");
                continue;
            }

            if (oldWork.containsKey(requestInfo.getMessage().getRequestID())) {
                handleOldWork(requestInfo);
                continue;
            }

            Long workerServer = workers.get(turno);
            turno = (turno + 1) % workers.size();
            requests++;

            InetSocketAddress worker = peerToAdd.get(workerServer);
            logger.info("Request " + requests + " assigned to worker " + workerServer);

            Message mensajeToWorker = new Message(Message.MessageType.WORK,
                    requestInfo.mensaje.getMessageContents(),
                    servidor.getAddress().getHostString(),
                    servidor.getUdpPort() + 2,
                    worker.getHostString(),
                    worker.getPort() + 2,
                    requests);

            addWorkAssignment(workerServer, requestInfo);
            pool.execute(() -> doWork(worker, requestInfo, mensajeToWorker));
        }
        shutdown();
    }

    private void handleOldWorkRequest(Socket clientSocket) throws IOException {
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        synchronized (oldWork) {
            out.writeInt(oldWork.size());
            for (var i : oldWork.values()) {
                byte[] messageBytes = i.getNetworkPayload();
                out.writeInt(messageBytes.length);
                out.write(messageBytes);
            }
        }
        clientSocket.close();
    }

    private void handleOldWork(RequestInfo requestInfo) {
        try {
            DataOutputStream out = new DataOutputStream(requestInfo.getClientConnection().getOutputStream());
            byte[] messageBytes = oldWork.get(requestInfo.getMessage().getRequestID()).getNetworkPayload();
            out.writeInt(messageBytes.length);
            out.write(messageBytes);
            logger.info(String.format("Sent back cached request %d", requestInfo.getMessage().getRequestID()));
            oldWork.remove(requestInfo.getMessage().getRequestID());
        } catch (IOException e) {
            logger.warning("Error handling old work: " + e.getMessage());
        }
    }

    private void getAllWork() {
        for (var i : workers) {
            try {
                InetSocketAddress worker = peerToAdd.get(i);
                logger.info("Connecting to worker " + i);
                Socket workerSocket = new Socket(worker.getHostString(), worker.getPort() + 2);

                DataOutputStream out = new DataOutputStream(workerSocket.getOutputStream());
                DataInputStream in = new DataInputStream(workerSocket.getInputStream());

                Message request = new Message(Message.MessageType.NEW_LEADER_GETTING_LAST_WORK, new byte[0], servidor.getAddress().getHostString(), servidor.getUdpPort() + 2, worker.getHostString(), worker.getPort());

                byte[] requestBytes = request.getNetworkPayload();
                out.writeInt(requestBytes.length);
                out.write(requestBytes);

                int messageCount = in.readInt();
                for (var w = 0; w < messageCount; w++) {
                    int length = in.readInt();
                    byte[] messageBytes = new byte[length];
                    in.readFully(messageBytes);
                    Message response = new Message(messageBytes);
                    logger.info("Received old work " + response.getRequestID() + " from worker " + i);
                    oldWork.put(response.getRequestID(), response);
                }
                workerSocket.close();
            } catch (IOException e) {
                logger.fine("Error collecting work from worker " + i + ": " + e.getMessage());
            }
        }
    }

    private void doWork(InetSocketAddress workerAddress, RequestInfo requestInfo, Message mensaje) {
        Long workerId = getWorkerId(workerAddress);
        int retryCount = 0;
        final int maxRetries = 3;

        while (retryCount < maxRetries) {
            try (Socket workerSocket = new Socket()) {
                workerSocket.connect(new InetSocketAddress(workerAddress.getHostName(), workerAddress.getPort() + 2), 3000);
                DataOutputStream out = new DataOutputStream(workerSocket.getOutputStream());
                DataInputStream in = new DataInputStream(workerSocket.getInputStream());

                out.writeInt(mensaje.getNetworkPayload().length);
                out.write(mensaje.getNetworkPayload());

                int responseLength = in.readInt();
                byte[] response = new byte[responseLength];
                in.readFully(response);

                Message responseMessage = new Message(response);
                sendResponseToGateway(requestInfo.clientSocket, responseMessage);
                removeWorkAssignment(workerId, requestInfo);
                return;
            } catch (IOException e) {
                logger.warning("Failed to send work to worker " + workerId + ": " + e.getMessage());
                retryCount++;
            }
        }

        logger.severe("Worker " + workerId + " failed after " + maxRetries + " retries.");
        reportFailedWorker(workerId);
        im.offer(requestInfo);
    }

    private Long getWorkerId(InetSocketAddress address) {
        for (Map.Entry<Long, InetSocketAddress> entry : peerToAdd.entrySet()) {
            if (entry.getValue().equals(address)) return entry.getKey();
        }
        return null;
    }

    private void sendResponseToGateway(Socket clientSocket, Message response) throws IOException {
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        byte[] responseBytes = response.getNetworkPayload();
        out.writeInt(responseBytes.length);
        out.write(responseBytes);
        clientSocket.close();
    }

    private synchronized void addWorkAssignment(Long workerId, RequestInfo request) {
        workAssignments.computeIfAbsent(workerId, k -> new HashSet<>()).add(request);
    }

    private synchronized void removeWorkAssignment(Long workerId, RequestInfo request) {
        if (workAssignments.containsKey(workerId)) workAssignments.get(workerId).remove(request);
    }

    public synchronized void reportFailedWorker(Long workerId) {
        if (!failedWorkers.contains(workerId)) {
            failedWorkers.add(workerId);
            workers.remove(workerId);
            workerFailed = true;
            logger.warning("Worker " + workerId + " reported as failed");

            if (workAssignments.containsKey(workerId)) {
                HashSet<RequestInfo> failedWork = workAssignments.remove(workerId);
                if (failedWork != null) {
                    for (var i : failedWork) {
                        im.offer(i);
                    }
                }
            }
            if (turno >= workers.size()) turno = 0;
        }
    }

    public void shutdown() {
        isShutdown = true;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();

            if (pool != null) {
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(5, TimeUnit.SECONDS)) pool.shutdownNow();
                } catch (InterruptedException e) {
                    pool.shutdownNow();
                }
            }

            logger.info("Successfully shutdown leader on: " + this.servidor.getServerId());
        } catch (IOException e) {
            logger.severe("Error during shutdown: " + e.getMessage());
        }
        interrupt();
    }
}