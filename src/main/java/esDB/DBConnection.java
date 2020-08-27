  
package esDB;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.log4j.Logger;
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
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
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

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;

import entity.Item;
import entity.Item.ItemBuilder;
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
            scheduleBuilder.field("NGOUsername", schedule.getNGOUsername());
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
 		deleteRequest.setQuery(QueryBuilders.termQuery("itemID", itemID));
 		// execution
 		BulkByScrollResponse bulkResponse = esClient.deleteByQuery(deleteRequest, RequestOptions.DEFAULT);
 		long deletedDocs = bulkResponse.getDeleted();
 		if (deletedDocs > 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}

   
 // Mark item schedule completed - Lin
 	public boolean MarkCompleteItem(String scheduleID) throws IOException {
 		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
 		// update request
 		UpdateByQueryRequest updateRequest = new UpdateByQueryRequest(esDBUtil.index);
 		// search query
 		String queryString = "if (ctx._source.scheduleID == '" + scheduleID + "') {ctx._source.status=2;}";
 		updateRequest.setScript(new Script(ScriptType.INLINE, "painless", queryString, Collections.emptyMap()));
 		// execution
 		BulkByScrollResponse bulkResponse = esClient.updateByQuery(updateRequest, RequestOptions.DEFAULT);
 		long updatedDocs = bulkResponse.getUpdated();
 		if (updatedDocs > 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}

 	// Mark NGO schedule completed - Lin
 	public boolean MarkCompleteNGO(String scheduleID) throws IOException {
 		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
 		// update request
 		UpdateByQueryRequest updateRequest = new UpdateByQueryRequest("schedule");
 		// search query
 		String queryString = "if (ctx._source.scheduleID == '" + scheduleID + "') {ctx._source.status=1}";
 		updateRequest.setScript(new Script(ScriptType.INLINE, "painless", queryString, Collections.emptyMap()));
 		// execution
 		BulkByScrollResponse bulkResponse = esClient.updateByQuery(updateRequest, RequestOptions.DEFAULT);
 		long updatedDocs = bulkResponse.getUpdated();
 		if (updatedDocs > 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	
 	//update item's field when mark schedule complete
	public boolean updateItems(String[] items, String scheduleId, String scheduleTime, String NGOID) throws IOException {
		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
		// update request
		UpdateByQueryRequest updateRequest = new UpdateByQueryRequest(esDBUtil.index);
		// search query
		StringBuilder sb = new StringBuilder();
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
	
	
	// Get schedule list
		public List<Schedule> getSchedule(String username) throws IOException {
			List<Schedule> result = new ArrayList<>();
			RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
			logger.info("Successfully built client for DB.");
			MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("NGOUsername", username);
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.query(matchQueryBuilder);
			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices("schedule");
			searchRequest.source(searchSourceBuilder);
			SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
			
			// get access to the returned documents
			SearchHits hits = searchResponse.getHits();
			SearchHit[] searchHits = hits.getHits();
			//System.out.println("!!!!!" + searchHits.length);
			// build and add
			for (SearchHit hit : searchHits) {
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
				Schedule.ScheduleBuilder builder = new Schedule.ScheduleBuilder();
				// build
				builder.setNGOID((String) sourceAsMap.get("NGOID"));
				
				Object ids = sourceAsMap.get("ITEM_ID[]");
				ArrayList<String> itemIds = new ArrayList<>();
				if (ids instanceof ArrayList<?>) {
					for (int i = 0; i < ((ArrayList<?>) ids).size(); i++) {
						// Still not enough for a type.
						Object o = ((ArrayList<?>) ids).get(i);
						if (o instanceof String) {
							// Here we go!
							String v = (String) o;
							// use v
							itemIds.add(v);
						}
					}
				}
				// builder.setItemIDList(itemIds);
				
				ArrayList<Item> itemList = new ArrayList<>(); 
				for (String id : itemIds) {
					Item it = GetItemByID(id);
					if (it != null) {
						itemList.add(it); 
					}
				}
				// System.out.println(itemList.size());
				builder.setItemList(itemList);
				
				builder.setScheduleID((String) sourceAsMap.get("scheduleID"));
				builder.setScheduleTime((String) sourceAsMap.get("scheduleTime"));
				builder.setStatus((int) sourceAsMap.get("status"));
				Schedule s = builder.build();
				result.add(s);
			}
			return result;
		}
		
		// Get one Item by Item ID
		public Item GetItemByID(String itemID) throws IOException {
			RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
			MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("itemID", itemID);
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.query(matchQueryBuilder);
			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices("item");
			searchRequest.source(searchSourceBuilder);
			SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
			
			// Get access to the returned documents
			SearchHits hits = searchResponse.getHits();
			SearchHit[] searchHits = hits.getHits();
			// System.out.println(searchHits.length);
			// System.out.println(itemID);
			if (searchHits.length > 0) {
				SearchHit hit = searchHits[0];
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
				ItemBuilder builder = new ItemBuilder();
		
				builder.setName((String) sourceAsMap.get("name"));
				//builder.setResidentID((String) sourceAsMap.get("residentID"));
				builder.setDescription((String) sourceAsMap.get("description"));
				builder.setImageUrl((String) sourceAsMap.get("imageUrl"));
				builder.setAddress((String) sourceAsMap.get("address"));
				// builder.setLocation((GeoPoint) sourceAsMap.get("location"));
				//builder.setNGOID((String) sourceAsMap.get("NGOID"));
				//builder.setScheduleID((String) sourceAsMap.get("scheduleID"));
				//builder.setScheduleTime((String) sourceAsMap.get("scheduleTime"));
				//builder.setStatus((int) sourceAsMap.get("status"));
				//builder.setItemID((String) sourceAsMap.get("id"));
		
				Item item = builder.build();
				return item;
			} else {
				System.out.println("SIZE IS WRONG: " + searchHits.length);
				return null;
			}
		}
	
	
	
		// Get Item List By Geo point.
		public List<Item> GetItemListByGeo(GeoPoint point) throws IOException {
			RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
			GeoDistanceQueryBuilder qb = QueryBuilders.geoDistanceQuery("location")               
					.point(point.getLat(), point.getLon())                                          
					.distance(10, DistanceUnit.KILOMETERS); 
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.query(qb);
			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices("item");
			searchRequest.source(searchSourceBuilder);
			SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

			// Get access to the returned documents
			SearchHits hits = searchResponse.getHits();
			SearchHit[] searchHits = hits.getHits();
			// System.out.println(searchHits.length);
			List<Item> itemList = new ArrayList<>();
			for (SearchHit hit : searchHits) {	
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();	
				if((int)sourceAsMap.get("status") == 0) {
					ItemBuilder builder = new ItemBuilder();
					builder.setItemID((String) sourceAsMap.get("itemID"));
					builder.setName((String) sourceAsMap.get("name"));
					builder.setResidentID((String) sourceAsMap.get("residentID"));
					builder.setDescription((String) sourceAsMap.get("description"));
					builder.setImageUrl((String) sourceAsMap.get("imageUrl"));
					builder.setAddress((String) sourceAsMap.get("address"));
					// builder.setLocation((GeoPoint) sourceAsMap.get("location"));
					builder.setNGOID((String) sourceAsMap.get("NGOID"));
					builder.setScheduleID((String) sourceAsMap.get("scheduleID"));
					builder.setScheduleTime((String) sourceAsMap.get("scheduleTime"));
					builder.setStatus((int) sourceAsMap.get("status"));

					Item item = builder.build();
					itemList.add(item);	
				}
				
			}

			return itemList;
		}
	
	
	
	
	
	// Get Item List.
		public List<Item> GetItemList(String residentID) throws IOException {
			RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(QueryBuilders.matchQuery("residentID", residentID));
			sourceBuilder.from(0);
			sourceBuilder.size(25);
			sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
			
			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices("item");
			searchRequest.source(sourceBuilder);
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
				builder.setNGOID((String) sourceAsMap.get("NGOID"));
				builder.setScheduleID((String) sourceAsMap.get("scheduleID"));
				builder.setScheduleTime((String) sourceAsMap.get("scheduleTime"));
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
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
        return new RestHighLevelClient(RestClient.builder(HttpHost.create(esDBUtil.aesEndpoint))
                .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }


}
