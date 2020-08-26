package esDB;

import java.io.IOException;
import java.util.Collections;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
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
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;

import entity.Item;

public class DBConnection {

	static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

	public DBConnection() {
	}

	public IndexResponse indexItem(Item item) throws IOException {
		RestHighLevelClient esClient = esClient(esDBUtil.serviceName, esDBUtil.region);

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
			// builder.field("postTime", item.getPostTime());
			builder.field("NGOID", item.getNGOID());
			builder.field("scheduleID", item.getScheduleID());
			builder.timeField("scheduleTime", item.getScheduleTime());
			builder.field("status", item.getStatus());
		}
		builder.endObject();

		// Form the indexing request, send it, and print the response
		IndexRequest request = new IndexRequest(esDBUtil.index, esDBUtil.type, item.getItemID()).source(builder);
		return esClient.index(request, RequestOptions.DEFAULT);
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
