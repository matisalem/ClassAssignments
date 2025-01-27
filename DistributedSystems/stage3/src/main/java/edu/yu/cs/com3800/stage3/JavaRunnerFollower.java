package edu.yu.cs.com3800.stage3;

import edu.yu.cs.com3800.*;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.logging.*;
import java.io.*;

public class JavaRunnerFollower extends Thread {

    private PeerServer servidor;
    private JavaRunner javaRunner;
    private LinkedBlockingQueue<Message> im;
    private LinkedBlockingQueue<Message> om;
    private Logger logger;
    private InetSocketAddress lider;

    public JavaRunnerFollower(PeerServerImpl peerServer, LinkedBlockingQueue<Message> incomingMessages, LinkedBlockingQueue<Message> outgoingMessages, InetSocketAddress leader, Logger log){
        servidor = peerServer;
        im = incomingMessages;
        om = outgoingMessages;
        lider = leader;
        logger = log;

        try {
            this.javaRunner = new JavaRunner();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!isInterrupted()) {

            try {
                Message mensaje = im.take();
                if (mensaje == null || !mensaje.getMessageType().equals(Message.MessageType.WORK)) continue;

                try {
                    String response = javaRunner.compileAndRun(new ByteArrayInputStream(mensaje.getMessageContents()));
                    Message mensajeFinal = new Message(Message.MessageType.COMPLETED_WORK, response.getBytes(), servidor.getAddress().getHostString(), servidor.getUdpPort(), lider.getHostString(), lider.getPort(), mensaje.getRequestID());
                    om.offer(mensajeFinal);
                    logger.info("Trabajo mandado! BH funciona, desde el servidor " + servidor.getServerId());
                } catch (Exception e) {
                    e.printStackTrace();
                    String response = "Mal codigo. Error: " + e.getMessage();
                    // Said in Piazza to send this to client
                    Message mensajeFinal = new Message(Message.MessageType.COMPLETED_WORK, response.getBytes(), servidor.getAddress().getHostString(), servidor.getUdpPort(), lider.getHostString(), lider.getPort(), mensaje.getRequestID());
                    om.offer(mensajeFinal);
                    logger.info("Mal codigo. Error: " + e.getMessage());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        interrupt();
    }
}
