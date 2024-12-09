package telran.net;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TCPServer implements Runnable {
    Protocol protocol;
    int port;
    ExecutorService executor;
    AtomicBoolean isShutDown;

    public TCPServer(Protocol protocol, int port) {
        this.protocol = protocol;
        this.port = port;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.isShutDown = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(1000);
            System.out.println("Server is listening on the port: " + port);
            while (!isShutDown.get()) {
                try {
                    Socket socket = serverSocket.accept();
                    var session = new TCPClientServerSession(protocol, socket, isShutDown);
                    executor.execute(session);
                } catch (SocketTimeoutException e) {
                    
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            shutDown();
        }
    }

    public void shutDown() {
        isShutDown.set(true);
        executor.shutdownNow();
    }

}
