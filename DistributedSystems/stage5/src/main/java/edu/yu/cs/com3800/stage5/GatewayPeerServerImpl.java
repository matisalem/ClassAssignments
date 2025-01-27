package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class GatewayPeerServerImpl extends PeerServerImpl {
    private Logger logger;
    private volatile boolean shutdown;
    private CountDownLatch waitForElection;
    private GatewayServer gateway;
    private LinkedBlockingQueue<Message> outgoingMessages;
    private LinkedBlockingQueue<Message> incomingMessages;
    private Map<Long, InetSocketAddress> peerIDtoAddress;
    private Gossip gossiper;
    private List<Long> failedServers;
    private boolean electionNeeded;
    private HashMap<InetSocketAddress, Long> addressToPeerID;
    private LeaderElection eleccion;
    private long peerEpoch;
    private Logger loggerElection;

    public GatewayPeerServerImpl(int udpPort, long peerEpoch, Long id, Map<Long, InetSocketAddress> peerIDtoAddress, GatewayServer gateway) throws IOException {
        super(udpPort, peerEpoch, id, peerIDtoAddress, id, 1);
        init(peerIDtoAddress, gateway);
    }

    public GatewayPeerServerImpl(int udpPort, long peerEpoch, Long id, Map<Long, InetSocketAddress> peerIDtoAddress, int numberOfObservers) throws IOException {
        super(udpPort, peerEpoch, id, peerIDtoAddress, id, numberOfObservers);
        init(peerIDtoAddress, null);
    }

    private void init(Map<Long, InetSocketAddress> peerIDtoAddress, GatewayServer gateway) {
        loadLoggers();
        setPeerState(ServerState.OBSERVER);
        this.gateway = gateway;
        this.peerIDtoAddress = peerIDtoAddress;
        this.failedServers = new ArrayList<>();
        this.electionNeeded = true;
        this.addressToPeerID = new HashMap<>();
        this.incomingMessages = new LinkedBlockingQueue<>();
        this.outgoingMessages = new LinkedBlockingQueue<>();
        this.waitForElection = new CountDownLatch(1);

        try {
            setCurrentLeader(new Vote(getServerId(), this.peerEpoch));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
            addressToPeerID.put(entry.getValue(), entry.getKey());
        }

        loggerElection = Logger.getLogger("leaderElectionLogger");
        File path2 = new File("logs/logsElection");
        path2.mkdir();
        try {
            FileHandler fh2 = new FileHandler("logs/logsElection/LeaderElectionLogger" + this.getUdpPort() + ".log");
            loggerElection.addHandler(fh2);
            SimpleFormatter formatter2 = new SimpleFormatter();
            fh2.setFormatter(formatter2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.eleccion = new LeaderElection(this, incomingMessages, loggerElection);
        logger.info("Gateway peer server initialized with ID: " + getServerId());
        super.setPeerState(ServerState.OBSERVER);
    }

    private void loadLoggers(){
        logger = Logger.getLogger("GatewaypeerServerLogger");
        File path = new File("logs");
        path.mkdir();
        FileHandler fh = null;
        try {
            fh = new FileHandler("logs/GatewayPeerServer.log");
        } catch (IOException e) {}
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
    }

    @Override
    public void setPeerState(ServerState nuevo) {
        if (nuevo != ServerState.OBSERVER) logger.info("Observer state cannot be changed");
        super.setPeerState(ServerState.OBSERVER);
    }

    @Override
    public void setCurrentLeader(Vote v) throws IOException {
        if (v == null) {
            logger.warning("Lider es null");
            return;
        }
        if (v.getProposedLeaderID() == this.getServerId()) return;
        super.setCurrentLeader(v);
        InetSocketAddress leaderAddress = getPeerByID(v.getProposedLeaderID());
        if (logger != null && gateway != null && v.getProposedLeaderID() == 0) electionNeeded = true;
    }

    @Override
    public void reportFailedPeer(long peerID) {

        peerIDtoAddress.remove(peerID);
        failedServers.add(peerID);

        if (peerID == getCurrentLeader().getProposedLeaderID()) {
            electionNeeded = true;
            peerEpoch++;
            try {
                setCurrentLeader(new Vote(0, peerEpoch));
            } catch (IOException e) {
                e.printStackTrace();
            }
            waitForElection = new CountDownLatch(1);
            if (gateway != null) {
                gateway.receiveLatch(waitForElection);
                gateway.reportLeaderIsDead();
            }
        } else {
            if (this.leader != null) leader.reportFailedWorker(peerID);
        }
    }

    @Override
    public void sendMessage(Message.MessageType type, byte[] messageContents, InetSocketAddress target) throws IllegalArgumentException {
        Message message = new Message(type, messageContents, getAddress().getHostString(), getUdpPort(),
                target.getHostString(), target.getPort());
        outgoingMessages.offer(message);
    }

    @Override
    public boolean isPeerDead(long peerID) {
        return failedServers.contains(peerID);
    }

    @Override
    public boolean isPeerDead(InetSocketAddress address) {
        if (addressToPeerID.containsKey(address)) return false;
        return true;
    }

    public HashMap<Long, ServerState> getEstados() {
        HashMap<Long, ServerState> states = new HashMap<>();
        Vote lider = getCurrentLeader();
        if (lider != null) {
            for (var i : peerIDtoAddress.keySet()) {
                if (i == lider.getProposedLeaderID()) states.put(i, ServerState.LEADING);
                 else states.put(i, ServerState.FOLLOWING);
            }
        }
        return states;
    }

    @Override
    public void run(){
        try {
            UDPMessageSender sender = new UDPMessageSender(outgoingMessages, getUdpPort());
            UDPMessageReceiver receiver = new UDPMessageReceiver(incomingMessages, getAddress(), getUdpPort(), this);
            sender.start();
            receiver.start();

            while (!this.shutdown){

                if (getPeerState() == ServerState.OBSERVER && electionNeeded) {
                    logger.info("New eleccion");

                    if (gossiper != null)gossiper.pauseForElections(waitForElection);

                    setCurrentLeader(eleccion.lookForLeader());
                    if (this.getCurrentLeader() != null) {
                        sleep(7000);
                        waitForElection.countDown();
                        incomingMessages.clear();
                        electionNeeded = false;
                    }

                    if (gossiper == null) {
                        this.gossiper = new Gossip(getServerId(), this.peerIDtoAddress.keySet(), outgoingMessages, incomingMessages, addressToPeerID, this, getUdpPort()-1001, getUdpPort()-2001, 0L);
                        gossiper.start();
                    }


                } else {
                   // logger.severe("Election failed: no leader elected. Retrying...");
                    continue;
                }
            }


        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        shutdown = true;
        if (gossiper != null) {
            gossiper.shutdown();
        }
        super.shutdown();
    }
}