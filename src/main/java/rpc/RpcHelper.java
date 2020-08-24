package rpc;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;
import entity.Schedule;
import entity.Schedule.ScheduleBuilder;
import map.Converter;
import s3.S3Client;
import external.CognitoClient;

public class RpcHelper {
	private static final Logger logger = Logger.getLogger(RpcHelper.class);
	// The valid period of the URL
	public static final int MAX_FILE_SIZE = 2 * 1024 * 1024;
	// TODO: change the maximum memory size according to the sever
	public static final int MAX_MEM_SIZE = 100 * 1024 * 1024;
	// TODO: change the temp path according to the server
	public static final String TEMP_PATH = "C:\\temp";
	
	// Writes a JSONArray to http response.
    public static void writeJsonArray(HttpServletResponse response, JSONArray array) 
    		throws IOException{
        response.setContentType("application/json");
        response.getWriter().print(array);
    }

    // Writes a JSONObject to http response.
    public static void writeJsonObject(HttpServletResponse response, JSONObject obj) 
    		throws IOException{
        response.setContentType("application/json");
        response.getWriter().print(obj);
    }
    
    // Parses request and get post item
    public static Item parsePostItem(HttpServletRequest request, String filePath) throws UnsupportedEncodingException {
    	ItemBuilder builder = new ItemBuilder();
        String[] address = new String[3];
        String zip = "", fileName = "", name = "";
        
        name = request.getParameter("name");
        builder.setName(name);
        builder.setDescription(request.getParameter("description"));
                        
        Part image = null;
        try {
			image = request.getPart("image");
		} catch (IOException | ServletException e2) {
			logger.error("Fail to get the image from the request.");
		}
        fileName = image.getSubmittedFileName();
        fileName = fileName.lastIndexOf("\\") >= 0 ? 
        		filePath + fileName.substring(fileName.lastIndexOf("\\")) : 
    			    filePath + fileName.substring(fileName.lastIndexOf("\\") + 1);
        try {	        	    
        	image.write(fileName);
        	logger.info("Successfully uploaded Filename: " + fileName + " to the server.");
        } catch (Exception e) {
        	logger.error("Failed to write file to " + fileName + ".");
        }
        
        Date postTime = null;
		try {
			postTime = getCurDate();
		} catch (ParseException e1) {
			logger.error("Failed to get the post time");
		}
		builder.setPostTime(postTime);

        String id = idGenerator();
        builder.setItemID(id);
        
        S3Client s3Client = new S3Client();
        String imageUrl = "";
        try {
			imageUrl = s3Client.putObject(new File(fileName), id, name, postTime);
		} catch (IOException e) {
			logger.error("Failed to get image URL.");
		}
        builder.setImageUrl(imageUrl);
        
        String username = getUsername(request);
        if (username == null) {
        	logger.error("Failed to get username.");
        	return (Item) null;
        }
        builder.setResidentID(username);
        
        address[0] = request.getParameter("address");
        address[1] = request.getParameter("city");
        address[2] = request.getParameter("state");
        zip = request.getParameter("zip");
        String addr = String.join(", ", address);
        builder.setAddress(addr + " " + zip);
        builder.setLocation(Converter.getGeoPointFromStr(addr));
        
        builder.setNGOID("");
        builder.setScheduleID("");
        builder.setScheduleTime(new Date());
        builder.setStatus(0);

        return builder.build();
    }
    
    // Creates new schedule for NGO
    public static Schedule createNewSchedule() {   	
    	ScheduleBuilder builder = new ScheduleBuilder();
        
    	builder.setScheduleID(idGenerator());
        builder.setNGOID("");
        builder.setItemIDList(new ArrayList<String>());
        builder.setScheduleTime(new Date());
        builder.setStatus(0);
        
        return builder.build();
    }

    // Randomly generates 18-digit ID for items and schedules
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
    
    // Gets username from Cognito
    public static String getUsername(HttpServletRequest request) {
    	// Get token in header
    	String username = "";
    	String authorization = request.getHeader("Authorization");
    	String tokenStr = authorization.substring("Bearer ".length());
    	// tokenStr = CognitoClient.generateToken();  // for tests
    	// Decode token
    	username = CognitoClient.getContentFromToken(tokenStr, "sub");
        return username;
    }
    
    // Get current date
    public static Date getCurDate() throws ParseException {
    	ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of( "America/Los_Angeles" ));
    	String time = zdt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    	return new SimpleDateFormat("MM/dd/yyyy").parse(time);
    }
}
