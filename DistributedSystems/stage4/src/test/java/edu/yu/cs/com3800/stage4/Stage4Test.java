package edu.yu.cs.com3800.stage4;

import edu.yu.cs.com3800.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;
import org.junit.Test;
import edu.yu.cs.com3800.stage4.Client.*;

public class Stage4Test {

    private String validClass = "package edu.yu.cs.fall2019.com3800.stage4;\n\npublic class DaleBoca\n{\npublic String run()\n {\nreturn\"Dale Boooca\";\n}\n}\n";

    private int[] ports = {8010, 8020, 8030, 8040, 8050, 8060, 8070, 8080};
    private ArrayList<PeerServer> servers;
    private GatewayServer gatewayServer;

    private void createServers() throws IOException {

        HashMap<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>(8);
        for (int i = 0; i < ports.length - 1; i++) {
            peerIDtoAddress.put((long) i, new InetSocketAddress("localhost", ports[i]));
        }

        peerIDtoAddress.put(8080L, new InetSocketAddress("localhost", 8080));

        gatewayServer = new GatewayServer(8888, 8080, 0, 8080L, new ConcurrentHashMap<>(peerIDtoAddress), 1);
        gatewayServer.start();

        servers = new ArrayList<>();
        for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
            if (entry.getKey() == 8080L) continue;

            HashMap<Long, InetSocketAddress> map = new HashMap<>(peerIDtoAddress);
            map.remove(entry.getKey());
            PeerServerImpl server = new PeerServerImpl(entry.getValue().getPort(), 0, entry.getKey(), map, 8080L, 1);
            servers.add(server);
            new Thread(server, "Server on port " + server.getAddress().getPort()).start();
        }

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopServers() {
        gatewayServer.stop();
        for (PeerServer server : servers) {
            server.shutdown();
        }
    }

    @Test
    public void test1() throws IOException {
        createServers();

        ClientImpl cliente = new ClientImpl("localhost", 8888);
        cliente.sendCompileAndRunRequest(validClass);

        Response response = cliente.getResponse();
        assertEquals(200, response.getCode());
        assertEquals("Dale Boooca", response.getBody());
        assertFalse(response.getCached());

        stopServers();
    }

    @Test
    public void caching() throws IOException {
        createServers();

        ClientImpl cliente = new ClientImpl("localhost", 8888);
        cliente.sendCompileAndRunRequest(validClass);

        Response response1 = cliente.getResponse();
        assertFalse(response1.getCached());

        cliente.sendCompileAndRunRequest(validClass);
        Response response2 = cliente.getResponse();
        assertTrue(response2.getCached());

        stopServers();
    }

    @Test
    public void variosClientes() throws IOException {
        createServers();

        ClientImpl cliente1 = new ClientImpl("localhost", 8888);
        ClientImpl cliente2 = new ClientImpl("localhost", 8888);
        ClientImpl cliente3 = new ClientImpl("localhost", 8888);
        ClientImpl cliente4 = new ClientImpl("localhost", 8888);

        String clase1 = validClass.replace("Dale Boooca", "BOCA 1");
        String calse2 = validClass.replace("Dale Boooca", "BOCA 2");
        String calse3 = validClass.replace("Dale Boooca", "BOCA 3");
        String calse4 = validClass.replace("Dale Boooca", "BOCA 4");

        cliente1.sendCompileAndRunRequest(clase1);
        cliente2.sendCompileAndRunRequest(calse2);
        cliente3.sendCompileAndRunRequest(calse3);
        cliente4.sendCompileAndRunRequest(calse4);

        assertEquals("BOCA 1", cliente1.getResponse().getBody());
        assertEquals("BOCA 2", cliente2.getResponse().getBody());
        assertEquals("BOCA 3", cliente3.getResponse().getBody());
        assertEquals("BOCA 4", cliente4.getResponse().getBody());

        stopServers();
    }


    @Test
    public void concurrentRequests() throws InterruptedException, IOException {
        createServers();

        int requesrs = 10;
        CountDownLatch latch = new CountDownLatch(requesrs);
        Set<String> respuestas = Collections.synchronizedSet(new HashSet<>());

        for (var i = 0; i < requesrs; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    ClientImpl cliente = new ClientImpl("localhost", 8888);
                    String code = validClass.replace("Dale Boooca", "BOCA:  " + index);
                    cliente.sendCompileAndRunRequest(code);
                    respuestas.add(cliente.getResponse().getBody());
                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        latch.await(30, TimeUnit.SECONDS);
        assertEquals(requesrs, respuestas.size());

        stopServers();
    }
}