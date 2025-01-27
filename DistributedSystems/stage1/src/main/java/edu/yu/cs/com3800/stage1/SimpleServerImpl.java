package edu.yu.cs.com3800.stage1;

import com.sun.net.httpserver.*;
import edu.yu.cs.com3800.SimpleServer;
import edu.yu.cs.com3800.JavaRunner;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.logging.*;


public class SimpleServerImpl implements SimpleServer {

    int puerto;
    private HttpServer servidor;
    private Logger logger = Logger.getLogger("SimpleServerImpl");

    public SimpleServerImpl(int port) throws IOException {
        puerto = port;
        servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.createContext("/compileandrun", new CosoHandler());
        servidor.setExecutor(null);
    }

    public static void main(String[] args){
        int port = 9000;
        if(args.length >0) port = Integer.parseInt(args[0]);

        SimpleServer myserver = null;

        try {
            myserver = new SimpleServerImpl(port);
            myserver.start();
        } catch(Exception e){
            System.err.println(e.getMessage());
            myserver.stop();
        }
    }

    @Override
    public void start() {
        servidor.start();
        logger.info("Servidor funciona en el puerto " + puerto);
    }

    @Override
    public void stop() {
        if (servidor != null) {
            servidor.stop(0);
            logger.info("Se freno el servidor");
        }
    }

    private class CosoHandler implements HttpHandler {



        @Override
        public void handle(HttpExchange exchange) throws IOException {

            Logger logger = Logger.getLogger("CosoHandler");
            File path = new File("logs");
            path.mkdir();
            FileHandler fh = new FileHandler("logs/logs.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            logger.info("Handle llamado");

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                logger.info("Mal response type, no es POST");
                return;
            }

            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

            if (contentType == null || !contentType.equalsIgnoreCase("text/x-java-source")) {
                exchange.sendResponseHeaders(400, -1);
                logger.info("Mal request type, no es lo que Judah pide");
                return;
            }

            InputStream i = exchange.getRequestBody();
            String source = new String(i.readAllBytes());
            String result;

            try {
                JavaRunner javaRunner = new JavaRunner();
                InputStream inputStream = new ByteArrayInputStream(source.getBytes());
                result = javaRunner.compileAndRun(inputStream);
                exchange.sendResponseHeaders(200, result.length());
            } catch (Exception e) {
                result = "Error: " + e.getMessage() + "\n" + getStackTraceAsString(e);
                exchange.sendResponseHeaders(400, result.length());
            }

            OutputStream o = exchange.getResponseBody();
            o.write(result.getBytes());
            o.close();
        }

        private String getStackTraceAsString(Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        }
    }
}
