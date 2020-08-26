package s3;

import com.amazonaws.regions.Regions;

public class S3Util {
	public static final Regions REGIONS = Regions.US_WEST_2;
	public static final String BUCKET_NAME = "donationcollector";
	// Requests that are pre-signed by SigV4 algorithm are valid for at most 7 days.
	public static final int VALID_PERIOD = 6; // Days
}
