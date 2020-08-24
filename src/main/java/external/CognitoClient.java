package external;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.RSAKeyProvider;

public class CognitoClient {
	private static final Logger logger = Logger.getLogger(CognitoClient.class);
	private static final RSAKeyProvider keyProvider = new AwsCognitoRSAKeyProvider(CognitoUtil.REGION, CognitoUtil.USER_POOLS_ID);
	private static final Algorithm algorithm = Algorithm.RSA256(keyProvider);
	
	// Gets specified content from token string
	public static String getContentFromToken(String tokenStr, String key) {
	    // This line will throw an exception if it is not a signed JWS (as expected)
	    String content = null;        
        JWTVerifier jwtVerifier = JWT.require(algorithm)
                //.withAudience("2qm9sgg2kh21masuas88vjc9se") // Validate your apps audience if needed
                .build();
        try {
            DecodedJWT varify = jwtVerifier.verify(tokenStr);
            logger.info("Token is verified");
            Base64 base64Url = new Base64(true);
            String header = new String(base64Url.decode(varify.getHeader()));
            logger.info("JWT Header : " + header);
            String body = new String(base64Url.decode(varify.getPayload()));
            logger.info("JWT Body : " + body);
            try {
                JSONObject bodyObject = new JSONObject(body);
                content = bodyObject.getString(key);
                logger.info("Successfully got " + key + " = " + content);
            } catch (JSONException err) {
                logger.error("Failed to get " + key + " from token.");
            }
        }catch (JWTVerificationException e){
        	logger.info("Token is wrong");
        }
	    return content;
	}
}
