package edu.yu.cs.com3800.stage4;

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
    }

    public RoundRobinLeader(PeerServer server, int udpPort, Map<Long, InetSocketAddress> peerIDtoAddress, List<Long> workers, Logger log) {
        this.servidor = server;
        this.peerToAdd = peerIDtoAddress;
        this.workers = workers;
        turno = 0;
        requests = 0;
        this.setDaemon(true);
        this.logger = log;
        this.pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3);
        this.im = new ConcurrentLinkedQueue<>();
        startTCPServer(udpPort + 5);
    }

    private void startTCPServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            logger.info("TCP " + port);

            Thread tcpServerThread = new Thread(() -> {
                while (!isShutdown && !Thread.currentThread().isInterrupted()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        conection(clientSocket);
                    } catch (IOException e) {
                        if (!isShutdown) logger.info("Error: " + e.getMessage());
                    }
                }
            });
            tcpServerThread.setDaemon(true);
            tcpServerThread.start();

        } catch (IOException e) {
            logger.info("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void conection(Socket clientSocket) {
        try {
            DataInputStream is = new DataInputStream(clientSocket.getInputStream());
            int length = is.readInt();
            byte[] bytes = new byte[length];
            is.readFully(bytes, 0, bytes.length);
            Message mensaje = new Message(bytes);

            logger.info("Nuevo request");
            im.offer(new RequestInfo(clientSocket, mensaje));
        } catch (IOException e) {
            logger.info("Error: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ee) {}
        }
    }

    private void doWork(InetSocketAddress workerAddress, RequestInfo requestInfo, Message mensaje) {
        try {
            Socket workerSocket = new Socket(workerAddress.getHostString(), workerAddress.getPort() + 5);
            DataOutputStream workerOut = new DataOutputStream(workerSocket.getOutputStream());
            DataInputStream workerIn = new DataInputStream(workerSocket.getInputStream());

            byte[] messageBytes = mensaje.getNetworkPayload();
            workerOut.writeInt(messageBytes.length);
            workerOut.write(messageBytes);

            while (workerIn.available() == 0) {
                Thread.sleep(100);
            }

            int l = workerIn.readInt();
            byte[] bytes = new byte[l];
            workerIn.readFully(bytes);

            Message respuesa = new Message(bytes);
            Message gatewayRespuesta = new Message(Message.MessageType.COMPLETED_WORK, respuesa.getMessageContents(), servidor.getAddress().getHostString(), servidor.getUdpPort() + 5, requestInfo.mensaje.getSenderHost(), requestInfo.mensaje.getSenderPort(), respuesa.getRequestID());

            DataOutputStream gatewayInfo = new DataOutputStream(requestInfo.clientSocket.getOutputStream());
            byte[] responseBytes = gatewayRespuesta.getNetworkPayload();
            gatewayInfo.writeInt(responseBytes.length);
            gatewayInfo.write(responseBytes);

            workerSocket.close();
            requestInfo.clientSocket.close();

        } catch (IOException | InterruptedException e) {
            logger.info("Error: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while (!isShutdown && !isInterrupted()) {

            RequestInfo requestInfo = im.poll();
            if (requestInfo == null) {
                try {
                    Thread.sleep(100);
                    continue;
                } catch (InterruptedException e) {
                    break;
                }
            }

            Long workerServer = workers.get(turno);
            turno = (turno + 1) % workers.size();
            requests++;

            InetSocketAddress worker = peerToAdd.get(workerServer);
            logger.info(("request: " + requests + ". Worker: " + workerServer));

            Message mensajeToWorker = new Message(Message.MessageType.WORK, requestInfo.mensaje.getMessageContents(), servidor.getAddress().getHostString(), servidor.getUdpPort() + 5, worker.getHostString(), worker.getPort() + 5, requests);
            pool.execute(() -> doWork(worker, requestInfo, mensajeToWorker));
        }

        shutdown();
    }

    public void shutdown() {
        isShutdown = true;
        try {
            if (serverSocket != null && !serverSocket.isClosed())serverSocket.close();
        } catch (IOException e) {
            logger.info("Error: " + e.getMessage());
        }
        pool.shutdownNow();
        interrupt();
    }
}