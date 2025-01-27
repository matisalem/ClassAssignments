package edu.yu.cs.com3800.stage2;

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
        File path2 = new File("logsElection");
        path2.mkdir();
        FileHandler fh2 = null;
        try {
            fh2 = new FileHandler("logsElection/LeaderElectionLogger" + getServerId() + ".log");
        } catch (IOException e) {}
        loggerElection.addHandler(fh2);
        SimpleFormatter formatter2 = new SimpleFormatter();
        fh2.setFormatter(formatter2);
    }

    @Override
    public void shutdown(){
        this.shutdown = true;
        this.senderWorker.shutdown();
        this.receiverWorker.shutdown();
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

    @Override
    public void run(){
        //step 1: create and run thread that sends broadcast messages
        //step 2: create and run thread that listens for messages sent to this server
        this.senderWorker = new UDPMessageSender(outgoingMessages, myPort);
        try {
            this.receiverWorker = new UDPMessageReceiver(incomingMessages, myAddress, myPort, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.senderWorker.start();
        this.receiverWorker.start();
        //step 3: main server loop
        try{
            while (!this.shutdown){
                switch (getPeerState()){
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
                    default: break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.senderWorker.shutdown();
            this.receiverWorker.shutdown();
        }
    }

}
