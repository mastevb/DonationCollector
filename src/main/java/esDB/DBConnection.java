package esDB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONArray;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;

import entity.Item;
import entity.Item.ItemBuilder;
import entity.Schedule;
import esDB.esDBUtil;
import org.apache.log4j.Logger;

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
            builder.timeField("scheduleTime", item.getScheduleTime());
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

	// Delete donation item - Lin
	public boolean deleteDonorItem(String itemID) throws IOException {
		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
		// delete request
		DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest(esDBUtil.index);
		// query condition
		deleteRequest.setQuery(QueryBuilders.termQuery("ItemID", itemID));
		// execution
		BulkByScrollResponse bulkResponse = esClient.deleteByQuery(deleteRequest, RequestOptions.DEFAULT);
		long deletedDocs = bulkResponse.getDeleted();
		if (deletedDocs > 0) {
			return true;
		} else {
			return false;
		}
	}

	// Mark NGO schedule completed - Lin
	public boolean MarkCompleteItem(String scheduleID) throws IOException {
		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
		//update request
		UpdateByQueryRequest updateRequest =new UpdateByQueryRequest(esDBUtil.index);
		//search query
		String queryString = "if (ctx._source.scheduleID == '"+scheduleID+"') {ctx._source.status=0;}";
		updateRequest.setScript(
			    new Script(
			        ScriptType.INLINE, "painless",
			        queryString,
			        Collections.emptyMap())); 
		//execution
		BulkByScrollResponse bulkResponse = esClient.updateByQuery(updateRequest, RequestOptions.DEFAULT);
		long updatedDocs = bulkResponse.getUpdated();
		if (updatedDocs > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	// Get Item List.
	public List<Item> GetItemList(String residentID) throws IOException {
		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
		sourceBuilder.query(QueryBuilders.termQuery("residentID", "residentID"));
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices("item");
		searchRequest.source(sourceBuilder);
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); 
		SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
		
		// Get access to the returned documents
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		List<Item> itemList = new ArrayList<>();
		for (SearchHit hit : searchHits) {
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			ItemBuilder builder = new ItemBuilder();
			
			builder.setName((String) sourceAsMap.get("name"));
			builder.setResidentID((String) sourceAsMap.get("residentID"));
			builder.setDescription((String) sourceAsMap.get("description"));
			builder.setImageUrl((String) sourceAsMap.get("imageUrl"));
			builder.setAddress((String) sourceAsMap.get("address"));
			builder.setLocation((GeoPoint) sourceAsMap.get("location"));
			builder.setNGOID((String) sourceAsMap.get("NGOID"));
			builder.setScheduleID((String) sourceAsMap.get("scheduleID"));
			builder.setScheduleTime((Date) sourceAsMap.get("scheduleTime"));
			builder.setStatus((int) sourceAsMap.get("status"));
			builder.setItemID((String) sourceAsMap.get("id"));
			
			Item item = builder.build();
			itemList.add(item);
		}
		
		return itemList;
	}
	


	// Adds the interceptor to the ES REST client
	public RestHighLevelClient esClient(String serviceName, String region) {
		AWS4Signer signer = new AWS4Signer();
		signer.setServiceName(serviceName);
		signer.setRegionName(region);
		HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer,
				credentialsProvider);
		return new RestHighLevelClient(RestClient.builder(HttpHost.create(esDBUtil.aesEndpoint))
				.setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
	}
}
