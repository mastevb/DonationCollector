package rpc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;

import esDB.esDBUtil;

/**
 * Servlet implementation class test4
 */
public class test4 extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public test4() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
		XContentBuilder builder = XContentFactory.jsonBuilder();
	    builder.startObject();
	    {
	        builder.startObject("properties");
	        {
	            builder.startObject("message");
	            {
	                builder.field("type", "text");

	            }
	            builder.endObject();
	        }
	        builder.endObject();
	    }
	    builder.endObject();
	    CreateIndexRequest createRequest = new CreateIndexRequest("test1");
	    createRequest.alias(new Alias("twitter_alias").filter(QueryBuilders.termQuery("type", "test2"))); 
	    createRequest.mapping(builder);
	    CreateIndexResponse createIndexResponse =esClient.indices().create(createRequest, RequestOptions.DEFAULT);
	    boolean acknowledged = createIndexResponse.isAcknowledged(); 
	    if(acknowledged==true) {
    		response.getWriter().append("Serve at: ").append(request.getContextPath());
    	}
    	else {
    		response.getWriter().append("Failed at: ").append(request.getContextPath());
    	}
	}
	
	// Adds the interceptor to the ES REST client
    public RestHighLevelClient esClient(String serviceName, String region) {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
        return new RestHighLevelClient(RestClient.builder(HttpHost.create(esDBUtil.aesEndpoint))
                .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }

}
