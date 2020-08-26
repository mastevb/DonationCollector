  
package esDB;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.log4j.Logger;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;

import entity.Item;
import entity.Schedule;

public class DBConnection {

    static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
    private static Logger logger = Logger.getLogger(DBConnection.class);

    public DBConnection() {
    }

    public boolean indexItem(Item item) throws IOException {
        RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
        logger.info("Successfully built client for DB.");

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.field("name", item.getName());
            builder.field("itemID", item.getItemID());
            builder.field("residentID", item.getResidentID());
            builder.field("description", item.getDescription());
            builder.field("imageUrl", item.getImageUrl());
            builder.field("address", item.getAddress());
            builder.field("location", item.getLocation());
            builder.field("postTime", item.getPostTime());
            builder.field("NGOID", item.getNGOID());
            builder.field("scheduleID", item.getScheduleID());
            builder.field("scheduleTime", item.getScheduleTime());
            builder.field("status", item.getStatus());
        }
        builder.endObject();
        logger.info("Successfully built item.");

        // Form the indexing request, send it, and print the response
       
		IndexRequest request = new IndexRequest(esDBUtil.index, esDBUtil.type, item.getItemID()).source(builder);
        logger.info("Successfully built request.");
        IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
        
        return response.status().equals(RestStatus.CREATED);
    }
    
    public boolean indexSchedule(Schedule schedule) throws IOException {
        RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
        logger.info("Successfully built client for DB.");

        // Creates Schedule Index
        XContentBuilder scheduleBuilder = XContentFactory.jsonBuilder();
        scheduleBuilder.startObject();
        {
            scheduleBuilder.field("scheduleID", schedule.getScheduleID());
            scheduleBuilder.field("NGOID", schedule.getNGOID());
            scheduleBuilder.field("ITEM_ID[]", schedule.getItemIDList());
            scheduleBuilder.field("scheduleTime", schedule.getScheduleTime());
            scheduleBuilder.field("status", schedule.getStatus());
        }
        scheduleBuilder.endObject();

        // Form the indexing request, send it, and print the response
        IndexRequest request = new IndexRequest("schedule", esDBUtil.type, schedule.getScheduleID()).source(scheduleBuilder);
        logger.info("Successfully built request.");
        IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
        
        return response.status().equals(RestStatus.CREATED);
    }

   

	public boolean updateItems(String[] items, String scheduleId, String scheduleTime, String NGOID) throws IOException {
		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
		// update request
		UpdateByQueryRequest updateRequest = new UpdateByQueryRequest(esDBUtil.index);
		// search query
		StringBuilder sb = new StringBuilder();
//		String time = new SimpleDateFormat("yyyy-mm-dd").format(scheduleTime);
		for(String item : items) {
			sb.append("if (ctx._source.itemID == '" + item + "') {ctx._source.status=1; ctx._source.scheduleID='" + scheduleId + "'; ctx._source.scheduleTime='" + scheduleTime +"'; ctx._source.NGOID='" + NGOID + "';}");
		}
		
		updateRequest.setScript(new Script(ScriptType.INLINE, "painless", sb.toString(), Collections.emptyMap()));
		// execution
		BulkByScrollResponse bulkResponse = esClient.updateByQuery(updateRequest, RequestOptions.DEFAULT);
		long updatedDocs = bulkResponse.getUpdated();
		if (updatedDocs > 0) {
			return true;
		} else {
			return false;
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
