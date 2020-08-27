package rpc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import esDB.DBConnection;
import external.CognitoClient;

/**
 * Servlet implementation class GetItem
 */
public class GetItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Verify the token
		String tokenStr = CognitoClient.getTokenFromRequest(request);
		if (!CognitoClient.verifyJwt(tokenStr)) {
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
			return;
		}
		
		// Get resident ID
		String residentID = CognitoClient.getContentFromToken(tokenStr, "cognito:username");
    	
		
		// Create DB
		DBConnection connection = new DBConnection();
		
		// Write to response
		List<Item> items = connection.GetItemList(residentID);
		JSONArray array = new JSONArray();
		for (Item item : items) {
			array.put(item.toJSONObject());
		}
		RpcHelper.writeJsonArray(response, array);
	}
	
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

}