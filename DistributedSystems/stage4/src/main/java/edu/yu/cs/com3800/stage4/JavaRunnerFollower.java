package edu.yu.cs.com3800.stage4;

import edu.yu.cs.com3800.*;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.logging.*;
import java.io.*;

public class JavaRunnerFollower extends Thread {

    private PeerServer servidor;
    private JavaRunner javaRunner;
    private Logger logger;
    private InetSocketAddress lider;

    ServerSocket serverSocket;
    Socket socket;
    DataOutputStream os;
    DataInputStream is;

    public JavaRunnerFollower(PeerServerImpl peerServer, InetSocketAddress leader, Logger log){
        servidor = peerServer;
        lider = leader;
        logger = log;
        this.setDaemon(true);

        try {
            this.javaRunner = new JavaRunner();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(servidor.getUdpPort() + 5);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (!isInterrupted()) {

            try {
                socket = serverSocket.accept();
                os = new DataOutputStream(socket.getOutputStream());
                is = new DataInputStream(socket.getInputStream());

                try {

                    int length = is.readInt();
                    byte[] messageBytes = new byte[length];
                    is.readFully(messageBytes, 0, messageBytes.length);
                    Message mensaje = new Message(messageBytes);
                    if (mensaje == null || !mensaje.getMessageType().equals(Message.MessageType.WORK)) continue;



                    String response = javaRunner.compileAndRun(new ByteArrayInputStream(mensaje.getMessageContents()));
                    Message mensajeFinal = new Message(Message.MessageType.COMPLETED_WORK, response.getBytes(), servidor.getAddress().getHostString(), servidor.getUdpPort(), lider.getHostString(), lider.getPort(), mensaje.getRequestID());
                    //             om.offer(mensajeFinal);

                    byte[] messagesBytes = mensajeFinal.getNetworkPayload();
                    os.writeInt(messagesBytes.length);
                    os.write(messagesBytes);
                    logger.info("Trabajo mandado! BH funciona, desde el servidor " + servidor.getServerId());

                } catch (Exception e) {
                    e.printStackTrace();


//                    String response = "";
//                    // Said in Piazza to send this to client
//                    Message mensajeFinal = new Message(Message.MessageType.COMPLETED_WORK, response.getBytes(), servidor.getAddress().getHostString(), servidor.getUdpPort(), lider.getHostString(), lider.getPort(), mensaje.getRequestID());
//                    om.offer(mensajeFinal);
//                    logger.info("Mal codigo. Error: " + e.getMessage());
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        interrupt();
    }
}


