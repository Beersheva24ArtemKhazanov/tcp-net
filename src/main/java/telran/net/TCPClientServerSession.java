package telran.net;

import java.net.*;
import java.io.*;

public class TCPClientServerSession implements Runnable {
    Protocol protocol;
    Socket socket;

    public TCPClientServerSession(Protocol protocol, Socket socket) {
        this.protocol = protocol;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream writer = new PrintStream(socket.getOutputStream())) {
            String request = "";
            while ((request = reader.readLine()) != null) {
                String response = protocol.getResponseWithJSON(request);
                writer.println(response);
            }
            socket.close();
        } catch (Exception e) {
            System.out.println("Server is not working because of " + e);
        }
    }

}
