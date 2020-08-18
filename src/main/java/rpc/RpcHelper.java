package rpc;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;
import map.Converter;

public class RpcHelper {
	// Writes a JSONArray to http response.
    public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException{
        response.setContentType("application/json");
        response.getWriter().print(array);
    }

    // Writes a JSONObject to http response.
    public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException{
        response.setContentType("application/json");
        response.getWriter().print(obj);
    }

    // Converts a JSON object to Item object
    public static Item parsePostItem(JSONObject postItem) throws ParseException {
        if (postItem == null) {
            return (Item)null;
        }
        ItemBuilder builder = new ItemBuilder();

        builder.setName(postItem.getString("name"));
        builder.setDescription(postItem.getString("description"));
        builder.setImageUrl(postItem.getString("image_link"));
        // TODO: schedule pick time based on the time posted by the resident
        // builder.setPostTime(new SimpleDateFormat("MM/dd/yyyy").parse(postItem.getString("post_date"));

        // generated
        builder.setItemID(idGenerator());
        // from Cognito
        builder.setResidentID(getUsername());

        JSONObject address = postItem.getJSONObject("address");
        builder.setAddress(Converter.getAddressStr(address));
        JSONObject location = postItem.getJSONObject("location");
        builder.setLocation(Converter.getGeoPoint(location));

        builder.setNGOID("");
        builder.setScheduleID("");
        builder.setScheduleTime(new Date());
        builder.setStatus(0);

        return builder.build();
    }

    public static String idGenerator() {
        char[] id = new char[18];
        Random rand1 = new Random();
        for (int i = 0; i < id.length; i++) {
            id[i] = (char)(rand1.nextInt(10) + '0');
        }
        Random rand2 = new Random();
        for (int i = id.length - 1; i > 0; i--) {
            int index = rand2.nextInt(i + 1);
            // Simple swap
            char a = id[index];
            id[index] = id[i];
            id[i] = a;
        }
        return new String(id);
    }

    public static String getUsername() {
        // get username from Cognito
        return "";
    }
}
