
package com.xiaotiao.elasticsearch.controller;

import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.xiaotiao.elasticsearch.Book;
import org.apache.lucene.queryparser.xml.QueryBuilder;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/book")
public class BookService {

    @Autowired
    private RestHighLevelClient client;

    @GetMapping(value = "/get")
    @ResponseBody
    public ResponseEntity get(@RequestParam("id") String id){

        GetRequest request = new GetRequest("book", id);
        try {
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            Map<String, Object> resultMap = response.getSource();
            return new ResponseEntity(response.getSource(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity add(@RequestBody Book book){

        try {
            XContentBuilder content = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("title", book.getTitle())
                    .field("author", book.getAuthor())
                    .field("word_count", book.getWord_count())
                    .field("publish_date", book.getPublish_date())
                    .endObject();
            IndexRequest request = new IndexRequest("book").source(content);
            IndexResponse response =  client.index(request, RequestOptions.DEFAULT);
            return new ResponseEntity(response.toString(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity update(@RequestBody Book book){

        try {
            UpdateRequest request = new UpdateRequest("book", book.getId());
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject();
            if(!StringUtils.isEmpty(book.getAuthor())){
                builder.field("author", book.getAuthor());
            }
            if(!StringUtils.isEmpty(book.getAuthor())){
                builder.field("title", book.getTitle());
            }
            if(!StringUtils.isEmpty(book.getAuthor())){
                builder.field("word_count", book.getWord_count());
            }
            if(!StringUtils.isEmpty(book.getAuthor())){
                builder.field("publish_date", book.getPublish_date());
            }
            builder.endObject();
            request.doc(builder);
            UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
            return new ResponseEntity(response.toString(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("delete")
    @ResponseBody
    public String delete(@RequestParam("id") String id){

        try {
            DeleteRequest request = new DeleteRequest("book", id);
            DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    @PostMapping("/complexQuery")
    public String complexQuery(@RequestBody Book book){


        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if(!StringUtils.isEmpty(book.getAuthor())){
            //K分词器会把中文分割, 加上keyword就可以避免
            boolQuery.must(QueryBuilders.matchQuery("author.keyword", book.getAuthor()));
        }
        if(!StringUtils.isEmpty(book.getTitle())){
            boolQuery.must(QueryBuilders.matchQuery("title", book.getTitle()));
        }
        RangeQueryBuilder rangeQuery = null;
        if(book.getLt_word_count() != null && book.getLt_word_count() > 0){
            if(rangeQuery == null){
                rangeQuery = QueryBuilders.rangeQuery("word_count");
            }
            rangeQuery.from(book.getGt_word_count());
            boolQuery.filter(rangeQuery);
        }

        if(book.getLt_word_count() != null && book.getLt_word_count() > 0){
            if(rangeQuery == null){
                rangeQuery = QueryBuilders.rangeQuery("word_count");
            }
            rangeQuery.to(book.getLt_word_count());
            boolQuery.filter(rangeQuery);
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(boolQuery).from(0).size(3);//限制查询个数

        SearchRequest searchRequest = new SearchRequest().source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<SearchHit> resultList = new ArrayList<SearchHit>();
            for (SearchHit searchHit: searchResponse.getHits()) {
                resultList.add(searchHit);
            }
            return resultList.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";

    }
}
