package edu.yu.cs.com3800.stage4;

import edu.yu.cs.com3800.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GatewayPeerServerImpl extends PeerServerImpl {
    private volatile InetSocketAddress currentLeaderAddress;
    private Logger logger;

    public GatewayPeerServerImpl(int udpPort, long peerEpoch, Long id, Map<Long, InetSocketAddress> peerIDtoAddress) throws IOException {
        super(udpPort, peerEpoch, id, peerIDtoAddress, id, 1);
        setPeerState(ServerState.OBSERVER);

        loadLoggers();
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

    public InetSocketAddress getCurrentLeaderAddress() {
        return this.currentLeaderAddress;
    }

    @Override
    public void setCurrentLeader(Vote vote) throws IOException {
        super.setCurrentLeader(vote);

        if (vote != null) {
            InetSocketAddress leaderAddress = getPeerByID(vote.getProposedLeaderID());
            this.currentLeaderAddress = leaderAddress;

            if (this.logger != null) {
                this.logger.info("LIDER de Gateway es: " + vote.getProposedLeaderID());
            } else {
                System.out.println("AAAAAAJFaiongosdngos");
            }
        }
    }

    @Override
    public int getQuorumSize() {
        return ((super.getQuorumSize() * 2) - 1) / 2 + 1;
    }
}

