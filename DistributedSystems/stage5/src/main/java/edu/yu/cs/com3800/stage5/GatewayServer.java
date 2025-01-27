package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.*;
import com.sun.net.httpserver.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class GatewayServer implements LoggingServer {
    private HttpServer httpServidor;
    private HttpServer electionService;
    private GatewayPeerServerImpl servidor;
    private Logger logger;
    private int request = 0;
    private volatile boolean leaderIsDead;
    private CountDownLatch waitForElection;

    public GatewayServer(int httpPort, int peerPort, long peerEpoch, Long serverID, HashMap<Long, InetSocketAddress> peerIDtoAddress, int numberOfObservers) throws IOException {
        try {
            httpServidor = HttpServer.create(new InetSocketAddress(httpPort), 0);
            electionService = HttpServer.create(new InetSocketAddress(httpPort-1000), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.servidor = new GatewayPeerServerImpl(peerPort, peerEpoch, serverID, peerIDtoAddress, this);
        this.waitForElection = new CountDownLatch(1);
        this.leaderIsDead = false;

        loadLoggers();

        httpServidor.createContext("/compileandrun", new CosoHandler());
        httpServidor.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3));
        electionService.createContext("/isLeaderElected", new EleccionHandler());
        electionService.setExecutor(null);

        logger.info("Gateway server initialized with server ID: " + servidor.getServerId());
    }

    private void loadLoggers(){
        logger = Logger.getLogger("GatewayLogger");
        File path = new File("logs");
        path.mkdir();
        FileHandler fh = null;
        try {
            fh = new FileHandler("logs/Gateway.log");
        } catch (IOException e) {}
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
    }

    public PeerServer.ServerState getState() {
        return servidor.getPeerState();
    }

    private class CosoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Message returnMessage = null;

            try {
                if (!exchange.getRequestMethod().equals("POST")) {
                    sendResponse(exchange, 405, "Method not allowed");
                    return;
                }

                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                if (!"text/x-java-source".equals(contentType)) {
                    sendResponse(exchange, 400, "Invalid content type");
                    return;
                }

                InputStream is = exchange.getRequestBody();
                String code = new String(is.readAllBytes());

                InetSocketAddress leaderAddr = getLeaderAddress();
                int tcpPort = leaderAddr.getPort() + 2;
                final String host = System.getProperty("host", "localhost");

                Message message = new Message(Message.MessageType.WORK, code.getBytes(), getAddress().getHostString(), getAddress().getPort(), leaderAddr.getHostString(), tcpPort, request++);

                try (Socket clientSocket = new Socket(host, tcpPort);
                     DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
                     DataInputStream ois = new DataInputStream(clientSocket.getInputStream())) {

                    byte[] messageBytes = message.getNetworkPayload();
                    os.writeInt(messageBytes.length);
                    os.write(messageBytes);

                    while (ois.available() == 0) {
                        if (leaderIsDead) {
                            electNewLeaderAndFinish(host, message, exchange);
                            return;
                        }
                        Thread.sleep(500);
                    }

                    if (leaderIsDead) {
                        electNewLeaderAndFinish(host, message, exchange);
                        return;
                    }

                    int length = ois.readInt();
                    byte[] responseBytes = new byte[length];
                    ois.readFully(responseBytes, 0, responseBytes.length);
                    returnMessage = new Message(responseBytes);
                }

                if (returnMessage != null) {
                    String response = new String(returnMessage.getMessageContents());
                    sendResponse(exchange, 200, response);
                    logger.info("Respuesta de: " + returnMessage.getSenderPort());
                } else {
                    sendResponse(exchange, 500, "Failed to get response from leader");
                }

            } catch (Exception e) {
                logger.severe("Error processing request: " + e.getMessage());
                sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
    }

    private void electNewLeaderAndFinish(String host, Message message, HttpExchange exchange) throws IOException, InterruptedException {
        logger.info("EL LIDER MURIO, AHY QUE HVER QUE ACABA DE PASAR. RIBBBBBBBBBER");
        waitForElection.await();
        logger.info("Nuevo Lider ESSSS: " + servidor.getCurrentLeader().getProposedLeaderID());
        leaderIsDead = false;

        InetSocketAddress leaderAddr = getLeaderAddress();
        int tcpPort = leaderAddr.getPort() + 2;

        message = new Message(message.getMessageType(), message.getMessageContents(), message.getSenderHost(), message.getSenderPort(), leaderAddr.getHostString(), tcpPort, message.getRequestID());

        Message returnMessage = null;
        try (Socket clientSocket = new Socket(host, tcpPort);
             DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
             DataInputStream ois = new DataInputStream(clientSocket.getInputStream())) {

            byte[] messageBytes = message.getNetworkPayload();
            os.writeInt(messageBytes.length);
            os.write(messageBytes);

            while (ois.available() == 0) {
                if (leaderIsDead) {
                    electNewLeaderAndFinish(host, message, exchange);
                    return;
                }
                Thread.sleep(500);
            }

            if (leaderIsDead) {
                electNewLeaderAndFinish(host, message, exchange);
                return;
            }

            int length = ois.readInt();
            byte[] responseBytes = new byte[length];
            ois.readFully(responseBytes, 0, responseBytes.length);
            returnMessage = new Message(responseBytes);
            String response = new String(returnMessage.getMessageContents());
            logger.info("OFICIAL: new leader.");
            sendResponse(exchange, 200, response);
            logger.info("Respuesta de: " + returnMessage.getSenderPort());
        } catch (IOException ex) {
            logger.severe("Raro error with new leader: " + ex.getMessage());
            throw ex;
        }
    }

    private void sendResponse(HttpExchange exchange, int rCode, String body) throws IOException {
        byte[] response = body.getBytes();
        exchange.sendResponseHeaders(rCode, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    public void reportLeaderIsDead() {
        logger.warning("Leader marked as dead");
        this.leaderIsDead = true;
    }

    public synchronized void receiveLatch(CountDownLatch latch) {
        this.waitForElection = latch;
        logger.info("Nuevo latch recivido");
    }

    public synchronized InetSocketAddress getAddress() {
        return servidor.getAddress();
    }

    public synchronized Vote getLeader() {
        return servidor.getCurrentLeader();
    }

    public synchronized InetSocketAddress getLeaderAddress() {
        return servidor.getPeerByID(getLeader().getProposedLeaderID());
    }

    private class EleccionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (servidor != null && servidor.getCurrentLeader() != null) {
                String response = servidor.getEstados().toString();
                t.sendResponseHeaders(200, response.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                String response = "Mal request";
                t.sendResponseHeaders(204, response.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    public void start() {
        electionService.start();
        servidor.start();
        httpServidor.start();
        logger.info("Gateway server started on HTTP port " + httpServidor.getAddress().getPort());
    }

    public void stop() {
        if (servidor != null) servidor.shutdown();
        if (httpServidor != null) httpServidor.stop(3);
        if (electionService != null) electionService.stop(3);
        logger.info("Gateway server stopped");
    }
}