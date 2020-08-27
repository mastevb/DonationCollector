package esDB;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
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

        // Creates Item Index
        XContentBuilder itemBuilder = XContentFactory.jsonBuilder();
        itemBuilder.startObject();
        {
            itemBuilder.field("ItemID", "keyword").field("index", "true");
            itemBuilder.field("name", "string");
            itemBuilder.field("residentID", "keyword");
            itemBuilder.field("description", "string");
            itemBuilder.field("imageUrl", "string");
            itemBuilder.field("address", "string");
            itemBuilder.field("location", new GeoPoint());
            itemBuilder.field("NGOID", "keyword");
            itemBuilder.field("scheduleID", "keyword");
            itemBuilder.field("scheduleTime", "keyword");
            itemBuilder.field("postTime", "keyword");
            itemBuilder.field("status", 0);
        }
        itemBuilder.endObject();

        // Form the indexing request, send it, and print the response
        IndexRequest request = new IndexRequest("item", type, id).source(itemBuilder);
        IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());

        // Creates Schedule Index
        XContentBuilder scheduleBuilder = XContentFactory.jsonBuilder();
        scheduleBuilder.startObject();
        {
            scheduleBuilder.field("scheduleID", "keyword");
            scheduleBuilder.field("NGOID", "keyword");
            scheduleBuilder.field("ITEM_ID[]", "text");
            scheduleBuilder.field("scheduleTime", "keyword");
            scheduleBuilder.field("status", 0);
        }
        scheduleBuilder.endObject();

        // Form the indexing request, send it, and print the response
        request = new IndexRequest("schedule", type, id).source(scheduleBuilder);
        response = esClient.index(request, RequestOptions.DEFAULT);

        System.out.println(response.toString());
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