package edu.yu.cs.com3800.stage2;

import edu.yu.cs.com3800.*;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class Stage2Test {


    @Test
    public void servidores() {
        Map<Long, InetSocketAddress> peerMap = new HashMap<>();
        peerMap.put(2L, new InetSocketAddress("localhost", 8020));
        peerMap.put(3L, new InetSocketAddress("localhost", 8030));

        PeerServerImpl server = new PeerServerImpl(8010, 1L, 1L, peerMap);

        assertEquals(1L, server.getServerId().longValue());
        assertEquals(8010, server.getUdpPort());
        assertEquals(PeerServer.ServerState.LOOKING, server.getPeerState());
        assertEquals(1L, server.getPeerEpoch());
        assertEquals(1L, server.getCurrentLeader().getProposedLeaderID());

    }

    @Test
    public void smallTest() throws InterruptedException {
        Map<Long, InetSocketAddress> peerMap = new HashMap<>();
        peerMap.put(1L, new InetSocketAddress("localhost", 8021));
        peerMap.put(2L, new InetSocketAddress("localhost", 8022));
        peerMap.put(3L, new InetSocketAddress("localhost", 8024));

        ArrayList<PeerServerImpl> servers = new ArrayList<>(3);
        for (Map.Entry<Long, InetSocketAddress> entry : peerMap.entrySet()) {
            HashMap<Long, InetSocketAddress> map = (HashMap<Long, InetSocketAddress>) ((HashMap<Long, InetSocketAddress>) peerMap).clone();
            map.remove(entry.getKey());
            PeerServerImpl server = new PeerServerImpl(entry.getValue().getPort(), 1, entry.getKey(), map);
            servers.add(server);
            new Thread(server, "Server on port " + server.getAddress().getPort()).start();
        }

        Thread.sleep(5000);

        for (var i : servers){
            assertEquals(3L, i.getCurrentLeader().getProposedLeaderID());
        }
        for (var i : servers){
            i.shutdown();
        }
    }
    

    @Test
    public void tradeMessages() {

        HashMap<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>(3);
        peerIDtoAddress.put(1L, new InetSocketAddress("localhost", 8025));
        peerIDtoAddress.put(2L, new InetSocketAddress("localhost", 8026));
        peerIDtoAddress.put(3L, new InetSocketAddress("localhost", 8030));
        peerIDtoAddress.put(4L, new InetSocketAddress("localhost", 8040));
        peerIDtoAddress.put(5L, new InetSocketAddress("localhost", 8050));
        peerIDtoAddress.put(6L, new InetSocketAddress("localhost", 8060));
        peerIDtoAddress.put(7L, new InetSocketAddress("localhost", 8070));
        peerIDtoAddress.put(8L, new InetSocketAddress("localhost", 8080));

        ArrayList<PeerServer> servers = new ArrayList<>(3);
        for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
            HashMap<Long, InetSocketAddress> map = (HashMap<Long, InetSocketAddress>) peerIDtoAddress.clone();
            map.remove(entry.getKey());
            PeerServerImpl server = new PeerServerImpl(entry.getValue().getPort(), 0, entry.getKey(), map);
            servers.add(server);
            new Thread(server, "Server on port " + server.getAddress().getPort()).start();
        }
        try {
            Thread.sleep(5000);
        }
        catch (Exception e) {
        }
        for (var i : servers) {
            long lider = i.getCurrentLeader().getProposedLeaderID();
            System.out.println("SERVER: " + i + ", STATE: " + i.getPeerState());
            assertEquals(8, lider);
            if (i.getServerId() != 8) {
                assertEquals(PeerServer.ServerState.FOLLOWING, i.getPeerState());
            } else {
                System.out.println(lider + "AAA");
                assertEquals(PeerServer.ServerState.LEADING, i.getPeerState());
            }
        }
        
        
        for (PeerServer server : servers) {
            Vote leader = server.getCurrentLeader();
            if (leader != null) {
                System.out.println("Server on port " + server.getAddress().getPort() + " whose ID is " + server.getServerId() + " has the following ID as its leader: " + leader.getProposedLeaderID() + " and its state is " + server.getPeerState().name());
                server.shutdown();
            }
        }
    }

    @Test
    public void manyServers() {

        HashMap<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>(3);
        peerIDtoAddress.put(1L, new InetSocketAddress("localhost", 8001));
        peerIDtoAddress.put(2L, new InetSocketAddress("localhost", 8002));
        peerIDtoAddress.put(3L, new InetSocketAddress("localhost", 8003));
        peerIDtoAddress.put(4L, new InetSocketAddress("localhost", 8004));
        peerIDtoAddress.put(5L, new InetSocketAddress("localhost", 8005));
        peerIDtoAddress.put(6L, new InetSocketAddress("localhost", 8006));
        peerIDtoAddress.put(7L, new InetSocketAddress("localhost", 8007));
        peerIDtoAddress.put(8L, new InetSocketAddress("localhost", 8008));
        peerIDtoAddress.put(9L, new InetSocketAddress("localhost", 8009));
        peerIDtoAddress.put(10L, new InetSocketAddress("localhost", 8010));
        peerIDtoAddress.put(11L, new InetSocketAddress("localhost", 8011));
        peerIDtoAddress.put(12L, new InetSocketAddress("localhost", 8012));
        peerIDtoAddress.put(13L, new InetSocketAddress("localhost", 8013));
        peerIDtoAddress.put(14L, new InetSocketAddress("localhost", 8014));
        peerIDtoAddress.put(15L, new InetSocketAddress("localhost", 8015));
        peerIDtoAddress.put(16L, new InetSocketAddress("localhost", 8016));
        peerIDtoAddress.put(17L, new InetSocketAddress("localhost", 8017));
        peerIDtoAddress.put(18L, new InetSocketAddress("localhost", 8018));
        peerIDtoAddress.put(19L, new InetSocketAddress("localhost", 8019));
        peerIDtoAddress.put(20L, new InetSocketAddress("localhost", 8020));

        List<PeerServerImpl> servidores = new ArrayList<>();

        for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
            HashMap<Long, InetSocketAddress> map = (HashMap<Long, InetSocketAddress>) peerIDtoAddress.clone();
            map.remove(entry.getKey());
            PeerServerImpl server = new PeerServerImpl(entry.getValue().getPort(), 0, entry.getKey(), map);
            servidores.add(server);
            new Thread(server, "Server on port " + server.getAddress().getPort()).start();
        }
        
        try {
            Thread.sleep(18000);
        } catch (Exception e) {}

        for (var i : servidores) {
            long lider = i.getCurrentLeader().getProposedLeaderID();
            System.out.println("SERVER: " + i + ", STATE: " + i.getPeerState());
            assertEquals(20, lider);
            if (i.getServerId() != 20) {
                assertEquals(PeerServer.ServerState.FOLLOWING, i.getPeerState());
            } else {
                System.out.println(lider + "AAA");
                assertEquals(PeerServer.ServerState.LEADING, i.getPeerState());
            }
        }

        for (var i : servidores){
            i.shutdown();
        }
    }
}