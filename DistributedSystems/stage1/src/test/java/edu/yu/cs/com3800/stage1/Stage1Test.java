package edu.yu.cs.com3800.stage1;

import edu.yu.cs.com3800.SimpleServer;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Stage1Test {

    @Test
    public void test1() throws IOException {
        String java = "package edu.yu.cs.com3800; public class Matusa { public Matusa(){ } public String run(){  return \"dale booooca\"; }}";
        Client client = new ClientImpl("localhost", 8000);
        SimpleServer simpleServer = new SimpleServerImpl(8000);
        simpleServer.start();
        client.sendCompileAndRunRequest(java);
        Client.Response response = client.getResponse();
        System.out.println("Expected response: dale booooca");
        System.out.println("Acual response: " + response.getBody());
        assertEquals(response.getBody(), "dale booooca");
        assertEquals(200, response.getCode());
        simpleServer.stop();
    }

    @Test
    public void test2() throws IOException{

        String java2 = "package edu.yu.cs.com3800; public class Matusa { public Matusa(){ } public String run(){  return \"dale boca\"; }}";

        Client client = new ClientImpl("localhost", 1111);
        SimpleServer simpleServer = new SimpleServerImpl(1111);
        simpleServer.start();
        client.sendCompileAndRunRequest(java2);
        Client.Response response = client.getResponse();
        System.out.println("Expected response: dale boca");
        System.out.println("Acual response: " + response.getBody());
        assertEquals(200, response.getCode());
        simpleServer.stop();
    }

    @Test
    public void test3() throws IOException{

        String java = "package edu.yu.cs.com3800; public class Matusa { public Matusa(){ } public String sss(){  return \"dale booooca\"; }}";

        Client client = new ClientImpl("localhost", 1111);
        SimpleServer simpleServer = new SimpleServerImpl(1111);
        simpleServer.start();
        client.sendCompileAndRunRequest(java);
        Client.Response response = client.getResponse();
        assertEquals(400, response.getCode());
        simpleServer.stop();
    }

}
