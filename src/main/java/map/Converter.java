package map;

import org.elasticsearch.common.geo.GeoPoint;
import org.json.JSONObject;

import external.GeoCodingClient;

public class Converter {
	// converts JSONObject to String
    public static String getAddressStr(JSONObject address) {
        if (address == null) {
            return (String)null;
        }
        String addr = address.getString("address");
        String city = address.getString("city");
        String state = address.getString("state");
        String zip = address.getString("zip");
        return addr + ", " + city + ", " + state + " " + zip;
    }

    // converts JSONObject to GeoPoint
    public static GeoPoint getGeoPoint(JSONObject address) {
        if (address == null) {
            return null;
        }
        String addr = address.getString("address") + " "
                + address.getString("city") + " "
                + address.getString("state");
        JSONObject loc = GeoCodingClient.getLocation(addr.replaceAll(" ", "%20"));
        return new GeoPoint(loc.getFloat("lat"), loc.getFloat("lng"));
    }
}
