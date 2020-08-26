package rpc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import entity.Schedule;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import esDB.DBConnection;

/**
 * Servlet implementation class PostItem
 */
@MultipartConfig
public class MySchedule extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(PostItem.class);
    // TODO: Update the upload path according to the server
    private static final String uploadPath = "D:\\LaiOffer\\Project_Class\\Apache-Tomcat\\apache-tomcat-9.0.37\\webapps\\data\\";

    /**
     * @see HttpServlet#HttpServlet()
     */
    public MySchedule() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userName = RpcHelper.getUsername(request);
        if (userName == null) {
            response.setStatus(403);
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
            return;
        }
        DBConnection conn = new DBConnection();
        List<Schedule> schedules = conn.getSchedule(userName);
        JSONArray array = new JSONArray();
        for (Schedule s : schedules) {
            array.put(s.toJSONObject());
        }
        RpcHelper.writeJsonArray(response, array);
    }
}
