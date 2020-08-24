package esDB;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.rest.RestStatus;

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
