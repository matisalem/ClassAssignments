package edu.yu.cs.com3800;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.yu.cs.com3800.PeerServer.ServerState;
import edu.yu.cs.com3800.stage2.PeerServerImpl;

/**We are implemeting a simplfied version of the election algorithm. For the complete version which covers all possible scenarios, see https://github.com/apache/zookeeper/blob/90f8d835e065ea12dddd8ed9ca20872a4412c78a/zookeeper-server/src/main/java/org/apache/zookeeper/server/quorum/FastLeaderElection.java#L913
 */
public class LeaderElection {

    //  to wait once we believe we've reached the end of leader election.
    private final static int finalizeWait = 3200;

    // Upper bound on the amount of time between two consecutive notification checks.
    // This impacts the amount of time to get the system up again after long partitions. Currently 30 seconds.
    private final static int maxNotificationInterval = 30000;
    PeerServer servidor;
    LinkedBlockingQueue<Message> mensajes;
    private Logger logger;
    private long proposedLeader;
    private long proposedEpoch;
    private int time;



    public LeaderElection(PeerServer server, LinkedBlockingQueue<Message> incomingMessages, Logger logger) {
        servidor = server;
        mensajes = incomingMessages;
        this.logger = logger;
        proposedLeader = servidor.getCurrentLeader().getProposedLeaderID();
        proposedEpoch = servidor.getCurrentLeader().getPeerEpoch();
        time = finalizeWait;
    }

    /**
     * Note that the logic in the comments below does NOT cover every last "technical" detail you will need to address to implement the election algorithm.
     * How you store all the relevant state, etc., are details you will need to work out.
     * @return the elected leader
     */
    public synchronized Vote lookForLeader() {
        try {
            //send initial notifications to get things started
            sendNotifications();
            Random random = new Random();
            Map<Long, ElectionNotification> votes = new HashMap<>();

            //Loop in which we exchange notifications with other servers until we find a leader
            while(true){
                //Remove next notification from queue
                Message message = null;
                try {
                    message = mensajes.poll(time * 2L + random.nextInt(time), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                //If no notifications received...
                    //...resend notifications to prompt a reply from others
                    //...use exponential back-off when notifications not received but no longer than maxNotificationInterval...
                if (message == null){
                    sendNotifications();
                    if (time < maxNotificationInterval) time = Math.min(time *= 2, maxNotificationInterval);
                    continue;
                }
                //If we did get a message...
                ElectionNotification notification = getNotificationFromMessage(message);
                //...if it's for an earlier epoch, or from an observer, ignore it.
                if (notification.getPeerEpoch() < this.proposedEpoch || notification.getState() == ServerState.OBSERVER) continue;


                if (servidor.getPeerState() == ServerState.LOOKING) {
                    //...if the received message has a vote for a leader which supersedes mine, change my vote (and send notifications to all other voters about my new vote).
                    //(Be sure to keep track of the votes I received and who I received them from.)

                    if (supersedesCurrentVote(notification.getProposedLeaderID(), notification.getPeerEpoch())) {
                        proposedLeader = notification.getProposedLeaderID();
                        proposedEpoch = notification.getPeerEpoch();
                        Vote voto = new Vote(proposedLeader, proposedEpoch);
                        servidor.setCurrentLeader(voto);
                        sendNotifications();
                    }

                    votes.put(notification.getSenderID(), notification);
                    boolean hayMejor = false;

                    //If I have enough votes to declare my currently proposed leader as the leader...
                    if (haveEnoughVotes(votes, new Vote(proposedLeader, proposedLeader))) {
                        logger.info("Parece que hay suficientes");

                        Message newMessage = mensajes.poll(finalizeWait, TimeUnit.MILLISECONDS);
                        while (newMessage != null) {

                            ElectionNotification newNotification = getNotificationFromMessage(newMessage);

                            if (supersedesCurrentVote(newNotification.getProposedLeaderID(), newNotification.getPeerEpoch())){
                                this.proposedLeader = newNotification.getProposedLeaderID();
                                this.proposedEpoch = newNotification.getPeerEpoch();
                                sendNotifications();
                                hayMejor = true;
                                logger.info("Hay uno mejor");
                                break;
                            }
                            newMessage = mensajes.poll(finalizeWait, TimeUnit.MILLISECONDS);
                        }
                        if (hayMejor) continue;
                        logger.info("Termino la busqueda!");
                        return acceptElectionWinner(notification);
                    }

                } else if (servidor.getPeerState() == ServerState.LEADING) {
                    return new Vote(servidor.getServerId(), servidor.getPeerEpoch());
                } else if (servidor.getPeerState() == ServerState.FOLLOWING) {
                    return servidor.getCurrentLeader();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private synchronized void sendNotifications() {
        ElectionNotification notificacion = new ElectionNotification(servidor.getCurrentLeader().getProposedLeaderID(), servidor.getPeerState(), servidor.getServerId(), servidor.getPeerEpoch());
        ByteBuffer buffer = ByteBuffer.allocate(26);
        buffer.clear();
        buffer.putLong(notificacion.getProposedLeaderID());  // 8
        buffer.putChar(notificacion.getState().getChar()); // 2
        buffer.putLong(notificacion.getSenderID()); // 8
        buffer.putLong(notificacion.getPeerEpoch()); // 8
        servidor.sendBroadcast(Message.MessageType.ELECTION, buffer.array());
    }

    public static ElectionNotification getNotificationFromMessage(Message mensaje) {
        ByteBuffer buffer = ByteBuffer.wrap(mensaje.getMessageContents());
        long leader = buffer.getLong();
        char stateChar = buffer.getChar();
        long senderID = buffer.getLong();
        long peerEpoch = buffer.getLong();
        return new ElectionNotification(leader, PeerServer.ServerState.getServerState(stateChar), senderID, peerEpoch);
    }

    private Vote acceptElectionWinner(ElectionNotification n) {
        if (n.getProposedLeaderID() == servidor.getServerId()) {
            servidor.setPeerState(ServerState.LEADING);
            sendNotifications();
            logger.log(Level.INFO, "Servidor " + servidor.getServerId() + " gano la Libertadores");
        } else {
            servidor.setPeerState(ServerState.FOLLOWING);
            sendNotifications();
            logger.log(Level.INFO, "Servidor " + servidor.getServerId() + " se fue a la B" + n.getProposedLeaderID());
        }

        mensajes.clear();
        return new Vote(n.getProposedLeaderID(), n.getPeerEpoch());
    }

    public static byte[] buildMsgContent(ElectionNotification notification) {
        ByteBuffer buffer = ByteBuffer.allocate(2 + Long.BYTES*3);
        buffer.putLong(notification.getProposedLeaderID());
        buffer.putChar(notification.getState().getChar());
        buffer.putLong(notification.getSenderID());
        buffer.putLong(notification.getPeerEpoch());
        return buffer.array();
    }

    /*
     * We return true if one of the following three cases hold:
     * 1- New epoch is higher
     * 2- New epoch is the same as current epoch, but server id is higher.
     */
    protected boolean supersedesCurrentVote(long newId, long newEpoch) {
        return (newEpoch > this.proposedEpoch) || ((newEpoch == this.proposedEpoch) && (newId > this.proposedLeader));
    }

    /**
     * Termination predicate. Given a set of votes, determines if we have sufficient support for the proposal to declare the end of the election round.
     * Who voted for who isn't relevant, we only care that each server has one current vote.
     */
    protected boolean haveEnoughVotes(Map<Long, ElectionNotification> votes, Vote proposal) {

        int quorum = servidor.getQuorumSize();
        int cuenta = 0;
        for (var i : votes.keySet()){
            if (votes.get(i).getProposedLeaderID() == proposal.getProposedLeaderID()) cuenta++;
            if (cuenta >= quorum) return true;
        }
        logger.info("No habia quorum");
        return false;
    }
}