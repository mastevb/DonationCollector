package map;

import org.apache.log4j.Logger;
import org.elasticsearch.common.geo.GeoPoint;
import org.json.JSONObject;

import external.GeoCodingClient;

public class Converter {
	private static Logger logger = Logger.getLogger(Converter.class);
    // converts JSONObject to GeoPoint
    public static GeoPoint getGeoPointFromObj(String address) {
        JSONObject loc = GeoCodingClient.getLocation(address.replaceAll(" ", "%20"));
        return new GeoPoint(loc.getFloat("lat"), loc.getFloat("lng"));
    }
    
    // converts String to GeoPoint
    public static GeoPoint getGeoPointFromStr(String address) {
        if (address == null) {
            return null;
        }
        address = address.replaceAll(" ", "+");
        JSONObject loc = GeoCodingClient.getLocation(address);
        return new GeoPoint(loc.getFloat("lat"), loc.getFloat("lng"));
    }
}