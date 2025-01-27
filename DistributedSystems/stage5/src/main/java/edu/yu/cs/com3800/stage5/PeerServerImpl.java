// claude PeerrServerImpl

package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
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
    RoundRobinLeader leader;

    private Gossip gossiper;
    private LeaderElection election;
    private CountDownLatch waitForElection;
    private CountDownLatch lineUpForElection;
    private HashMap<Long, InetSocketAddress> failedServers;
    private HashMap<InetSocketAddress, Long> addressToPeerID;
    private LinkedBlockingQueue<Message> gossipMessages;
    private boolean electionNeeded = true;

    public PeerServerImpl(int udpPort, long peerEpoch, Long serverID, Map<Long, InetSocketAddress> peerIDtoAddress, Long gatewayID, int numberOfObservers) throws IOException {
        this.myPort = udpPort;
        this.peerEpoch = peerEpoch;
        this.id = serverID;
        this.peerIDtoAddress = peerIDtoAddress;
        this.gatewayID = gatewayID;
        this.numberOfObservers = numberOfObservers;
        this.myAddress = new InetSocketAddress("localhost", myPort);

        loadLoggers();

        this.incomingMessages = new LinkedBlockingQueue<>();
        this.outgoingMessages = new LinkedBlockingQueue<>();
        this.gossipMessages = new LinkedBlockingQueue<>();
        this.failedServers = new HashMap<>();
        this.addressToPeerID = new HashMap<>();
        this.waitForElection = new CountDownLatch(1);

        setCurrentLeader(new Vote(this.id, this.peerEpoch));

        if (id == gatewayID) state = ServerState.OBSERVER;
        else state = ServerState.LOOKING;


        for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
            addressToPeerID.put(entry.getValue(), entry.getKey());
        }

        if (currentLeader == null) {
            System.out.println("IMPRESO: " + serverID);
            currentLeader = new Vote(this.id, this.peerEpoch);
        }

        this.election = new LeaderElection(this, incomingMessages, loggerElection);
    }

    public synchronized Map<Long, InetSocketAddress> getPeerIDtoAddress() {
        return peerIDtoAddress;
    }

    @Override
    public void shutdown(){
        this.shutdown = true;
        if (this.senderWorker != null ) this.senderWorker.shutdown();
        if (this.receiverWorker != null) this.receiverWorker.shutdown();
        if (gossiper != null) gossiper.shutdown();
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

    public Long getCurrentLeaderAddress() {
        return currentLeader.getProposedLeaderID();
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
    public void setPeerState(ServerState newState) {
        this.state = newState;
    }


    @Override
    public void reportFailedPeer(long peerID){

        if (failedServers.containsKey(peerID)) return;

        failedServers.put(peerID, getPeerByID(peerID));
        logger.warning("Server " + getServerId() + " detected failed peer: " + peerID);

        peerIDtoAddress.remove(peerID);
        if (getPeerState() != ServerState.OBSERVER) {
            if (peerID != getCurrentLeader().getProposedLeaderID()) {

                logger.warning("Server " + getServerId() + " detected leader failure, starting new election");

                if (!(this.leader == null)) leader.reportFailedWorker(peerID);
            } else {
                this.peerEpoch++;
                waitForElection = new CountDownLatch(1);
                try {
                    setCurrentLeader(new Vote(getServerId(), peerEpoch));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setPeerState(ServerState.LOOKING);
                follower.esprandoLider(waitForElection);
            }
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

    @Override
    public boolean isPeerDead(long peerID){
        return failedServers.containsKey(peerID);
    }

    @Override
    public boolean isPeerDead(InetSocketAddress address){
        return failedServers.containsValue(address);
    }

    @Override
    public void run(){
        // Start UDP threads
        this.senderWorker = new UDPMessageSender(outgoingMessages, myPort);
        try {
            this.receiverWorker = new UDPMessageReceiver(incomingMessages, myAddress, myPort, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.senderWorker.start();
        this.receiverWorker.start();

        try{
            while (!this.shutdown){

                switch (this.getPeerState()){
                    case LOOKING:


                        if(gossiper != null){
                            gossiper.pauseForElections(waitForElection);
                            sleep(5000);
                        }
                        Vote result = election.lookForLeader();
                        sleep(3000);
                        currentLeader = result;

//                        if (result.getProposedLeaderID() == this.id) {
//                       //     setPeerState(ServerState.LEADING);
//                            logger.info("Server " + id + " is now LEADING");
//                        } else {
//                     //       setPeerState(ServerState.FOLLOWING);
//                            logger.info("Server " + id + " is now FOLLOWING");
//                        }

                        incomingMessages.clear();
                        waitForElection.countDown();
                        if(gossiper == null) {
                            this.gossiper = new Gossip(id, peerIDtoAddress.keySet(), outgoingMessages, incomingMessages, addressToPeerID, this, getUdpPort()-1000, getUdpPort()-2000, 0L);
                            this.gossiper.start();
                        }

                        break;
                    case FOLLOWING:
                        if (follower == null) {
                            follower = new JavaRunnerFollower(this, getPeerByID(getCurrentLeader().getProposedLeaderID()), createJRFLogger());
                            follower.start();
                        }
                        break;
                    case LEADING:

                        if (leader == null) {
                            List<Long> workers = new ArrayList<>(peerIDtoAddress.keySet());
                            workers.remove(gatewayID);
                            workers.remove(id);

                            if (follower == null) {
                                leader = new RoundRobinLeader(this, getUdpPort(),peerIDtoAddress, workers, new CountDownLatch(0), createRRLLogger());
                                leader.start();
                            } else {
                                HashMap<Long, Message> oldWork = follower.getViejo();
                                follower.closePortToBecomeLeader();
                                follower.shutdown();
                                follower = null;
                                leader = new RoundRobinLeader(this, getUdpPort(),peerIDtoAddress, workers, new CountDownLatch(1), oldWork, createRRLLogger());
                                leader.start();
                            }
                        }
                        break;
                    case OBSERVER:
                        LeaderElection election1 = new LeaderElection(this, incomingMessages, loggerElection);
                        try {
                            setCurrentLeader(election1.lookForLeader());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        logger.info("I am observer with ID: " + id + ", and my leader is: " + getCurrentLeader().getProposedLeaderID());
                        break;

                }
            }
            shutdownThreads();
            if(leader != null) leader.shutdown();
            if(follower != null)follower.shutdown();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void shutdownThreads() {
        if (senderWorker != null) senderWorker.shutdown();
        if (receiverWorker != null) receiverWorker.shutdown();
        if (gossiper != null) gossiper.shutdown();
    }

    private void loadLoggers() {
        logger = Logger.getLogger("peerServerLogger-" + myPort);
        loggerElection = Logger.getLogger("leaderElectionLogger-" + myPort);

        File path = new File("logs");
        path.mkdir();

        try {
            FileHandler fh = new FileHandler("logs/servidor-" + myPort + ".log");
            logger.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());

            File electionPath = new File("logs/logsElection");
            electionPath.mkdir();
            FileHandler fh2 = new FileHandler("logs/logsElection/LeaderElectionLogger" + myPort + ".log");
            loggerElection.addHandler(fh2);
            fh2.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            System.err.println("Failed to initialize loggers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Logger createRRLLogger() {
        Logger l = Logger.getLogger("RoundRobinLeaderLogger");
        File path = new File("logs/logsRRL");
        path.mkdir();
        FileHandler fh = null;
        try {
            fh = new FileHandler("logs/logsRRL/server-" + getServerId() + ".log");
            l.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            logger.severe("Failed to create RRL logger: " + e.getMessage());
        }
        return l;
    }

    private Logger createJRFLogger() {
        Logger l = Logger.getLogger("javaRunnerFollowerLogger");
        File path = new File("logs/logsJRF");
        path.mkdir();
        FileHandler fh = null;
        try {
            fh = new FileHandler("logs/logsJRF/servidor-" + getServerId() + ".log");
            l.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            logger.severe("Failed to create JRF logger: " + e.getMessage());
        }
        return l;
    }
}
