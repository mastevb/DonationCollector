package rpc;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;

import esDB.esDBUtil;

/**
 * Servlet implementation class test3
 */
public class test3 extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public test3() {
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
		UpdateByQueryRequest updateRequest =new UpdateByQueryRequest(esDBUtil.index);
		//updateRequest.setQuery(new TermQueryBuilder("scheduleID", "777"));
		updateRequest.setScript(
			    new Script(
			        ScriptType.INLINE, "painless",
			        "if (ctx._source.scheduleID == '555') {ctx._source.status=0;}",
			        Collections.emptyMap())); 
		BulkByScrollResponse bulkResponse = esClient.updateByQuery(updateRequest, RequestOptions.DEFAULT);
		long updatedDocs = bulkResponse.getUpdated();
		if(updatedDocs>0 ) {
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
