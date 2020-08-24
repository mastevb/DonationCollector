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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpRequest;
import org.apache.log4j.Logger;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;
import entity.Schedule;
import entity.Schedule.ScheduleBuilder;
import map.Converter;
import s3.S3Client;

public class RpcHelper {
	private static Logger logger = Logger.getLogger(RpcHelper.class);
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
    	// Parse multipart content
    	DiskFileItemFactory factory = new DiskFileItemFactory();   	   
        // maximum size that will be stored in memory
        factory.setSizeThreshold(MAX_MEM_SIZE);    
        // Location to save data that is larger than maxMemSize.
        factory.setRepository(new File(TEMP_PATH));
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);    
        // maximum file size to be uploaded.
        upload.setSizeMax(MAX_FILE_SIZE);
        
        ItemBuilder builder = new ItemBuilder();
        String[] address = new String[3];
        String zip = "", fieldName = "", fileName = "", name = "";
        File file = null;
        
        // Parse the request to get file items.
        List<FileItem> fileItems = null;
		try {
			fileItems = upload.parseRequest(request);
		} catch (FileUploadException e2) {
			logger.error("Failed to parse the upload request.");
		}  	
        // Process the uploaded file items
        Iterator<FileItem> i = fileItems.iterator();
            
        while (i.hasNext()) {     
	        FileItem fi = (FileItem)i.next();
	        fieldName = fi.getFieldName();	        
			if (fieldName.equals("image")) {
				fileName = fi.getName();       
	            fileName = fileName.lastIndexOf("\\") >= 0 ? 
	            		filePath + fileName.substring(fileName.lastIndexOf("\\")) : 
	        			    filePath + fileName.substring(fileName.lastIndexOf("\\") + 1);
		        file = new File(fileName);
		        if (!file.exists()) {
		        	try {
						file.createNewFile();
						logger.info("Successfully created temp file.");
					} catch (IOException e) {
						logger.error("Failed to create temp file.");
					}
		        }	        	
		        try {	        	    
		        	fi.write(file);
		        	logger.info("Successfully uploaded Filename: " + fileName + " to the server.");
		        } catch (Exception e) {
		        	logger.error("Failed to write file to " + fileName + ".");
		        }
			} else if (fieldName.equals("name")) {
				name = fi.getString("ascii");
	        	builder.setName(name);
			} else if (fieldName.equals("description")) {
				builder.setDescription(fi.getString("ascii"));
			} else if (fieldName.equals("address")) {
				address[0] = fi.getString("ascii");
			} else if (fieldName.equals("city")) {
				address[1] = fi.getString("ascii");
			} else if (fieldName.equals("state")) {
				address[2] = fi.getString("ascii");
			} else if (fieldName.equals("zip")) {
				zip = fi.getString("ascii");
			} else {
				logger.error("Get invalid field name.");
			}
        }
        
        Date postTime = null;
		try {
			postTime = getCurDate();
		} catch (ParseException e1) {
			logger.error("Failed to get the post time");
			e1.printStackTrace();
		}
		builder.setPostTime(postTime);

        String id = idGenerator();
        builder.setItemID(id);
        
        S3Client s3Client = new S3Client();        
        try {
			builder.setImageUrl(s3Client.putObject(file, id, name, postTime));
		} catch (IOException e) {
			builder.setImageUrl("");
			e.printStackTrace();	
		}
        
        builder.setResidentID(getUsername());
        
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
    public static String getUsername() {
    	
        return "";
    }
    
    // Get current date
    public static Date getCurDate() throws ParseException {
    	ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of( "America/Los_Angeles" ));
    	String time = zdt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    	return new SimpleDateFormat("MM/dd/yyyy").parse(time);
    }
}
