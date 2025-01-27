// ULTIMO LA mati


// LA mati


package edu.yu.cs.com3800;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.yu.cs.com3800.PeerServer.ServerState;

/**We are implemeting a simplfied version of the election algorithm. For the complete version which covers all possible scenarios, see https://github.com/apache/zookeeper/blob/90f8d835e065ea12dddd8ed9ca20872a4412c78a/zookeeper-server/src/main/java/org/apache/zookeeper/server/quorum/FastLeaderElection.java#L913
 */

public class LeaderElection {

    //  to wait once we believe we've reached the end of leader election.
    private final static int finalizeWait = 200;
    private final static int maxNotificationInterval = 60000;
    PeerServer servidor;
    LinkedBlockingQueue<Message> mensajes;
    private Logger logger;
    private long proposedLeader;
    private long proposedEpoch;
    private int time;
    private long lastNotificationTime;
    Set<Long> servers = new HashSet<>();




    //   boolean observerIsHere = false;

    Map<Long, ElectionNotification> votes = new HashMap<>();



    public LeaderElection(PeerServer server, LinkedBlockingQueue<Message> incomingMessages, Logger logger) {
        servidor = server;
        mensajes = incomingMessages;
        this.logger = logger;
        proposedLeader = servidor.getCurrentLeader().getProposedLeaderID();
        proposedEpoch = servidor.getCurrentLeader().getPeerEpoch();
        time = finalizeWait;
    }


    public synchronized Vote lookForLeader() {


        if(!servers.isEmpty()) {
            List<Long> toRemove = new ArrayList<>();
            for (var i : servers) {
                if (servidor.isPeerDead(i)) toRemove.add(i);
            }

            for (var i : toRemove) {
                servers.remove(i);
            }
        }

        proposedEpoch = servidor.getPeerEpoch();
        proposedLeader = servidor.getCurrentLeader().getProposedLeaderID();
        ElectionNotification vot = sendNotifications();

        Random random = new Random();

        while (servidor.getPeerState() == PeerServer.ServerState.LOOKING || servidor.getPeerState() == ServerState.OBSERVER) {

            Message message = null;
            try {
                message = mensajes.poll(time* 2L + random.nextInt(time), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            if(message != null && Objects.requireNonNull(message).getMessageType() != Message.MessageType.ELECTION){
                logger.info("Gossip message lost in election\n" + servers + " \n" + votes);
                continue;
            }

            if(message == null) {
                sendNotifications();

            if (time < maxNotificationInterval) time = Math.min(time *= 2, maxNotificationInterval);

                continue;

            }

            ElectionNotification notification = getNotificationFromMessage(message);

            if(servidor.isPeerDead(notification.getSenderID())){
                continue;
            }

            votes.put(notification.getSenderID(), notification);
            votes.put(servidor.getServerId(), vot);
            if(notification.getState() == ServerState.OBSERVER){
                continue;
            }
            if(notification.getPeerEpoch() < servidor.getPeerEpoch())continue;



            try {
                if (notification.getState() == ServerState.LOOKING) {
                    if (supersedesCurrentVote(notification.getProposedLeaderID(), notification.getPeerEpoch())) {
                        proposedLeader = notification.getProposedLeaderID();
                        proposedEpoch = notification.getPeerEpoch();
                        ElectionNotification notif = new ElectionNotification(proposedLeader, ServerState.LOOKING, servidor.getServerId(), proposedEpoch);
                        votes.putIfAbsent(servidor.getServerId(), notif);
                        Vote voto = new Vote(proposedLeader, proposedEpoch);
                        servidor.setCurrentLeader(voto);
                        sendNotifications();
                    }

                    if (haveEnoughVotes(votes, new Vote(proposedLeader, proposedEpoch))) {
                        Message newMessage = mensajes.poll(finalizeWait, TimeUnit.MILLISECONDS);
                        while (newMessage != null) {
                            ElectionNotification newNotification = getNotificationFromMessage(newMessage);
                            if (servidor.isPeerDead(newNotification.getSenderID())) {
                                logger.info("Ignoring notification from failed server during finalization: " + newNotification.getSenderID());
                                newMessage = mensajes.poll(finalizeWait, TimeUnit.MILLISECONDS);
                                continue;
                            }
                            if (supersedesCurrentVote(newNotification.getProposedLeaderID(), newNotification.getPeerEpoch())) {
                                proposedLeader = newNotification.getProposedLeaderID();
                                proposedEpoch = newNotification.getPeerEpoch();
                                sendNotifications();
                                break;
                            }
                            newMessage = mensajes.poll(finalizeWait, TimeUnit.MILLISECONDS);
                        }
                        servers = votes.keySet();
                        return acceptElectionWinner(new ElectionNotification(proposedLeader, ServerState.LEADING, servidor.getServerId(), proposedEpoch));
                    }
                } else if (notification.getState() == ServerState.LEADING) {votes.put(notification.getSenderID(), notification);
                    if(notification.getPeerEpoch() == proposedEpoch && haveEnoughVotes(votes, notification)){
                        return acceptElectionWinner(notification);
                    } else{

                        if(haveEnoughVotes(votes, notification )){
                            proposedEpoch = notification.getPeerEpoch();
                            return acceptElectionWinner(notification);
                        }
                    }
                } else if (notification.getState() == ServerState.FOLLOWING) {

                    votes.put(notification.getSenderID(), notification);

                    if(notification.getPeerEpoch() == proposedEpoch && haveEnoughVotes(votes, notification)){
                        return acceptElectionWinner(notification);
                    } else{
                        if(haveEnoughVotes(votes, notification )){
                            proposedEpoch = notification.getPeerEpoch();
                            return acceptElectionWinner(notification);
                        }
                    }
                    break;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }


    private synchronized ElectionNotification sendNotifications() {
        ElectionNotification notificacion = new ElectionNotification(servidor.getCurrentLeader().getProposedLeaderID(), servidor.getPeerState(), servidor.getServerId(), servidor.getPeerEpoch());
        ByteBuffer buffer = ByteBuffer.allocate(26);
        buffer.clear();
        buffer.putLong(notificacion.getProposedLeaderID());  // 8
        buffer.putChar(notificacion.getState().getChar()); // 2
        buffer.putLong(notificacion.getSenderID()); // 8
        buffer.putLong(notificacion.getPeerEpoch()); // 8
        servidor.sendBroadcast(Message.MessageType.ELECTION, buffer.array());
        return notificacion;
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

        proposedLeader = n.getProposedLeaderID();
        proposedEpoch = n.getPeerEpoch();

        if (n.getProposedLeaderID() == servidor.getServerId()) servidor.setPeerState(ServerState.LEADING);
        else servidor.setPeerState(ServerState.FOLLOWING);
        Vote vote = new ElectionNotification(proposedLeader, servidor.getPeerState(), proposedLeader, proposedEpoch);
        mensajes.clear();
        if(!servers.isEmpty()){
            if (votes.size() < servers.size()){
                for (var i = 0; i < 6; i++) {
                    sendNotifications();
                }
            }
        }
        servers = votes.keySet();
        votes = new HashMap<>();
        sendNotifications();
        sendNotifications();
        return vote;
    }



    public static byte[] buildMsgContent(ElectionNotification notification) {
        ByteBuffer buffer = ByteBuffer.allocate(2 + Long.BYTES*3);
        buffer.putLong(notification.getProposedLeaderID());
        buffer.putChar(notification.getState().getChar());
        buffer.putLong(notification.getSenderID());
        buffer.putLong(notification.getPeerEpoch());
        return buffer.array();
    }


    protected boolean supersedesCurrentVote(long newId, long newEpoch) {
        if (servidor.isPeerDead(newId)) {
            logger.info("Ignoring proposal from failed server: " + newId);
            return false;
        }
        if (newEpoch != this.proposedEpoch) {
            return newEpoch > this.proposedEpoch;
        }
        return newId > this.proposedLeader;
    }




    protected boolean haveEnoughVotes(Map<Long, ElectionNotification> votes, Vote proposal) {
        int quorum = servidor.getQuorumSize();
        int count = 0;

        for (var senderID : votes.keySet()) {
            if (servidor.isPeerDead(senderID)) {
                logger.info("Excluding vote from failed server: " + senderID);
                continue;
            }
            if (votes.get(senderID).getProposedLeaderID() == proposal.getProposedLeaderID()) {
                count++;
            }
            if (count >= quorum) return true;
        }
        return false;
    }

}


