package edu.yu.cs.com3800.stage3;

import edu.yu.cs.com3800.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.io.*;
import java.net.*;

public class RoundRobinLeader extends Thread {
    private PeerServerImpl servidor;
    private Map<Long, InetSocketAddress> peerToAdd;
    private LinkedBlockingQueue<Message> om;
    private LinkedBlockingQueue<Message> im;
    private List<Long> workers;
    private Logger logger;

    private String clientHost;
    private int clientPort;
    private int turno;
    private long requests;

    public RoundRobinLeader(PeerServerImpl peerServer, Map<Long, InetSocketAddress> peerIDtoAddress, LinkedBlockingQueue<Message> incomingMessages, LinkedBlockingQueue<Message> outgoingMessages, Logger log) {
        servidor = peerServer;
        peerToAdd = peerIDtoAddress;
        om = outgoingMessages;
        im = incomingMessages;
        workers = new ArrayList<>(peerIDtoAddress.keySet());
        workers.remove(servidor.getServerId());
        turno = 0;
        requests = 0;
        setDaemon(true);
        logger = log;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {

            Message mensaje = null;
            try {

                mensaje = im.take();

                switch (mensaje.getMessageType()) {
                    case WORK:
                        clientPort = mensaje.getSenderPort();
                        clientHost = mensaje.getSenderHost();
                        logger.info("Trabajo mandado a Follower " + workers.get(turno % workers.size()));
                        break;
                    case COMPLETED_WORK:
                        Message mensaje2 = new Message(Message.MessageType.COMPLETED_WORK, mensaje.getMessageContents(), servidor.getAddress().getHostString(), servidor.getUdpPort(), clientHost, clientPort, mensaje.getRequestID());
                        om.offer(mensaje2);
                        logger.info("Trabajo terminado desde Follower " + mensaje2.getReceiverHost());
                        break;
                }

                Long server = workers.get(turno);
                turno = (turno + 1) % workers.size();
                InetSocketAddress worker = peerToAdd.get(server);
                Message mensajeFinal = new Message(Message.MessageType.WORK, mensaje.getMessageContents(), servidor.getAddress().getHostString(), servidor.getUdpPort(), worker.getHostString(), worker.getPort(), requests);
                requests++;
                om.offer(mensajeFinal);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        interrupt();
    }
}
