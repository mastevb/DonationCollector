package s3;

import com.amazonaws.regions.Regions;

public class S3Util {
	public static final Regions REGIONS = Regions.US_EAST_2;
	public static final String BUCKET_NAME = "donationcollector";
	public static final String ACCESS_KEY = "AKIAJCTUDZVITZVNMGNA";
	public static final String SECRET_KEY = "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";
	// Requests that are pre-signed by SigV4 algorithm are valid for at most 7 days.
	public static final int VALID_PERIOD = 6; // Days
}
