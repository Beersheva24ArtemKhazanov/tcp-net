package telran.net;

import java.io.*;
import java.net.*;
import java.time.Instant;

import org.json.JSONObject;

import static telran.net.TCPConfigurationProperties.*;

public class TCPClient implements Closeable {
    Socket socket;
    PrintStream writer;
    BufferedReader reader;
    int interval;
    int nTrials;
    String host;
    int port;

    public TCPClient(String host, int port, int interval, int nTrials) {
        this.interval = interval;
        this.port = port;
        this.host = host;
        this.nTrials = nTrials;
        connect();
    }

    public TCPClient(String host, int port) {
        this(host, port, DEFAULT_INTERVAL_CONNECTION, DEFAULT_TRIALS_NUMBER_CONNECTION);
    };

    private void connect() {
        int count = nTrials;
        do {
            try {
                socket = new Socket(host, port);
                writer = new PrintStream(socket.getOutputStream());
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                count = 0;
            } catch (IOException e) {
                waitForInterval();
                count--;
            }
        } while (count != 0);
    }

    private void waitForInterval() {
        Instant finish = Instant.now().plusMillis(interval);
        while(Instant.now().isBefore(finish));
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public String sendAndReceive(String requestType, String requestData) {
        Request request = new Request(requestType, requestData);
        try {
            writer.println(request);
            String responseJSON = reader.readLine();
            JSONObject json = new JSONObject(responseJSON);
            ResponseCode responseCode = json.getEnum(ResponseCode.class, RESPONSE_CODE_FIELD);
            String responseData = json.getString(RESPONSE_DATA_FIELD);
            if (responseCode != responseCode.OK) {
                throw new RuntimeException(responseData);
            }
            return responseData;
        } catch (IOException e) {
            throw new RuntimeException("Server is unavailable");
        }
    }

}
