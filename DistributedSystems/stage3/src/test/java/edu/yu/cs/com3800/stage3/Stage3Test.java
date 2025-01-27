package edu.yu.cs.com3800.stage3;

import edu.yu.cs.com3800.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import edu.yu.cs.com3800.*;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Stage3Test {

    private String validClass = "package edu.yu.cs.fall2019.com3800.stage1;\n\npublic class HelloWorld\n{\n    public String run()\n    {\n        return \"Hello world!\";\n    }\n}\n";

    private LinkedBlockingQueue<Message> outgoingMessages;
    private LinkedBlockingQueue<Message> incomingMessages;
    private int[] ports = {8010, 8020, 8030, 8040, 8050, 8060, 8070, 8080};
    //private int[] ports = {8010, 8020};
    private int leaderPort = this.ports[this.ports.length - 1];
    private int myPort = 9999;
    private InetSocketAddress myAddress = new InetSocketAddress("localhost", this.myPort);
    private ArrayList<PeerServer> servers;


    private void printLeaders() {
        for (PeerServer server : this.servers) {
            Vote leader = server.getCurrentLeader();
            if (leader != null) {
                System.out.println("Server on port " + server.getAddress().getPort() + " whose ID is " + server.getServerId() + " has the following ID as its leader: " + leader.getProposedLeaderID() + " and its state is " + server.getPeerState().name());
            }
        }
    }

    private void stopServers() {
        for (PeerServer server : this.servers) {
            server.shutdown();
        }
    }

    private void printResponses() throws Exception {
        String completeResponse = "";
        for (int i = 0; i < this.ports.length; i++) {
            Message msg = this.incomingMessages.take();
            String response = new String(msg.getMessageContents());
            completeResponse += "Response to request " + msg.getRequestID() + ":\n" + response + "\n\n";
        }
        System.out.println(completeResponse);
    }

    private void sendMessage(String code) throws InterruptedException {
        Message msg = new Message(Message.MessageType.WORK, code.getBytes(), this.myAddress.getHostString(), this.myPort, "localhost", this.leaderPort);
        this.outgoingMessages.put(msg);
    }

    private void createServers() throws IOException {
        //create IDs and addresses
        HashMap<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>(8);
        for (int i = 0; i < this.ports.length; i++) {
            peerIDtoAddress.put(Integer.valueOf(i).longValue(), new InetSocketAddress("localhost", this.ports[i]));
        }
        //create servers
        this.servers = new ArrayList<>(3);
        for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
            HashMap<Long, InetSocketAddress> map = (HashMap<Long, InetSocketAddress>) peerIDtoAddress.clone();
            map.remove(entry.getKey());
            PeerServerImpl server = new PeerServerImpl(entry.getValue().getPort(), 0, entry.getKey(), map);
            this.servers.add(server);
            server.start();
        }
    }



    @Test
    public void judah() throws Exception {
        this.outgoingMessages = new LinkedBlockingQueue<>();
        UDPMessageSender sender = new UDPMessageSender(this.outgoingMessages,this.myPort);
        //step 2: create servers
        createServers();
        //step2.1: wait for servers to get started
        try {
            Thread.sleep(   13000);
        }
        catch (InterruptedException e) {
        }
        printLeaders();
        //step 3: since we know who will win the election, send requests to the leader, this.leaderPort
        for (int i = 0; i < this.ports.length; i++) {
            String code = this.validClass.replace("world!", "world! from code version " + i);
            sendMessage(code);
        }
        Util.startAsDaemon(sender, "Sender thread");
        this.incomingMessages = new LinkedBlockingQueue<>();
        UDPMessageReceiver receiver = new UDPMessageReceiver(this.incomingMessages, this.myAddress, this.myPort,null);
        Util.startAsDaemon(receiver, "Receiver thread");
        //step 4: validate responses from leader
        printResponses();

        //step 5: stop servers
        stopServers();
    }

    @Test
    public void manyRequests() throws Exception {

        this.outgoingMessages = new LinkedBlockingQueue<>();
        UDPMessageSender sender = new UDPMessageSender(this.outgoingMessages, this.myPort);
        createServers();
        Thread.sleep(13000);
        Util.startAsDaemon(sender, "Sender thread");
        this.incomingMessages = new LinkedBlockingQueue<>();
        UDPMessageReceiver receiver = new UDPMessageReceiver(this.incomingMessages, this.myAddress, this.myPort, null);
        Util.startAsDaemon(receiver, "Receiver thread");

        int requests = 25;
        for (int i = 0; i < requests; i++) {
            String code = this.validClass.replace("world!", "world! from code version " + i);
            sendMessage(code);
        }

        Set<Integer> respuestas = new HashSet<>();
        for (int i = 0; i < requests; i++) {
            Message r = incomingMessages.take();
            respuestas.add((int)r.getRequestID());
        }
        assertEquals(requests, respuestas.size());
        stopServers();
    }

    @Test
    public void largeCode() throws Exception {

        this.outgoingMessages = new LinkedBlockingQueue<>();
        UDPMessageSender sender = new UDPMessageSender(this.outgoingMessages, this.myPort);
        createServers();
        Thread.sleep(13000);
        Util.startAsDaemon(sender, "Sender thread");
        this.incomingMessages = new LinkedBlockingQueue<>();
        UDPMessageReceiver receiver = new UDPMessageReceiver(this.incomingMessages, this.myAddress, this.myPort, null);
        Util.startAsDaemon(receiver, "Receiver thread");

        String code = "package edu.yu.cs.fall2019.com3800.stage3;\n" +
                "public class HelloWorld {\n" +
                "    public String run() throws InterruptedException {\n" +
                "        Thread.sleep(3000);\n" +
                "        return \"boooooca\";\n" +
                "    }\n" +
                "}";
        sendMessage(code);

        Message r = incomingMessages.take();
        String result = new String(r.getMessageContents());
        assertTrue(result.contains("boooooca"));
        stopServers();
    }


    @Test
    public void malCode() throws Exception {

        this.outgoingMessages = new LinkedBlockingQueue<>();
        UDPMessageSender sender = new UDPMessageSender(this.outgoingMessages, this.myPort);
        createServers();
        Thread.sleep(13000);
        Util.startAsDaemon(sender, "Sender thread");
        this.incomingMessages = new LinkedBlockingQueue<>();
        UDPMessageReceiver receiver = new UDPMessageReceiver(this.incomingMessages, this.myAddress, this.myPort, null);
        Util.startAsDaemon(receiver, "Receiver thread");

        String code = "Te fuiste a la B";
        sendMessage(code);

        Message mensaje = this.incomingMessages.take();
        String erorr = new String(mensaje.getMessageContents());
        assertTrue(erorr.contains("Mal codigo"));

        stopServers();
    }



}
