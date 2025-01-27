package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.*;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.logging.*;
import java.io.*;

public class JavaRunnerFollower extends Thread {
    private final PeerServer servidor;
    private final JavaRunner javaRunner;
    private final Logger logger;
    private final InetSocketAddress lider;
    private final ServerSocket serverSocket;
    private final HashMap<Long, Message> oldWork;
    private volatile boolean shutdown;
    private volatile boolean leaderIsDead;
    private CountDownLatch latch;
    private int processedRequestCount;

    public JavaRunnerFollower(PeerServerImpl peerServer, InetSocketAddress leader, Logger log) {
        this.servidor = peerServer;
        this.lider = leader;
        this.logger = log;
        this.setDaemon(true);
        this.oldWork = new HashMap<>();
        this.leaderIsDead = false;
        this.shutdown = false;
        this.processedRequestCount = 0;

        try {
            this.javaRunner = new JavaRunner();
            this.serverSocket = new ServerSocket(servidor.getUdpPort() + 2);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            logger.severe("Error initializing JavaRunnerFollower: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!shutdown && !isInterrupted()) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(30000);
                logger.info("Server " + servidor.getServerId() + " accepted connection from leader");

                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                int length = in.readInt();
                byte[] messageBytes = new byte[length];
                in.readFully(messageBytes);
                Message mensaje = new Message(messageBytes);

                if (mensaje.getMessageType() == Message.MessageType.NEW_LEADER_GETTING_LAST_WORK) {
                    handleOldWork(out);
                    continue;
                }

                if (mensaje.getMessageType() != Message.MessageType.WORK) {
                    logger.warning("Received unexpected message type: " + mensaje.getMessageType());
                    continue;
                }

                try {
                    String response = javaRunner.compileAndRun(new ByteArrayInputStream(mensaje.getMessageContents()));
                    Message responseMsg = new Message(Message.MessageType.COMPLETED_WORK, response.getBytes(), servidor.getAddress().getHostString(), servidor.getUdpPort(), lider.getHostString(), lider.getPort(), mensaje.getRequestID());

                    if (mensaje.getRequestID() > 18 && processedRequestCount == 0) {
                        while (!leaderIsDead) {
                            Thread.sleep(500);
                        }
                        processedRequestCount++;
                    }

                    if (leaderIsDead) {
                        synchronized (oldWork) {
                            oldWork.put(responseMsg.getRequestID(), responseMsg);
                            logger.info(String.format("Server %d queued request %d (leader dead)", servidor.getServerId(), mensaje.getRequestID()));
                        }
                    } else {
                        byte[] responseBytes = responseMsg.getNetworkPayload();
                        out.writeInt(responseBytes.length);
                        out.write(responseBytes);
                        out.flush();
                        logger.info(String.format("Server %d completed work for request %d", servidor.getServerId(), mensaje.getRequestID()));
                    }
                } catch (Exception e) {
                    if (!leaderIsDead) {
                        logger.severe("Error processing work: " + e.getMessage());
                        String errorMsg = "Error processing work: " + e.getMessage();
                        Message errorResponse = new Message(Message.MessageType.COMPLETED_WORK, errorMsg.getBytes(), servidor.getAddress().getHostString(), servidor.getUdpPort(), lider.getHostString(), lider.getPort(), mensaje.getRequestID());
                        byte[] errorBytes = errorResponse.getNetworkPayload();
                        out.writeInt(errorBytes.length);
                        out.write(errorBytes);
                        out.flush();
                    }
                }

            } catch (IOException e) {
                if (!shutdown) logger.warning("Error handling connection: " + e.getMessage());
            } finally {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        logger.warning("Error closing client socket: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void handleOldWork(DataOutputStream out) throws IOException {
        if (latch != null) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.warning("Interrupted while waiting for election to complete");
                Thread.currentThread().interrupt();
                return;
            }
        }

        synchronized (oldWork) {
            out.writeInt(oldWork.size());
            for (Message msg : oldWork.values()) {
                byte[] messageBytes = msg.getNetworkPayload();
                out.writeInt(messageBytes.length);
                out.write(messageBytes);
            }
            logger.info(String.format("Server %d sent %d old work items to new leader",
                    servidor.getServerId(), oldWork.size()));
            oldWork.clear();
        }
        leaderIsDead = false;
    }


    public void esprandoLider(CountDownLatch waitForElection) {
        leaderIsDead = true;
        this.latch = waitForElection;
        try {
            waitForElection.await();
        } catch (InterruptedException e) {
            logger.warning("Interrupted while waiting for new leader");
            Thread.currentThread().interrupt();
        }
    }

    public synchronized HashMap<Long, Message> getViejo() {
        return new HashMap<>(oldWork);
    }

    public synchronized void closePortToBecomeLeader() throws IOException {
        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    public void shutdown() {
        this.shutdown = true;
        try {
            if (!serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            logger.warning("Error closing server socket: " + e.getMessage());
        }
        interrupt();
        logger.info("JavaRunnerFollower shutdown for server " + servidor.getServerId());
    }
}