package esDB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;

import entity.Item;
import entity.Item.ItemBuilder;
import entity.Schedule;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

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
		IndexRequest request = new IndexRequest("schedule", esDBUtil.type, schedule.getScheduleID())
				.source(scheduleBuilder);
		logger.info("Successfully built request.");
		IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);

		return response.status().equals(RestStatus.CREATED);
	}

	// Get schedule list
	public List<Schedule> getSchedule(String username) throws IOException {
		List<Schedule> result = new ArrayList<>();
		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
		logger.info("Successfully built client for DB.");
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.termQuery("username", "username"));
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices("schedule");
		searchRequest.source(sourceBuilder);
		SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
		// get access to the returned documents
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
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
			builder.setItemIDList(itemIds);
			builder.setScheduleID((String) sourceAsMap.get("Schedule_ID"));
			builder.setScheduleTime((String) sourceAsMap.get("time"));
			builder.setStatus((int) sourceAsMap.get("status"));
			Schedule s = builder.build();
			result.add(s);
		}
		return result;
	}

	// Get Item List.
	public List<Item> GetItemList(String residentID) throws IOException {
		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);
		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("residentID", residentID);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(matchQueryBuilder);
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices("item");
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

		// Get access to the returned documents
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		System.out.println(searchHits.length);
		List<Item> itemList = new ArrayList<>();
		for (SearchHit hit : searchHits) {
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			ItemBuilder builder = new ItemBuilder();

			builder.setName((String) sourceAsMap.get("name"));
			builder.setResidentID((String) sourceAsMap.get("residentID"));
			builder.setDescription((String) sourceAsMap.get("description"));
			builder.setImageUrl((String) sourceAsMap.get("imageUrl"));
			builder.setAddress((String) sourceAsMap.get("address"));
			//builder.setLocation((GeoPoint) sourceAsMap.get("location"));
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

	// Adds the interpreter to the ES REST client
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
