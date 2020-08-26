package s3;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3Client {	
	private static AmazonS3 s3Client;
	private static Logger logger = Logger.getLogger(S3Client.class);
	
	public S3Client() {
		AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
        	logger.error("Fail to get AWS credentials.");
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        s3Client = new AmazonS3Client(credentials);
        //Region region = Region.getRegion(S3Util.REGIONS);
        //s3Client.setRegion(region);
        logger.info("Successfully built S3 client.");
	}
	
	public String putObject(File file, String key, String title, Date postTime) throws IOException {
		String imageUrl = null;		
		if (file == null) {
			logger.error("No input image.");
			return imageUrl;
		}
		
		try {			
            // Upload a file as a new object with ContentType and title specified.
			// TODO: modify image upload
            PutObjectRequest request = new PutObjectRequest(S3Util.BUCKET_NAME, key, file);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("plain/txt");
            metadata.addUserMetadata("title", title);
            request.setMetadata(metadata);
            s3Client.putObject(request);
            logger.info("Successfully saved the image to S3.");
            
            // Request image URL
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(  
            		S3Util.BUCKET_NAME, key);           
            
            // Set expiration date
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(postTime);
            calendar.add(Calendar.DAY_OF_YEAR, S3Util.VALID_PERIOD); 
            urlRequest.setExpiration(calendar.getTime());
            
            // Get image URL
            imageUrl = s3Client.generatePresignedUrl(urlRequest).toString();
        } catch (AmazonServiceException e) {
        	logger.error("Amazon S3 couldn't process the request.");
            e.printStackTrace();
        } catch (SdkClientException e) {
        	logger.error("Amazon S3 couldn't be contacted for a response, "
        			+ "or the client couldn't parse the response from Amazon S3.");
            e.printStackTrace();
        }
		return imageUrl;
	}
	
	public S3ObjectInputStream getObject(String key) {
		S3Object obj = null;
        try {
            obj = s3Client.getObject(new GetObjectRequest(S3Util.BUCKET_NAME, key));
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
        return obj.getObjectContent();
	}
	
	public void deleteObject(String key) {
		try {			
            s3Client.deleteObject(new DeleteObjectRequest(S3Util.BUCKET_NAME, key));
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }	
	}
}
