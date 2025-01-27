package edu.yu.cs.com3800.stage5;

import static org.junit.jupiter.api.Assertions.*;


import java.io.IOException;


import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import edu.yu.cs.com3800.PeerServer;
import edu.yu.cs.com3800.PeerServer.ServerState;



class Stage4Test {

    @Nested
    class Stage5ArchitectureTest {
        HashMap<Long, InetSocketAddress> peerIDtoAddress;
        List<PeerServer> servers = new ArrayList();
        int httpPort = 8888;
        private GatewayServer gatewayServer;



        String validClass = "package edu.yu.cs.fall2019.com3800.stage1;\n\npublic class HelloWorld\n{\n    public String run()\n    {\n        return \"Hello world!\";\n    }\n}\n";

        public Stage5ArchitectureTest() throws IOException, InterruptedException {
            int numObservers = 1;
            peerIDtoAddress = new HashMap<>(3);
            peerIDtoAddress.put(1L, new InetSocketAddress("localhost", 8010));
            peerIDtoAddress.put(2L, new InetSocketAddress("localhost", 8020));
            peerIDtoAddress.put(3L, new InetSocketAddress("localhost", 8030));
            peerIDtoAddress.put(4L, new InetSocketAddress("localhost", 8040));
            peerIDtoAddress.put(5L, new InetSocketAddress("localhost", 8050));
            peerIDtoAddress.put(8L, new InetSocketAddress("localhost", 1234));
            peerIDtoAddress.put(0L, new InetSocketAddress("localhost", 8080));


        }


        void init() throws IOException, InterruptedException{

            this.gatewayServer = new GatewayServer(8888, 8080, 0, 0L, new ConcurrentHashMap<>(peerIDtoAddress), 1);


            this.gatewayServer.start();

            for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
                HashMap<Long, InetSocketAddress> map = (HashMap<Long, InetSocketAddress>) peerIDtoAddress.clone();
                map.remove(entry.getKey());
                if(entry.getKey() == 0L) {
                    continue;
                }

                PeerServer server = new PeerServerImpl(entry.getValue().getPort(), 0, entry.getKey(), map, 0L, 1); //TODO find out how gatewayid works


                this.servers.add(server);

                new Thread((PeerServerImpl) server, "Server on port " + server.getAddress().getPort()).start();
            }
            Thread.sleep(5000);

        }

        @Test
        void followerFailsDuringLongRequest() throws Exception{
            //Verify the Leader Election
            init();

            long leaders = this.servers
                    .stream()
                    .filter(a -> a.getPeerState() == ServerState.LEADING)
                    .count();

            //Pause until election is over and every server has started\
            int tries = 0;
            while( tries < 20){
                Thread.sleep(1000);
                leaders = this.servers
                        .stream()
                        .filter(a -> a.getCurrentLeader() != null)
                        .count();
                tries++;
            }

            leaders = this.servers
                    .stream()
                    .filter(a -> a.getPeerState() == ServerState.LEADING)
                    .count();

            assertEquals(leaders,1); //Make sure we have one leader


            int close = IntStream.range(0, this.servers.size())
                    .filter(i -> this.servers.get(i).getPeerState() == ServerState.LEADING)
                    .findFirst().orElseThrow(() -> new IllegalStateException("No leader found"));;


            //check gateway has the correct leader


            assertEquals(gatewayServer.getLeader(),servers.get(close).getServerId());


            String validClass = "package edu.yu.cs.fall2019.com3800.stage1;\n\n" +
                    "public class LongRunningTask {\n" +
                    "    public String run() {\n" +
                    "        try {\n" +
                    "            Thread.sleep(10000);\n" +
                    "        } catch (InterruptedException e) {\n" +
                    "            Thread.currentThread().interrupt();\n" +
                    "        }\n" +
                    "        return \"Task Complete\";\n" +
                    "    }\n" +
                    "}\n";

            ClientImpl client = new ClientImpl("localhost", httpPort);


            //Since client sendCompileAndRunRequest blocks, we use another thread to shutdown the server while the req is processing
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    Thread.sleep(3000);
                    // Shutdown the server fulfilling the request
                    servers.get(0).shutdown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Task was interrupted");
                }
            });

            executor.shutdown();
            client.sendCompileAndRunRequest(validClass);


            assertEquals("Task Complete", client.getResponse().getBody());

        }



        @Test
        void shutdownLeaderGateWayShouldQueueUntilNewLeaderTest() throws Exception{

            init();
            long leaders = this.servers
                    .stream()
                    .filter(a -> a.getPeerState() == ServerState.LEADING)
                    .count();

            //Pause until election is over and every server has started\
            int tries = 0;
            while(tries < 50){
                Thread.sleep(1000);
                leaders = this.servers
                        .stream()
                        .filter(a ->  a.getPeerState() == ServerState.LEADING)
                        .count();
                tries++;
            }

            leaders = this.servers
                    .stream()
                    .filter(a -> a.getPeerState() == ServerState.LEADING)
                    .count();

            assertEquals(leaders,1); //Make sure we have one leader

            int close = IntStream.range(0, this.servers.size())
                    .filter(i -> this.servers.get(i).getPeerState() == ServerState.LEADING)
                    .findFirst().orElseThrow(() -> new IllegalStateException("No leader found"));;

            assertEquals(gatewayServer.getLeader(),servers.get(close).getServerId());
            Thread.sleep(1000);

            this.servers.get(this.servers.size()-1).shutdown();
            Thread.sleep(60000);
            System.out.println("SLeep over");
            assert(gatewayServer.getLeader()!= servers.get(close).getServerId());
            ClientImpl cli = new ClientImpl("localhost", httpPort);
            Thread.sleep(1000);
            cli.sendCompileAndRunRequest(validClass);
            assertEquals(cli.getResponse().getBody(), "Hello world!");
        }



        @AfterEach
        void tearDown() throws InterruptedException {
            Thread.sleep(2000);


            this.gatewayServer.stop();


            for(PeerServer server: servers) {

                server.shutdown();
            }

            Thread.sleep(6000);
        }





    }
}