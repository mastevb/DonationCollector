package external;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.apache.log4j.Logger;

public class AwsCognitoRSAKeyProvider implements RSAKeyProvider {
    private final URL aws_kid_store_url;
    private static final Logger logger = Logger.getLogger(CognitoClient.class);

    public AwsCognitoRSAKeyProvider(String aws_cognito_region, String aws_user_pools_id) {
        String url = String.format(CognitoUtil.TEMPLATE, aws_cognito_region, aws_user_pools_id);
        try {
            this.aws_kid_store_url = new URL(url);
        } catch (MalformedURLException e) {
        	logger.error(String.format("Invalid URL provided, URL=%s", url));
        	throw new RuntimeException(String.format("Invalid URL provided, URL=%s", url));
        }
    }

    @Override
    public RSAPublicKey getPublicKeyById(String kid) {
    	RSAPublicKey key = null;
        try {
            JwkProvider provider = new JwkProviderBuilder(aws_kid_store_url).build();
            Jwk jwk = provider.get(kid);
            key = (RSAPublicKey) jwk.getPublicKey();
            logger.info(String.format("Successfully got JWT kid=%s from aws_kid_store_url=%s", kid, aws_kid_store_url));
        } catch (Exception e) {
            logger.error(String.format("Failed to get JWT kid=%s from aws_kid_store_url=%s", kid, aws_kid_store_url));
        }
        return key;
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return null;
    }

    @Override
    public String getPrivateKeyId() {
        return null;
    }
}
