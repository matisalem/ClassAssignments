package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.Message;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

public class ClientExecutor implements Runnable{

    String validClass;
    private InetSocketAddress myAddress;
    private int myPort;
    private int leaderPort;
    private int httpPort;
    private CountDownLatch latch;

    public ClientExecutor(String validClass, InetSocketAddress myAddress, int myPort, int leaderPort, int httpPort, CountDownLatch latch){
        this.validClass = validClass;
        this.myAddress = myAddress;
        this.myPort = myPort;
        this.leaderPort = leaderPort;
        this.httpPort = httpPort;
        this.latch = latch;
    }




    @Override
    public void run() {

        HttpURLConnection http =null;
        Message msg = new Message(Message.MessageType.WORK, validClass.getBytes(), this.myAddress.getHostString(), this.myPort, "localhost", this.leaderPort);

        //http = (HttpURLConnection) new URL("http", "localhost", httpPort, "/compileandrun").openConnection();



        //Send code to server

        try {
            http = (HttpURLConnection) new URL("http", "localhost", httpPort, "/compileandrun").openConnection();
            http.setRequestMethod("POST");
        } catch (IOException e) {
            e.printStackTrace();
        }
        http.setRequestProperty("Content-Type", "text/x-java-source");
        http.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(http.getOutputStream())) {
            //wr.write(msg.getMessageContents(), 0, msg.getMessageContents().length-100);
            wr.writeBytes(validClass);
            wr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }



        //receive response
        int responseCode = 0;
        try {
            responseCode = http.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader in = null;

        if (responseCode != 400) {
            try {
                in = new BufferedReader(new InputStreamReader(http.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            in = new BufferedReader(new InputStreamReader(http.getErrorStream()));

        String line = null;
        StringBuilder responseString = new StringBuilder();

        while (true) {
            try {
                if (!((line = in.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            responseString.append(line);
            if (responseCode == 400) {
                responseString.append("\n");
            }
        }
        System.out.printf("Response: %s\n", responseString.toString());
        latch.countDown();
    }
}
