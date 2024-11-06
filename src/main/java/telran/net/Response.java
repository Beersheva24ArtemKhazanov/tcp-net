package telran.net;

import org.json.JSONObject;
import static telran.net.TCPConfigurationProperties.*;

public record Response(ResponseCode responseCode, String responseData) {
     @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put(RESPONSE_CODE_FIELD, responseCode);
        json.put(RESPONSE_DATA_FIELD, responseData);
        return json.toString();
    }
}
