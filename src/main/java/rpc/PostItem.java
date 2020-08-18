package rpc;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.rest.RestStatus;
import org.json.JSONObject;

import entity.Item;
import esDB.DBConnection;

/**
 * Servlet implementation class PostItem
 */

public class PostItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
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
		HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(403);
            return;
        }

        JSONObject input = new JSONObject(IOUtils.toString(request.getReader()));
        Item item = null;

        try {
            item = RpcHelper.parsePostItem(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DBConnection connection = new DBConnection();

        if (item != null) {
            IndexResponse indexResponse = connection.indexItem(item);
            if (indexResponse != null && indexResponse.status() == RestStatus.OK) {
                RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
            } else {
                RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
            }
        } else {
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", "FAILED"));
        }
	}

}
