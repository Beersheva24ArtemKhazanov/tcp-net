package telran.net;

import org.json.JSONObject;
import static telran.net.TCPConfigurationProperties.*;

public interface Protocol {
    Response getResponse(Request request);

    default String getResponseWithJSON(String requestJSON) {
        JSONObject json = new JSONObject(requestJSON);
        String requestType = json.getString(REQUEST_TYPE_FIELD);
        String requestData = json.getString(REQUEST_DATA_FIELD);
        Request req = new Request(requestType, requestData);
        return getResponse(req).toString();
    };
}
