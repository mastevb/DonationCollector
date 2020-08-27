package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.rest.RestStatus;
import org.json.JSONObject;

import esDB.DBConnection;
import external.CognitoClient;

/**
 * Servlet implementation class DeleteItem
 */
public class DeleteItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String tokenStr = CognitoClient.getTokenFromRequest(request);
		if (!CognitoClient.verifyJwt(tokenStr)) {
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
			return;
		}
		
		
		String itemID;
		//Get delete item from parameter
		itemID = request.getParameter("item_id");
		if(itemID==null) {
			//Crate delete item id
			JSONObject input = new JSONObject(IOUtils.toString(request.getReader()));
			itemID = input.getString("item_id");
		}
		//Create DB
		DBConnection connection = new DBConnection();
		
		if (itemID != null) {
			//Get delete response
            boolean responseStatus = connection.deleteDonorItem(itemID);
    		if (responseStatus) {
    			response.getWriter().append("Succeed at: ").append(request.getContextPath());
    		} else {
    			response.getWriter().append("Failed at: ").append(request.getContextPath());
    		}
        } else {
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
        }
	}

}