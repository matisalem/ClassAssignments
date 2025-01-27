package edu.yu.cs.com3800.stage4;

import edu.yu.cs.com3800.*;
import com.sun.net.httpserver.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.Map;

public class GatewayServer implements LoggingServer {

    private HttpServer httpServidor;
    private GatewayPeerServerImpl servidor;
    private Map<Integer, String> cache;
    private Logger logger;

    public GatewayServer(int httpPort, int peerPort, long peerEpoch, Long serverID, ConcurrentHashMap<Long, InetSocketAddress> peerIDtoAddress,int numberOfObservers) throws IOException{
        try {
            this.httpServidor = HttpServer.create(new InetSocketAddress(httpPort), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.cache = new ConcurrentHashMap<>();
        loadLoggers();
        this.servidor = new GatewayPeerServerImpl(peerPort, peerEpoch, serverID, peerIDtoAddress);
        this.httpServidor.createContext("/compileandrun", new CosoHandler());
        this.httpServidor.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3));
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



    private class CosoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            try {
                if (!exchange.getRequestMethod().equals("POST")) {
                    sendResponse(exchange, 405, "No es post");
                    return;
                }

                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                if (!"text/x-java-source".equals(contentType)) {
                    sendResponse(exchange, 400, "Contenido malo");
                    return;
                }

                InputStream is = exchange.getRequestBody();
                String code = new String(is.readAllBytes());
                int codeHash = code.hashCode();

                String cached = cache.get(codeHash);
                if (cached != null) {
                    exchange.getResponseHeaders().add("Cached-Response", "true");
                    sendResponse(exchange, 200, cached);
                    return;
                }

                InetSocketAddress lider = servidor.getCurrentLeaderAddress();
                if (lider == null) {
                    sendResponse(exchange, 500, "No hay lider");
      //              logger.info("WWWWWWW no hay liderrrr");
                    return;
                }

                int tcpPort = lider.getPort() + 5;
                String respuesta = sendRequestToLeader(code, tcpPort);

                cache.put(codeHash, respuesta);
                exchange.getResponseHeaders().add("Cached-Response", "false");
                sendResponse(exchange, 200, respuesta);
                logger.info("Respuesta mandada a Client");

            } catch (Exception e) {
                logger.info("Error GRAVE: " + e.getMessage());
                sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
    }

    private String sendRequestToLeader(String code, int tcpPort) throws IOException {
        try (Socket socket = new Socket("localhost", tcpPort);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            Message workMessage = new Message(Message.MessageType.WORK, code.getBytes(), servidor.getAddress().getHostString(), servidor.getAddress().getPort(), servidor.getCurrentLeaderAddress().getHostString(), tcpPort);

            byte[] messageBytes = workMessage.getNetworkPayload();
            out.writeInt(messageBytes.length);
            out.write(messageBytes);

            while (in.available() == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException();
                }
            }

            int length = in.readInt();
            byte[] bytes = new byte[length];
            in.readFully(bytes);
            Message respuesta = new Message(bytes);

            return new String(respuesta.getMessageContents());
        }
    }

    private void sendResponse(HttpExchange exchange, int rCode, String body) throws IOException {
        byte[] respuesta = body.getBytes();
        exchange.sendResponseHeaders(rCode, respuesta.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(respuesta);
        }
    }

    public synchronized InetSocketAddress getAddress() {
        return servidor.getAddress();
    }

    public synchronized Long getLeader() {
        Vote currentLeader = servidor.getCurrentLeader();
        return currentLeader.getProposedLeaderID();
    }

    public void start() {
        servidor.start();
        httpServidor.start();
        logger.info("Gateway arrancad");
    }

    public void stop() {
        if (servidor != null) servidor.shutdown();
        if (httpServidor != null) httpServidor.stop(3);
        logger.info("Gateway parado");
    }
}
