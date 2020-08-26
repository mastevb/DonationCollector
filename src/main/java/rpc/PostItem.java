package rpc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;

import entity.Item;
import esDB.DBConnection;

import external.CognitoClient;

/**
 * Servlet implementation class PostItem
 */
@MultipartConfig
public class PostItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PostItem.class);
	// TODO: Update the upload path according to the server
	private static final String uploadPath = "D:\\LaiOffer\\Project_Class\\Apache-Tomcat\\apache-tomcat-9.0.37\\webapps\\data\\";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PostItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Verify the request type
		if(!ServletFileUpload.isMultipartContent(request)) {
			logger.error("Not a multipart content.");
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
			return;
		}
		
		// Verify the token
		String tokenStr = CognitoClient.getTokenFromRequest(request);
		if (!CognitoClient.verifyJwt(tokenStr)) {
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
			return;
		}
		
		// Parse request content and get post item
		Item item = null;		
		item = RpcHelper.parsePostItem(request, uploadPath);
        if (item == null) {
        	logger.error("Got no item.");
        	RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
        	return;
        }
               
        DBConnection connection = new DBConnection();
        logger.info("Successfully established DB connection.");
        
        // Index item to DB
        if (connection.indexItem(item)) {
            logger.info("Successfully saved the item in DB.");
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
        } else {
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
        }
	}
}
