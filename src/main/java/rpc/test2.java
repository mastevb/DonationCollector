package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;

import esDB.esDBUtil;

/**
 * Servlet implementation class test2
 */
public class test2 extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public test2() {
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
		//JSONObject input = new JSONObject(IOUtils.toString(request.getReader()));
		//String itemID = input.getString("item_id");
		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
		//delete request
		DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest(esDBUtil.index);
		//query condition
		deleteRequest.setQuery(QueryBuilders.termQuery("ItemID","999"));
		//execution
		BulkByScrollResponse bulkResponse = esClient.deleteByQuery(deleteRequest, RequestOptions.DEFAULT);
		long deletedDocs = bulkResponse.getDeleted();
		if(deletedDocs>0 ) {
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
