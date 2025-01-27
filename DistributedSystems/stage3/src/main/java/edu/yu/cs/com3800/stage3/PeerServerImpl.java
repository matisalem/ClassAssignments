package edu.yu.cs.com3800.stage3;

import edu.yu.cs.com3800.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


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
    private Map<Long,InetSocketAddress> peerIDtoAddress;

    private UDPMessageSender senderWorker;
    private UDPMessageReceiver receiverWorker;
    private Logger logger;
    private Logger loggerElection;

    private JavaRunnerFollower follower;
    private RoundRobinLeader leader;

    public PeerServerImpl(int myPort, long peerEpoch, Long id, Map<Long,InetSocketAddress> peerIDtoAddress){
        this.myPort = myPort;
        this.peerEpoch = peerEpoch;
        this.id = id;
        this.peerIDtoAddress = peerIDtoAddress;
        myAddress = new InetSocketAddress("localhost", myPort);
        state = ServerState.LOOKING;
        incomingMessages = new LinkedBlockingQueue<>();
        outgoingMessages = new LinkedBlockingQueue<>();
        shutdown = false;
        loadLoggers();
        setPeerState(ServerState.LOOKING);
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

    @Override
    public void setCurrentLeader(Vote v) throws IOException {
        this.currentLeader = v;
        this.peerEpoch = v.getPeerEpoch();
    }

    @Override
    public Vote getCurrentLeader() {
        return currentLeader;
    }

    @Override
    public void sendMessage(Message.MessageType type, byte[] messageContents, InetSocketAddress target) throws IllegalArgumentException {
        Message message = new Message(type, messageContents, myAddress.getHostString(), myPort, target.getHostString(), target.getPort(), id);
        outgoingMessages.offer(message);
    }

    @Override
    public void sendBroadcast(Message.MessageType type, byte[] messageContents) {
        for (var i : peerIDtoAddress.keySet()){
            sendMessage(type, messageContents, peerIDtoAddress.get(i));
        }
    }

    @Override
    public ServerState getPeerState() {
        return state;
    }

    @Override
    public void setPeerState(ServerState newState) {
        state = newState;
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
        int tamano = peerIDtoAddress.size() / 2;
        return tamano + 1;
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

        this.senderWorker = new UDPMessageSender(outgoingMessages, myPort);
        try {
            this.receiverWorker = new UDPMessageReceiver(incomingMessages, myAddress, myPort, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.senderWorker.start();
        this.receiverWorker.start();

        try{
            while (!shutdown) {
                switch (getPeerState()) {
                    case LOOKING:
                        logger.info("Servidor " + id + " esta en LOOKING");
                        LeaderElection eleccion = new LeaderElection(this, incomingMessages, loggerElection);
                        Vote result = eleccion.lookForLeader();
                        if (result == null) continue;
                        setCurrentLeader(result);
                        if (result.getProposedLeaderID() == this.id) {
                            setPeerState(ServerState.LEADING);
                            logger.info("Servidor " + id + " es de BOCA");
                        } else {
                            setPeerState(ServerState.FOLLOWING);
                            logger.info("Servidor " + id + " es de RiBER");
                        }
                        break;
                    case LEADING:
                        if (leader == null) {
                            leader = new RoundRobinLeader(this, peerIDtoAddress, incomingMessages, outgoingMessages, createRRLLogger());
                            leader.start();
                        }
                        break;
                    case FOLLOWING:
                        if (follower == null) {
                            follower = new JavaRunnerFollower(this, incomingMessages, outgoingMessages, getPeerByID(getCurrentLeader().getProposedLeaderID()), createJRFLogger());
                            follower.start();
                        }
                        break;
                }
            }
            shutdownThreads();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shutdownThreads() {
        this.senderWorker.shutdown();
        this.receiverWorker.shutdown();
    }

}
