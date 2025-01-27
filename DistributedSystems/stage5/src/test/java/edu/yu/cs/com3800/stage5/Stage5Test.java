package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

import org.junit.Test;
import edu.yu.cs.com3800.stage5.Client.*;
import org.junit.jupiter.api.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // Add this annotation
public class Stage5Test {

    @BeforeEach
    void setUp() {
        // Ensure everything is stopped before starting
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        stopServers();

    }

    @AfterEach
    void tearDown() {
        stopServers();
        try {
            // Add a small delay to ensure ports are freed
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private String validClass = "package edu.yu.cs.fall2019.com3800.stage4;\n\npublic class DaleBoca\n{\npublic String run()\n {\nreturn\"Dale Boooca\";\n}\n}\n";

    private int[] ports = {8010, 8020, 8030, 8040, 8050, 8060};
    private ArrayList<PeerServer> servers;
    private GatewayServer gateway;
    private int httpPort = 8888;
    private int gatewayPort = 8000;

    private void createServers() throws IOException {
        HashMap<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>();

        for (int i = 0; i < ports.length; i++) {
            peerIDtoAddress.put((long) (i + 1), new InetSocketAddress("localhost", ports[i]));
        }

        // Create and start gateway
        HashMap<Long, InetSocketAddress> gatewayMap = new HashMap<>(peerIDtoAddress);
        gateway = new GatewayServer(httpPort, gatewayPort, 0, 0L, gatewayMap, 1);
        gateway.start();

        // Create and start peer servers
        servers = new ArrayList<>();
        peerIDtoAddress.put(0L, new InetSocketAddress("localhost", gatewayPort));

        for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
            if (entry.getKey() == 0L) continue;

            HashMap<Long, InetSocketAddress> peerMap = new HashMap<>(peerIDtoAddress);
            peerMap.remove(entry.getKey());
            PeerServerImpl server = new PeerServerImpl(
                    entry.getValue().getPort(),
                    0,
                    entry.getKey(),
                    peerMap,
                    0L,
                    1
            );
            servers.add(server);
            server.start();
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printLeaders() {
        for (PeerServer server : servers) {
            Vote leader = server.getCurrentLeader();
            if (leader != null) {
                System.out.println("Server on port " + server.getAddress().getPort() +
                        " with ID " + server.getServerId() +
                        " has leader: " + leader.getProposedLeaderID() +
                        " and state: " + server.getPeerState().name());
            }
        }
        System.out.println("Gateway leader: " + gateway.getLeader() + " state: " + gateway.getState());
    }

    private Response sendRequest(String code) throws IOException {
        ClientImpl client = new ClientImpl("localhost", httpPort);
        client.sendCompileAndRunRequest(code);
        return client.getResponse();
    }

    private void stopServers() {
        if (gateway != null) {
            gateway.stop();
            gateway = null;
        }
        if (servers != null) {
            for (PeerServer server : servers) {
                if (server != null) {
                    server.shutdown();
                }
            }
            servers.clear();
            servers = null;
        }
    }



        @Test
        @Order(4)
        public void testElectionLikeJudah() throws IOException, InterruptedException {
            createServers();
            printLeaders();
            assertNotNull("Gateway must have a leader", gateway.getLeader());

            // Wait for initial election to complete
            Response initialResponse = sendRequest(validClass);
            while (initialResponse.getCode() != 200) {
                Thread.sleep(1000);
                initialResponse = sendRequest(validClass);
            }

            // Send concurrent requests
            int totalRequests = ports.length * 8;
            CountDownLatch latch = new CountDownLatch(totalRequests);
            Set<String> responses = Collections.synchronizedSet(new HashSet<>());
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3);

            for (var i = 0; i < totalRequests; i++) {
                final int index = i;
                executor.execute(() -> {
                    try {
                        String code = validClass.replace("Dale Boooca", "Dale Boooca " + index);
                        Response response = sendRequest(code);
                        responses.add(response.getBody());
                        latch.countDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            // Wait for some requests to process then kill leader
            while (latch.getCount() > 35) {
                Thread.sleep(100);
            }
            servers.get(servers.size() - 1).shutdown();
            System.out.println("Leader eliminated");

            // Wait for remaining requests
            latch.await();

            // Verify results
            assertTrue("Should have at least 35 responses", responses.size() >= 35);
            printLeaders();
            assertNotNull("Should have new leader after failure", gateway.getLeader());

            executor.shutdown();
            stopServers();
        }



//        @Test
//        @Order(3)
//        public void test1() throws IOException {
//            createServers();
//
//            ClientImpl cliente = new ClientImpl("localhost", 8888);
//            cliente.sendCompileAndRunRequest(validClass);
//
//            Response response = cliente.getResponse();
//            assertEquals(200, response.getCode());
//            assertEquals("Dale Boooca", response.getBody());
//            assertFalse(response.getCached());
//
//            stopServers();
//        }


//        @Test
//        @Order(2)
//        public void variosClientes() throws IOException {
//            createServers();
//
//            ClientImpl cliente1 = new ClientImpl("localhost", 8888);
//            ClientImpl cliente2 = new ClientImpl("localhost", 8888);
//            ClientImpl cliente3 = new ClientImpl("localhost", 8888);
//            ClientImpl cliente4 = new ClientImpl("localhost", 8888);
//
//            String clase1 = validClass.replace("Dale Boooca", "BOCA 1");
//            String calse2 = validClass.replace("Dale Boooca", "BOCA 2");
//            String calse3 = validClass.replace("Dale Boooca", "BOCA 3");
//            String calse4 = validClass.replace("Dale Boooca", "BOCA 4");
//
//            cliente1.sendCompileAndRunRequest(clase1);
//            cliente2.sendCompileAndRunRequest(calse2);
//            cliente3.sendCompileAndRunRequest(calse3);
//            cliente4.sendCompileAndRunRequest(calse4);
//
//            assertEquals("BOCA 1", cliente1.getResponse().getBody());
//            assertEquals("BOCA 2", cliente2.getResponse().getBody());
//            assertEquals("BOCA 3", cliente3.getResponse().getBody());
//            assertEquals("BOCA 4", cliente4.getResponse().getBody());
//
//            stopServers();
//        }




//        @Test
//        @Order(1)
//        public void concurrentRequests() throws InterruptedException, IOException {
//            createServers();
//
//            int requesrs = 10;
//            CountDownLatch latch = new CountDownLatch(requesrs);
//            Set<String> respuestas = Collections.synchronizedSet(new HashSet<>());
//
//            for (var i = 0; i < requesrs; i++) {
//                final int index = i;
//                new Thread(() -> {
//                    try {
//                        ClientImpl cliente = new ClientImpl("localhost", 8888);
//                        String code = validClass.replace("Dale Boooca", "BOCA:  " + index);
//                        cliente.sendCompileAndRunRequest(code);
//                        respuestas.add(cliente.getResponse().getBody());
//                        latch.countDown();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }).start();
//            }
//
//            latch.await(30, TimeUnit.SECONDS);
//            assertEquals(requesrs, respuestas.size());
//
//            stopServers();
//        }
}