package edu.yu.cs.com3800.stage4;

import java.io.*;
import java.net.*;

public class ClientImpl implements Client {
    private final String hostName;
    private final int hostPort;
    private Response response;

    public ClientImpl(String hostName, int hostPort) {
        this.hostName = hostName;
        this.hostPort = hostPort;
    }

    @Override
    public void sendCompileAndRunRequest(String src) throws IOException {
        URL url = new URL("http", hostName, hostPort, "/compileandrun");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setRequestProperty("Content-Type", "text/x-java-source");
        http.setDoOutput(true);

        try (OutputStream os = http.getOutputStream()) {
            os.write(src.getBytes());
            os.flush();
        }

        int responseCode = http.getResponseCode();
        String cachedHeader = http.getHeaderField("Cached-Response");
        boolean isCached = cachedHeader != null && cachedHeader.equals("true");

        InputStream inputStream = (responseCode >= 400) ?
                http.getErrorStream() :
                http.getInputStream();

        String responseBody = readResponseBody(inputStream);
        this.response = new Response(responseCode, responseBody, isCached);
        http.disconnect();
    }

    private String readResponseBody(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                if (this.response != null && this.response.getCode() == 400) {
                    response.append("\n");
                }
            }
            return response.toString();
        }
    }

    @Override
    public Response getResponse() throws IOException {
        return response;
    }
}