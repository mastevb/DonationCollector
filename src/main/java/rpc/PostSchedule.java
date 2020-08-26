package rpc;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.json.JSONObject;

import entity.Schedule;
import esDB.DBConnection;
import external.CognitoClient;

/**
 * Servlet implementation class PostSchedule
 */
public class PostSchedule extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PostSchedule.class);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PostSchedule() {
        super();
        // TODO Auto-generated constructor stub
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//verify token
		String tokenStr = CognitoClient.getTokenFromRequest(request);
		if (!CognitoClient.verifyJwt(tokenStr)) {
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
			return;
		}
		
		//build schedule obj
		Schedule schedule = null;
		
		schedule = RpcHelper.parsePostSchedule(request);
		
		DBConnection connection = new DBConnection();
		
		//update each item within ItemIdList with ngo name, schedule time, status,
		List<String> items = schedule.getItemIDList();
		String scheduleId = schedule.getScheduleID();
		String scheduleTime = schedule.getScheduleTime(); 
		String NGOID = schedule.getNGOID();
		if(!connection.updateItems(items, scheduleId, scheduleTime, NGOID)) {
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
		}
		
		//save to db
		if (connection.indexSchedule(schedule)) {
            logger.info("Successfully saved the item in DB.");
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
        } else {
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
        }
	}

}
