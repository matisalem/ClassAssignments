package edu.yu.cs.com3800.stage1;

import java.io.*;
import java.net.*;

public class ClientImpl implements Client{

    String hostName;
    int hostPort;
    HttpURLConnection http;
    Response response;

    public ClientImpl(String hostName, int hostPort) throws MalformedURLException {
        this.hostName = hostName;
        this.hostPort = hostPort;
    }

    @Override
    public void sendCompileAndRunRequest(String src) throws IOException {

        http = (HttpURLConnection) new URL("http", hostName, hostPort, "/compileandrun").openConnection();

        http.setRequestMethod("POST");
        http.setRequestProperty("Content-Type", "text/x-java-source");
        http.setDoOutput(true);

        try (OutputStream os = http.getOutputStream()) {
            byte[] input = src.getBytes();
            os.write(input, 0, input.length);
            os.flush();
        }

        int responseCode = http.getResponseCode();
        InputStream input;

        if (responseCode != 400) {
            input = http.getInputStream();
        } else {
            input = http.getErrorStream();
        }

        String responseBody;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
            StringBuilder finalString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                finalString.append(line);
                if(responseCode == 400){
                    finalString.append("\n");
                }
            }
            responseBody = finalString.toString();
        }

        this.response = new Response(responseCode, responseBody);
    }

    @Override
    public Response getResponse() throws IOException {
        return response;
    }
}
