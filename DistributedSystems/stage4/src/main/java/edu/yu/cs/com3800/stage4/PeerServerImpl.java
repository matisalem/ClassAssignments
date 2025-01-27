package edu.yu.cs.com3800.stage4;

import edu.yu.cs.com3800.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.File;

public class PeerServerImpl extends Thread implements PeerServer {
    private final InetSocketAddress myAddress;
    private final int myPort;
    private ServerState state;
    private volatile boolean shutdown;
    private LinkedBlockingQueue<Message> outgoingMessages;
    private LinkedBlockingQueue<Message> incomingMessages;
    private Long id;
    private long peerEpoch;
    private volatile Vote currentLeader;
    private Map<Long, InetSocketAddress> peerIDtoAddress;

    private Long gatewayID;
    private int numberOfObservers;

    private UDPMessageSender senderWorker;
    private UDPMessageReceiver receiverWorker;
    private Logger logger;
    private Logger loggerElection;

    private JavaRunnerFollower follower;
    private RoundRobinLeader leader;

    public PeerServerImpl(int udpPort, long peerEpoch, Long serverID, Map<Long, InetSocketAddress> peerIDtoAddress, Long gatewayID, int numberOfObservers) throws IOException{
    this.myPort = udpPort;
        this.peerEpoch = peerEpoch;
        this.id = serverID;
        this.peerIDtoAddress = peerIDtoAddress;
        this.gatewayID = gatewayID;
        this.numberOfObservers = numberOfObservers;

        myAddress = new InetSocketAddress("localhost", myPort);

        if (id == gatewayID) state = ServerState.OBSERVER;
        else state = ServerState.LOOKING;

        incomingMessages = new LinkedBlockingQueue<>();
        outgoingMessages = new LinkedBlockingQueue<>();
        shutdown = false;

        loadLoggers();
        try {
            setCurrentLeader(new Vote(this.id, this.peerEpoch));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        follower = null;
        leader = null;
    }

    private void loadLoggers(){
        logger = Logger.getLogger("peerServerLogger");
        File path = new File("logs");
        path.mkdir();
        FileHandler fh = null;
        try {
            fh = new FileHandler("logs/servidor-" + getServerId() + ".log");
        } catch (IOException e) {}
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        loggerElection = Logger.getLogger("leaderElectionLogger");
        File path2 = new File("logs/logsElection");
        path2.mkdir();
        FileHandler fh2 = null;
        try {
            fh2 = new FileHandler("logs/logsElection/LeaderElectionLogger" + getServerId() + ".log");
        } catch (IOException e) {}
        loggerElection.addHandler(fh2);
        SimpleFormatter formatter2 = new SimpleFormatter();
        fh2.setFormatter(formatter2);
    }


    @Override
    public void shutdown() {
        this.shutdown = true;
        shutdownThreads();
    }


//    public long getGatewayID() {
//        return this.gatewayID;
//    }

    @Override
    public void setCurrentLeader(Vote v) throws IOException {
        this.currentLeader = v;
        this.peerEpoch = v.getPeerEpoch();
        logger.info("for server: " + id + ", leader: " + v.getProposedLeaderID());
    }

    @Override
    public Vote getCurrentLeader() {
        return currentLeader;
    }

    @Override
    public void sendMessage(Message.MessageType type, byte[] messageContents, InetSocketAddress target) throws IllegalArgumentException {
        Message message = new Message(type, messageContents, myAddress.getHostString(), myPort, target.getHostString(), target.getPort());
        outgoingMessages.offer(message);
    }

    @Override
    public void sendBroadcast(Message.MessageType type, byte[] messageContents) {
        for (InetSocketAddress address : peerIDtoAddress.values()) {
            sendMessage(type, messageContents, address);
        }
    }

    @Override
    public ServerState getPeerState() {
        return state;
    }

    @Override
    public void setPeerState(ServerState nuevo) {
        ServerState old = getPeerState();
        if (old == nuevo) return;

        if (old == ServerState.FOLLOWING) {
            if (follower != null) {
                follower.shutdown();
                follower = null;
            }
        } else if (old == ServerState.LEADING) {
            if (leader != null) {
                leader.shutdown();
                leader = null;
            }
        }

        state = nuevo;
        logger.info("Servidor " + id + " era: " + old + ". Ahora: " + nuevo);

        if (nuevo == ServerState.FOLLOWING && follower == null) {
            follower = new JavaRunnerFollower(this, getPeerByID(getCurrentLeader().getProposedLeaderID()), createJRFLogger());
            follower.start();
        } else if (nuevo == ServerState.LEADING && leader == null) {
            List<Long> workers = new ArrayList<>(peerIDtoAddress.keySet());
            workers.remove(gatewayID);
            workers.remove(id);
            leader = new RoundRobinLeader(this, getUdpPort(), peerIDtoAddress, workers, createRRLLogger());
            leader.start();
        }
    }

    @Override
    public Long getServerId() {
        return id;
    }

    @Override
    public long getPeerEpoch() {
        return peerEpoch;
    }

    @Override
    public InetSocketAddress getAddress() {
        return myAddress;
    }

    @Override
    public int getUdpPort() {
        return myPort;
    }

    @Override
    public InetSocketAddress getPeerByID(long peerId) {
        return peerIDtoAddress.get(peerId);
    }

    @Override
    public int getQuorumSize() {
        return ((peerIDtoAddress.size() - numberOfObservers) / 2) + 1;
    }

    private Logger createRRLLogger(){
        Logger l = Logger.getLogger("RoundRobinLeaderLogger");
        File path = new File("logs/logsRRL");
        path.mkdir();
        FileHandler fh = null;
        try {
            fh = new FileHandler("logs/logsRRL/server-" + getServerId() + ".log");
        } catch (IOException e) {}
        l.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        return l;
    }

    private Logger createJRFLogger(){
        Logger l = Logger.getLogger("javaRunnerFollowerLogger");
        File path = new File("logs/logsJRF");
        path.mkdir();
        FileHandler fh = null;
        try {
            fh = new FileHandler("logs/logsJRF/servidor-" + getServerId() + ".log");
        } catch (IOException e) {}
        l.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        return l;
    }

    @Override
    public void run() {
        // Start UDP threads
        this.senderWorker = new UDPMessageSender(outgoingMessages, myPort);
        try {
            this.receiverWorker = new UDPMessageReceiver(incomingMessages, myAddress, myPort, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.senderWorker.start();
        this.receiverWorker.start();

        try {
            while (!shutdown) {
                switch (getPeerState()) {
                    case LOOKING:
                        logger.info("Server " + id + " is LOOKING for a leader");
                        LeaderElection election = new LeaderElection(this, incomingMessages, loggerElection);
                        Vote result = election.lookForLeader();
                        if (result == null) continue;
                        setCurrentLeader(result);
                        if (result.getProposedLeaderID() == this.id) {
                            setPeerState(ServerState.LEADING);
                            logger.info("Server " + id + " is now LEADING");
                        } else {
                            setPeerState(ServerState.FOLLOWING);
                            logger.info("Server " + id + " is now FOLLOWING");
                        }
                        break;
                    case LEADING:
                        if (leader == null) {
                            List<Long> workers = new ArrayList<>(peerIDtoAddress.keySet());
                            workers.remove(gatewayID);
                            workers.remove(id);
                            this.leader = new RoundRobinLeader(this, getUdpPort(), peerIDtoAddress, workers, createRRLLogger());
                            leader.start();
                        }
                        break;

                    case FOLLOWING:
                        if (follower == null) {
                            follower = new JavaRunnerFollower(this, getPeerByID(getCurrentLeader().getProposedLeaderID()), createJRFLogger());
                            follower.start();
                        }
                        break;

                    case OBSERVER:
                        LeaderElection election1 = new LeaderElection(this, incomingMessages, loggerElection);
                            setCurrentLeader(election1.lookForLeader());
                            logger.info("I am observer with ID: " + id + ", and my leader is: " + getCurrentLeader().getProposedLeaderID());
                        break;
                }
            }
            shutdownThreads();
            if(leader != null) leader.shutdown();
            if(follower != null)follower.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shutdownThreads() {
        this.senderWorker.shutdown();
        this.receiverWorker.shutdown();
    }
}