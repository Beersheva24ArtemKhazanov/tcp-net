package telran.net;

import java.net.*;

public class TCPServer implements Runnable {
    Protocol protocol;
    int port;
    public TCPServer(Protocol protocol, int port) {
        this.protocol = protocol;
        this.port = port;
    }
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on the port: " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                var session = new TCPClientServerSession(protocol, socket);
                Thread thread = new Thread(session);
                thread.start();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
