package telran.net;

import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.*;

public class TCPClientServerSession implements Runnable {
    private static final int SOCKET_TIMEOUT = 1000;
    private static final int MAX_FAILED_RESPONSES = 5;
    private static final int MAX_REQUESTS_PER_SECOND = 10;
    private static final long IDLE_TIMEOUT = 60000;

    Protocol protocol;
    Socket socket;
    AtomicBoolean isShutDown;
    private long idleTime;
    private int failedResponses;
    private int requestsInLastSecond;
    private long lastRequestTime;

    public TCPClientServerSession(Protocol protocol, Socket socket, AtomicBoolean isShutDown) {
        this.protocol = protocol;
        this.socket = socket;
        this.isShutDown = isShutDown;
        this.idleTime = 0;
        this.failedResponses = 0;
        this.requestsInLastSecond = 0;
        this.lastRequestTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream writer = new PrintStream(socket.getOutputStream())) {
            socket.setSoTimeout(SOCKET_TIMEOUT);
            String request = "";
            while (!isShutDown.get()) {
                if (reader.ready() && request != null) {
                    request = reader.readLine();
                    checkCLosingSocket();
                }
                String response = protocol.getResponseWithJSON(request);
                writer.println(response);
                updateActivityMetrics(response);
            }
            socket.close();
        } catch (SocketTimeoutException e) {
            idleTime += SOCKET_TIMEOUT;
            checkCLosingSocket();
        } catch (Exception e) {
            System.out.println("Server is not working because of " + e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateActivityMetrics(String response) {
        idleTime = 0;
        if (response != "OK") {
            failedResponses++;
            if (failedResponses > MAX_FAILED_RESPONSES) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            failedResponses = 0;
        }
    }

    private void checkCLosingSocket() {
        if (isDoSAttack() || idleTime >= IDLE_TIMEOUT) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isDoSAttack() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime > 1000) {
            requestsInLastSecond = 1;
            lastRequestTime = currentTime;
        } else {
            requestsInLastSecond++;
        }
        return requestsInLastSecond > MAX_REQUESTS_PER_SECOND;
    }

}
