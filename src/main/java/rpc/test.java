package rpc;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;

import esDB.esDBUtil;

/**
 * Servlet implementation class test
 */
public class test extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public test() {
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
		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
        {
            builder.field("name", "777");
            builder.field("ItemID", "666").field("index","true");
            builder.field("residentID", "777");
            builder.field("description", "777");
            builder.field("imageUrl", "777");
            builder.field("address", "777");
            builder.field("location", new GeoPoint());
            builder.field("NGOID", "777");
            builder.field("scheduleID", "666");
            builder.timeField("scheduleTime", new Date());
            builder.field("status", 1);
        }
        builder.endObject();
        
     // Form the indexing request, send it, and print the response
        IndexRequest requestR = new IndexRequest(esDBUtil.index).source(builder);
        IndexResponse responseR =  esClient.index(requestR, RequestOptions.DEFAULT);
        response.getWriter().append("Serve at: ").append(request.getContextPath());
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
