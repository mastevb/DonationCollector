package external;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import map.GoogleMapUtil;

import org.json.JSONArray;

public class GeoCodingClient {
	private static final String URL_TEMPLATE = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";


    public static JSONObject getLocation(String address) {
        try {
            address = URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = String.format(URL_TEMPLATE, address, GoogleMapUtil.API_KEY);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        // Create a custom response handler
        ResponseHandler<JSONObject> responseHandler = new ResponseHandler<JSONObject>() {

            @Override
            public JSONObject handleResponse(
                    final HttpResponse response) throws IOException {
                if (response.getStatusLine().getStatusCode() != 200) {
                    return new JSONObject();
                }
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return new JSONObject();
                }
                String responseBody = EntityUtils.toString(entity);
                return getLocation(new JSONObject(responseBody));
            }
        };

        try {
            return httpclient.execute( new HttpGet(url), responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    private static JSONObject getLocation(JSONObject object) throws JSONException {
        // results(JSONArray) -> geometry(JSONObject) -> 0: location(JSONObject)
        JSONArray results = object.getJSONArray("results");
        return results.getJSONObject(0);
    }
}
