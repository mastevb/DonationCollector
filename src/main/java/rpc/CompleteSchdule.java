package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import esDB.DBConnection;

/**
 * Servlet implementation class CompleteSchdule
 */
public class CompleteSchdule extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CompleteSchdule() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String scheduleID;
		//Get update item from parameter
		scheduleID = request.getParameter("schedule_id");
		if(scheduleID==null){
			// Get update item id from request body
			JSONObject input = new JSONObject(IOUtils.toString(request.getReader()));
			scheduleID = input.getString("schedule_id");
		}
		// Create DB
		DBConnection connection = new DBConnection();

		if (scheduleID != null) {
			// Get update response
			boolean responseStatus = connection.MarkCompleteItem(scheduleID);
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
