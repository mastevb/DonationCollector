package external;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;

public class CognitoClient {
	public static final CognitoConfig config = new CognitoConfig();
	
	
	public AWSCognitoIdentityProvider getAmazonCognitoIdentityClient() {
	      ClasspathPropertiesFileCredentialsProvider propertiesFileCredentialsProvider = 
	           new ClasspathPropertiesFileCredentialsProvider();
	 
	       return AWSCognitoIdentityProviderClientBuilder.standard()
	                      .withCredentials(propertiesFileCredentialsProvider)
	                             .withRegion(config.region)
	                             .build();
	 }
}
