package esDB;

import org.elasticsearch.common.Strings;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class AmazonESSetUp {

    private static String serviceName = "es";
    private static String region = "us-west-2";
    private static String aesEndpoint = "https://search-donationcollector-azytnztkxuwh625uythsxbxviy.us-west-2.es.amazonaws.com";
    private static String type = "_doc";
    private static String id = "1";
    private static int ITEM_ARRAY_SIZE = 100;
    
    static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

    public static void main(String[] args) throws IOException {
        RestHighLevelClient esClient = esClient(serviceName, region);
        CreateIndexRequest requestItem = new CreateIndexRequest("item");
        
        // Creates Item Index
        XContentBuilder itemBuilder = XContentFactory.jsonBuilder();
        itemBuilder.startObject();
        {
        	itemBuilder.startObject("ItemID");
            {
            	itemBuilder.field("type", "keyword");
            	itemBuilder.field("index", "true");
            }
            itemBuilder.endObject();
            
        	itemBuilder.startObject("name");
            {
            	itemBuilder.field("type", "text");
            }
            itemBuilder.endObject();
            
        	itemBuilder.startObject("residentID");
            {
            	itemBuilder.field("type", "keyword");
            }
            itemBuilder.endObject();
            
        	itemBuilder.startObject("description");
            {
            	itemBuilder.field("type", "text");
            }
            itemBuilder.endObject();
            
        	itemBuilder.startObject("imageUrl");
            {
            	itemBuilder.field("type", "text");
            }
            itemBuilder.endObject();
            
        	itemBuilder.startObject("address");
            {
            	itemBuilder.field("type", "text");
            }
            itemBuilder.endObject();
            
        	itemBuilder.startObject("location");
            {
            	itemBuilder.field("type", "geo_point");
            }
            itemBuilder.endObject();
            
        	itemBuilder.startObject("NGOID");
            {
            	itemBuilder.field("type", "keyword");
            }
            itemBuilder.endObject();
            
        	itemBuilder.startObject("scheduleID");
            {
            	itemBuilder.field("type", "keyword");
            }
            itemBuilder.endObject();
            
        	itemBuilder.startObject("scheduleTime");
            {
            	itemBuilder.field("type", "text");
            }
            itemBuilder.endObject();
            
        	itemBuilder.startObject("postTime");
            {
            	itemBuilder.field("type", "text");
            }
            itemBuilder.endObject();
            
        	itemBuilder.startObject("status");
            {
            	itemBuilder.field("type", "integer");
            }
            itemBuilder.endObject();
        }
        itemBuilder.endObject();
        String json = Strings.toString(itemBuilder);
        System.out.println(json);
        requestItem.mapping("properties", itemBuilder);
        
        CreateIndexResponse createIndexResponse = esClient.indices().create(requestItem, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse.toString());
        
        
        // Creates Schedule Index
        CreateIndexRequest requestSchedule = new CreateIndexRequest("schedule");
        XContentBuilder scheduleBuilder = XContentFactory.jsonBuilder();
        scheduleBuilder.startObject();
        {
        	scheduleBuilder.startObject("scheduleID");
            {
            	scheduleBuilder.field("type", "keyword");
            }
            scheduleBuilder.endObject();
            
            scheduleBuilder.startObject("NGOID");
            {
            	scheduleBuilder.field("type", "keyword");
            }
            scheduleBuilder.endObject();
            
            scheduleBuilder.startObject("ITEM_ID[]");
            {
            	scheduleBuilder.field("type", "keyword");
            }
            scheduleBuilder.endObject();
            
            scheduleBuilder.startObject("scheduleTime");
            {
            	scheduleBuilder.field("type", "text");
            }
            scheduleBuilder.endObject();
            
            scheduleBuilder.startObject("status");
            {
            	scheduleBuilder.field("type", "integer");
            }
            scheduleBuilder.endObject();
            
            //scheduleBuilder.field("scheduleID", "keyword");
            //scheduleBuilder.field("NGOID", "keyword");
            //scheduleBuilder.field("ITEM_ID[]", "text");
            //scheduleBuilder.field("scheduleTime", "keyword");
            //scheduleBuilder.field("status", 0);
        }
        scheduleBuilder.endObject();
        String json1 = Strings.toString(scheduleBuilder);
        System.out.println(json1);
        requestSchedule.mapping("properties", scheduleBuilder);

        // Form the indexing request, send it, and print the response
        createIndexResponse = esClient.indices().create(requestSchedule, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse.toString());
    }

    // Adds the interceptor to the ES REST client
    public static RestHighLevelClient esClient(String serviceName, String region) {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
        return new RestHighLevelClient(RestClient.builder(HttpHost.create(aesEndpoint)).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }
}