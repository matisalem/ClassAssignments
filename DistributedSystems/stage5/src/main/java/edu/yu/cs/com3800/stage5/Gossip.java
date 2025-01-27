package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class Gossip extends Thread {
    private final LinkedBlockingQueue<Message> outgoingMessages;
    private final LinkedBlockingQueue<Message> incomingMessages;
    private final long myId;
    private final List<Long> servers;
    private final HashMap<InetSocketAddress, Long> addressToPeerID;
    private final PeerServerImpl myPeerServer;
    private Logger summaryLogger;
    private Logger verboseLogger;
    private final Map<Long, ServerState> serverStates;
    private final Map<Long, Long> failureTimestamps;
    private long lastHeartbeatTime;
    private volatile boolean isShutdown;
    private CountDownLatch waitForElections;
    private ConcurrentHashMap<Long,Long> heartbeat = new ConcurrentHashMap<>();
    private HttpServer verboseServer;
    private HttpServer summaryServer;
    private File verboseFile;
    private File summaryFile;
    private Logger logger;
    private Long startTime;
    List<Long> deadList = new ArrayList<>();
    static final int GOSSIP = 2500;
    static final int FAIL = GOSSIP * 10;
    static final int CLEANUP = FAIL * 2;

    private Long gatewayId;

    private class ServerState {
        long heartbeatSequence;
        long lastUpdateTime;

        ServerState() {
            this.heartbeatSequence = 0;
            this.lastUpdateTime = currentTime();
        }
    }

    public Gossip(long myId, Set<Long> servers, LinkedBlockingQueue<Message> outgoingMessages, LinkedBlockingQueue<Message> incomingMessages, HashMap<InetSocketAddress, Long> addressToPeerID, PeerServerImpl myPeerServer, int verbosePort, int summaryPort, Long gatewayId) {
        setDaemon(true);
        this.myId = myId;
        this.servers = new ArrayList<>(new HashSet<>(servers));
        this.servers.add(myId);
        Collections.sort(this.servers);
        this.outgoingMessages = outgoingMessages;
        this.incomingMessages = incomingMessages;
        this.addressToPeerID = addressToPeerID;
        this.myPeerServer = myPeerServer;
        this.startTime = System.currentTimeMillis();
        this.serverStates = new ConcurrentHashMap<>();
        this.failureTimestamps = new ConcurrentHashMap<>();
        this.lastHeartbeatTime = currentTime();
        this.isShutdown = false;
        this.gatewayId = gatewayId;

        for (Long serverId : this.servers) {
            if (!serverId.equals(myId)) serverStates.put(serverId, new ServerState());
            heartbeat.put(serverId, 0L);
        }

        loadLoggers();
        loadVerbose();
        loadSummary();
        setupHttpServers(verbosePort, summaryPort);
    }

    private void loadLoggers() {
        logger = Logger.getLogger("gossipLogger"+myId);
        File path = new File("logs/Gossip");
        path.mkdir();
        try {
            FileHandler fh = new FileHandler("logs/Gossip/Gossip_" + myId);
            logger.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());

        } catch (IOException e) {
            System.err.println("Failed to initialize loggers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void loadVerbose() {
        verboseLogger = Logger.getLogger("verboseLogger"+myId);
        File path = new File("logs/verbose");
        path.mkdir();
        try {
            FileHandler fh = new FileHandler("logs/verbose/verbose_" + myId);
            logger.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadSummary() {
        verboseLogger = Logger.getLogger("summaryLogger");
        File path = new File("logs/summary");
        path.mkdir();
        try {
            FileHandler fh = new FileHandler("logs/summary/summary_" + myId);
            logger.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());

        } catch (IOException e) {
            System.err.println("Failed to initialize loggers: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        logger.info("Gossip thread started for server " + myId + "\n");
        this.heartbeat.put(myId, System.currentTimeMillis() - startTime);
        while (!isShutdown && !isInterrupted()) {
            try {
                if (waitForElections != null) {
                    handleElections();
                    continue;
                }

                long currentTime = currentTime();
                logger.fine("Server " + myId + " processing incoming messages.");

                processIncomingMessages();
                detectError(currentTime);
                cleanupFailures(currentTime);

                currentTime = currentTime();
                if (currentTime - lastHeartbeatTime >= GOSSIP) {
                    logger.fine("Server " + myId + " sending gossip heartbeat.");
                    if(gatewayId != myId) gossipToRandomPeer();
                    lastHeartbeatTime = currentTime;
                }

                Thread.sleep(GOSSIP / 2);
            } catch (InterruptedException e) {
                logger.severe("Gossip thread crashed for server " + myId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        logger.info("Gossip thread stopped for server " + myId);
        cleanup();
    }

    private void cleanupFailures(long currentTime) {
        List<Long> serversToRemove = new ArrayList<>();

        for(Map.Entry<Long, Long> entry : failureTimestamps.entrySet()) {
            long failedId = entry.getKey();
            long failureTime = entry.getValue();

            if(currentTime - failureTime >= CLEANUP) {
                serversToRemove.add(failedId);
                logger.info("Server " + failedId + " cleaned up after " + CLEANUP + "ms");
            }
        }

        for(Long serverId : serversToRemove) {
            failureTimestamps.remove(serverId);
            heartbeat.remove(serverId);
            deadList.remove(serverId);
        }
    }

    private void processIncomingMessages() {
        while (!incomingMessages.isEmpty()) {
            try {
                Message message = incomingMessages.take();
                InetSocketAddress senderAddress = new InetSocketAddress(message.getSenderHost(), message.getSenderPort());
                Long senderId = addressToPeerID.get(senderAddress);

                if (failureTimestamps.containsKey(senderId)) {
                     logger.warning("Este servidor fallo y mando un mensaje:: " + senderId);
                    continue;
                }

                if (message.getMessageType() == Message.MessageType.GOSSIP) {
                    logger.fine("Processing gossip from server " + senderId);

                    try {
                        HashMap<Long, Long> receivedMap = parseHeartbeatMap(new String(message.getMessageContents()).trim());
                        mergeHeartbeatMaps(receivedMap);

                        logger.info("Server on ID: " + myPeerServer.getUdpPort() + " Updated Heartbeat Map: " + heartbeat + "\nReceived heartbeat from: " + message.getSenderPort() + " Map: " + receivedMap + "\n");
                    } catch (Exception e) {
                        logger.severe("Failed to process gossip message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }



    private HashMap<Long, Long> parseHeartbeatMap(String mapString) {
        HashMap<Long, Long> resultMap = new HashMap<>();

        if (mapString.startsWith("{") && mapString.endsWith("}")) mapString = mapString.substring(1, mapString.length() - 1);

        String[] pairs = mapString.split(",\\s*");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                Long key = Long.parseLong(keyValue[0].trim());
                Long value = Long.parseLong(keyValue[1].trim());
                resultMap.put(key, value);
            }
        }

        return resultMap;
    }


    private synchronized void mergeHeartbeatMaps(HashMap<Long, Long> receivedMap) {
        for (var i : receivedMap.entrySet()) {
            Long serverId = i.getKey();
            Long receivedHeartbeatTime = i.getValue();
            if(deadList.contains(serverId))continue;

            heartbeat.merge(serverId, receivedHeartbeatTime, Math::max);
            serverStates.computeIfAbsent(serverId, k -> new ServerState()).lastUpdateTime = currentTime();
        }

        logger.fine(String.format("Server %d merged heartbeat map. Updated heartbeat: %s", myId, heartbeat));
    }



    private synchronized void detectError(long currentTime) {
        Iterator<Map.Entry<Long, Long>> iterator = heartbeat.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Long> entry = iterator.next();
            Long serverId = entry.getKey();
            Long state = entry.getValue();

            if (failureTimestamps.containsKey(serverId) || myPeerServer.isPeerDead(serverId) || serverId == gatewayId) continue;

     //       long  staticWait = myId == gatewayId? 10000l: 0L; //do not remove :)
            boolean timeSinceUpdate = currentTime - startTime > state + FAIL;

            if (timeSinceUpdate) {
                logger.info("Server " + myId + " detected server " + serverId+" has failed");

                failureTimestamps.put(serverId, currentTime);
                servers.remove(serverId);
                myPeerServer.reportFailedPeer(serverId);
                heartbeat.remove(serverId);
                deadList.add(serverId);
                iterator.remove();

                if (myPeerServer.getCurrentLeader() != null &&serverId == myPeerServer.getCurrentLeader().getProposedLeaderID()) handleLeaderFailure();
            }
        }
    }

    private void handleLeaderFailure() {

        logger.warning("LLEGO A HANDLE LEADER FAILURE");
        try {
            myPeerServer.setCurrentLeader(null);
            heartbeat.clear();
            failureTimestamps.clear();
            deadList.clear();
            servers.clear();
            servers.addAll(myPeerServer.getPeerIDtoAddress().keySet());
            servers.remove(gatewayId);
        } catch (IOException e) {
            logger.severe("Error handling leader failure: " + e);
        }
    }


    private void gossipToRandomPeer() {

        List<Long> availablePeers = new ArrayList<>(servers);
        availablePeers.removeAll(failureTimestamps.keySet());
        availablePeers.remove(myId);
        availablePeers = new ArrayList<>(new HashSet<>(availablePeers));

        logger.info("Server " + myId + ": Available peers for gossip: " + availablePeers + ", Failed servers: " + failureTimestamps.keySet());

        if (!availablePeers.isEmpty()) {
            Long randomPeerId = availablePeers.get(new Random().nextInt(availablePeers.size()));
            InetSocketAddress target = myPeerServer.getPeerByID(randomPeerId);
            if (target != null) {
                byte[] gossipMessage = createGossipMessage();
                myPeerServer.sendMessage(Message.MessageType.GOSSIP, gossipMessage, target);
            } else {
                logger.warning("Could not find address for peer " + randomPeerId);
            }
        } else {
            logger.warning("Server " + myId + " has no available peers to gossip with.");
        }
    }


    private byte[] createGossipMessage() {
        heartbeat.put(myId, (System.currentTimeMillis() - startTime));
        return heartbeat.toString().getBytes();
    }

    private void handleElections() throws InterruptedException {
        if (waitForElections == null) return;

        waitForElections.await();

        long postElectionTime = currentTime();
        for (var i : serverStates.values()) {
            i.lastUpdateTime = postElectionTime;
        }

        failureTimestamps.clear();
        waitForElections = null;
    }


    private void setupHttpServers(int verbosePort, int summaryPort) {
        try {
            summaryServer = HttpServer.create(new InetSocketAddress(summaryPort), 0);
            summaryServer.createContext("/summaryFile", new SummaryHandler());
            summaryServer.setExecutor(null);

            verboseServer = HttpServer.create(new InetSocketAddress(verbosePort), 0);
            verboseServer.createContext("/verboseFile", new VerboseHandler());
            verboseServer.setExecutor(null);

            summaryServer.start();
            verboseServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }

    public void pauseForElections(CountDownLatch latch) {
        this.waitForElections = latch;
    }

    private void cleanup() {
        if (summaryServer != null) summaryServer.stop(0);
        if (verboseServer != null) verboseServer.stop(0);
    }

    public void shutdown() {
        isShutdown = true;
        serverStates.clear();
        servers.clear();
        failureTimestamps.clear();
        interrupt();
    }

    class VerboseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = verboseFile.getAbsolutePath();
            t.sendResponseHeaders(200, response.length());
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    class SummaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = summaryFile.getAbsolutePath();
            t.sendResponseHeaders(200, response.length());
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}