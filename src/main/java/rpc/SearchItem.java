package rpc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.elasticsearch.common.geo.GeoPoint;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Schedule;
import esDB.DBConnection;
import external.CognitoClient;
import map.Converter;

/**
 * Servlet implementation class SearchItem
 */
public class SearchItem extends HttpServlet {
	private static final Logger logger = Logger.getLogger(SearchItem.class);
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// Verify the token
		String tokenStr = CognitoClient.getTokenFromRequest(request);
		if (!CognitoClient.verifyJwt(tokenStr)) {
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
			return;
		}
		
		// Get Address from request
		String address = RpcHelper.getAddress(request);
        if (address == null) {
        	logger.error("Got no address.");
        	RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
        	return;
        }
        
        // Convert Address to Geo-Point
        GeoPoint myPoint = Converter.getGeoPointFromStr(address);
        
        System.out.println("lat: " + myPoint.getLat() + "    lon: " + myPoint.getLon());
        DBConnection conn = new DBConnection();
        List<Item> itemList = conn.GetItemListByGeo(myPoint);
        
        JSONArray array = new JSONArray();
        for (Item s : itemList) {
            array.put(s.toJSONObject());
        }
        RpcHelper.writeJsonArray(response, array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}