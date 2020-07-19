package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.hibernate.criterion.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;

    //搜索所有
    @Test
    public void testSearchAll() throws IOException, ParseException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //搜索方式
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置过滤源对象  第一个参数:将来结果集包含哪个字段  第二个不包含
        searchSourceBuilder.fetchSource(new String[]{"name","price","studymodel","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的信息
        SearchHit[] hitsHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : hitsHits) {
            String id = hit.getId();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //有与前边设置了源字段过滤  因此拿不到description
            String description = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");

            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            //String timestamp = (String)sourceAsMap.get("timestamp");
            System.out.println(id);
            System.out.println(name);
            System.out.println(price);
           System.out.println(timestamp);
        }
    }

    /**分页搜索
     * ES支持分页查询,传入from和size
     * from : 表示其实文档的下标,从 0 开始
     * size : 查询文档的数量
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testSearchPage() throws IOException, ParseException {
        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //设置分页参数
        int page = 1;
        int size = 1;
        //计算记录的起始下标
        int from = (page - 1) * size;

        searchSourceBuilder.from(from);//起始记录下标 从0开始
        searchSourceBuilder.size(size);//每页记录数
        //搜索方式
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        //设置过滤源对象  第一个参数:将来结果集包含哪个字段  第二个不包含
        searchSourceBuilder.fetchSource(new String[]{"name","price","studymodel","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的信息
        SearchHit[] hitsHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : hitsHits) {
            String id = hit.getId();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //有与前边设置了源字段过滤  因此拿不到description
            String description = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");

            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(id);
            System.out.println(name);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }


    /**term查询
     * 在搜索时整体匹配关键字,不进行关键字分词
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testTerm() throws IOException, ParseException {
        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //创建搜索源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //搜索方式
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
        //设置过滤源对象
        searchSourceBuilder.fetchSource(new String[]{"name","price","studymodel","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的信息
        SearchHit[] hitsHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : hitsHits) {
            String id = hit.getId();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //有与前边设置了源字段过滤  因此拿不到description
            String description = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");

            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(id);
            System.out.println(name);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }

    /**根据id查询
     * 根据多个id值匹配
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testSearchByIds() throws IOException, ParseException {
        //创建搜索源对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //创建搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //要查询的id们
        String[] ids = {"1","3","50"};
        //搜索方式  termsQuery 根据id查询用termsQuery
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id",ids));
        //设置过滤源字段
        searchSourceBuilder.fetchSource(new String[]{"name","price","studymodel","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //搜索匹配的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的信息
        SearchHit[] hitsHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : hitsHits) {
            String id = hit.getId();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //有与前边设置了源字段过滤  因此拿不到description
            String description = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");

            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(id);
            System.out.println(name);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }

    /**
     * Match Query测试
     * 先将搜索的字符串分词,再使用各个词条从索引中搜索
     */
    @Test
    public void testMatchQuery() throws IOException, ParseException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建请求源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //搜索方式       0.8 * 3 = 2.4 三个分词中至少匹配两个才算匹配
        searchSourceBuilder.query(QueryBuilders.matchQuery("description","spring开发框架").minimumShouldMatch("80%"));
        //过滤源字段
        searchSourceBuilder.fetchSource(new String[]{"name","price","studymodel","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //获取搜索到的数据 匹配度高的
        SearchHit[] searchHits = hits.getHits();
        //创建日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取搜索到的数据
        for (SearchHit searchHit : searchHits) {
            String id = searchHit.getId();
            //将搜索到的数据转为map
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //有与前边设置了源字段过滤  因此拿不到description
            String description = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");

            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println("id = " + id);
            System.out.println("name = " + name);
            System.out.println("price = " + price);
            System.out.println("timestamp = " + timestamp);
        }
    }

    /**
     * multi Query
     * 一次可以匹配多个字段
     * 1、基本使用 单项匹配是在一个field中去匹配，多项匹配是拿关键字去多个Field中匹配。
     * 2、提升boost
     */
    @Test
    public void testMultiQuery() throws IOException, ParseException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //搜索类型  满足一个搜索分词就可以  提升10倍name域的权重
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring css","name","description")
                .minimumShouldMatch("50%")
                .field("name",10));
        //过滤源字段
        searchSourceBuilder.fetchSource(new String[]{"name","price","studymodel","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //获取搜索到的总条数
        long totalHits = hits.getTotalHits();
        //获取搜索到的数据 (匹配度高的)
        SearchHit[] searchHits = hits.getHits();
        //创建日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取搜索到的数据
        for (SearchHit searchHit : searchHits) {
            String id = searchHit.getId();
            //将搜索到的数据转为map
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //有与前边设置了源字段过滤  因此拿不到description
            String description = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");

            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println("id = " + id);
            System.out.println("name = " + name);
            System.out.println("price = " + price);
            System.out.println("timestamp = " + timestamp);
        }
    }

    /**
     * Bool Query
     * 实现多个查询的组合
     */
    @Test
    public void testBoolQuery() throws IOException, ParseException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //搜索类型
        // 1. multimatchquery  包含spring 或 css 其中一个分词的数据
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);
        // 2. term query  studymodel 域 为 201002 的数据
        TermQueryBuilder termQuery = QueryBuilders.termQuery("studymodel", "201002");

        //创建boolQuery 进行条件组合查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQuery);

        // 3. 使用boolQuery组合的条件进行查询
        searchSourceBuilder.query(boolQueryBuilder);

        //过滤源字段
        searchSourceBuilder.fetchSource(new String[]{"name","price","studymodel","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //获取搜索到的总条数
        long totalHits = hits.getTotalHits();
        //获取搜索到的数据 (匹配度高的)
        SearchHit[] searchHits = hits.getHits();
        //创建日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取搜索到的数据
        for (SearchHit searchHit : searchHits) {
            String id = searchHit.getId();
            //将搜索到的数据转为map
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //有与前边设置了源字段过滤  因此拿不到description
            String description = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");

            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println("id = " + id);
            System.out.println("name = " + name);
            System.out.println("price = " + price);
            System.out.println("timestamp = " + timestamp);
        }
    }

    /**
     * 过虑器在布尔查询中使用
     * 过虑是针对搜索的结果进行过虑，过虑器主要判断的是文档是否匹配，不去计算和判断文档的匹配度得分，
     * 所以过滤器性能比查询要高，推荐尽量使用过虑器去实现查询或者过虑器和查询共同使用。
     */
    @Test
    public void testFilter() throws ParseException, IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //搜索类型
        // 1. multimatchquery  包含spring 或 css 其中一个分词的数据
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);

        //创建boolQuery 进行条件组合查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        // 定义过滤器
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel","201001"));
        // price 大于60 小于100
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));

        // 3. 使用boolQuery组合的条件进行查询
        searchSourceBuilder.query(boolQueryBuilder);

        //过滤源字段
        searchSourceBuilder.fetchSource(new String[]{"name","price","studymodel","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //获取搜索到的总条数
        long totalHits = hits.getTotalHits();
        //获取搜索到的数据 (匹配度高的)
        SearchHit[] searchHits = hits.getHits();
        //创建日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取搜索到的数据
        for (SearchHit searchHit : searchHits) {
            String id = searchHit.getId();
            //将搜索到的数据转为map
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //有与前边设置了源字段过滤  因此拿不到description
            String description = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");

            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println("id = " + id);
            System.out.println("name = " + name);
            System.out.println("price = " + price);
            System.out.println("timestamp = " + timestamp);
        }
    }

    /**
     * 排序
     * @throws ParseException
     * @throws IOException
     */
    @Test
    public void testSort() throws ParseException, IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //搜索类型
        //创建boolQuery 进行条件组合查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // price
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        // 3. 使用boolQuery组合的条件进行查询
        searchSourceBuilder.query(boolQueryBuilder);

        //添加排序
        searchSourceBuilder.sort("studymodel", SortOrder.DESC);
        searchSourceBuilder.sort("price", SortOrder.ASC);
        //过滤源字段
        searchSourceBuilder.fetchSource(new String[]{"name","price","studymodel","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //获取搜索到的总条数
        long totalHits = hits.getTotalHits();
        //获取搜索到的数据 (匹配度高的)
        SearchHit[] searchHits = hits.getHits();
        //创建日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取搜索到的数据
        for (SearchHit searchHit : searchHits) {
            String id = searchHit.getId();
            //将搜索到的数据转为map
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //有与前边设置了源字段过滤  因此拿不到description
            String description = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");

            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println("id = " + id);
            System.out.println("name = " + name);
            System.out.println("price = " + price);
            System.out.println("timestamp = " + timestamp);
        }
    }

    /**
     * Highlight 高亮测试  针对关键字
     * @throws ParseException
     * @throws IOException
     */
    @Test
    public void testHighlight() throws ParseException, IOException {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //创建搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //搜索类型
        // 1. multimatchquery  包含spring 或 css 其中一个分词的数据
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发框架", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);

        //创建boolQuery 进行条件组合查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        // 定义过滤器
        // price 大于0 小于100
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        // 3. 使用boolQuery组合的条件进行查询
        searchSourceBuilder.query(boolQueryBuilder);

        //设置排序
        searchSourceBuilder.sort("studymodel", SortOrder.DESC);
        searchSourceBuilder.sort("price", SortOrder.ASC);
        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");
        highlightBuilder.postTags("</tag>");
        //设置哪些字段高亮
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        //过滤源字段
        searchSourceBuilder.fetchSource(new String[]{"name","price","studymodel","timestamp"},new String[]{});
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        //获取搜索到的总条数
        long totalHits = hits.getTotalHits();
        //获取搜索到的数据 (匹配度高的)
        SearchHit[] searchHits = hits.getHits();
        //创建日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取搜索到的数据
        for (SearchHit searchHit : searchHits) {
            String id = searchHit.getId();
            //将搜索到的数据转为map
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //取出高亮字段
            Map<String, HighlightField> map = searchHit.getHighlightFields();
            //源字段 name 字段内容
            String name = (String) sourceAsMap.get("name");
            if (map != null){
                //取出高亮字段
                HighlightField highlightField = map.get("name");
                if (highlightField != null){
                    Text[] fragments = highlightField.getFragments();
                    //拼接加标签后的内容
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text fragment : fragments) {
                        stringBuffer.append(fragment);
                    }
                    //给name重新赋值   如果被高亮则高亮标签会赋值给name
                    name = stringBuffer.toString();
                }
            }
            //有与前边设置了源字段过滤  因此拿不到description
            String description = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");

            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println("id = " + id);
            System.out.println("name = " + name);
            System.out.println("price = " + price);
            System.out.println("timestamp = " + timestamp);
        }
    }

    //Highlight
    @Test
    public void highlight() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //boolQuery搜索方式
        //先定义一个MultiMatchQuery
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发框架", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);

        //定义一个boolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        //定义过虑器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过虑,第一个参数结果集包括哪些字段，第二个参数表示结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"},new String[]{});

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");
        highlightBuilder.postTags("</tag>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
//        highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        searchSourceBuilder.highlighter(highlightBuilder);

        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(SearchHit hit:searchHits){
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //源文档的name字段内容
            String name = (String) sourceAsMap.get("name");
            //取出高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(highlightFields!=null){
                //取出name高亮字段
                HighlightField nameHighlightField = highlightFields.get("name");
                if(nameHighlightField!=null){
                    Text[] fragments = nameHighlightField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for(Text text:fragments){
                        stringBuffer.append(text);
                    }
                    name = stringBuffer.toString();
                }
            }

            //由于前边设置了源文档字段过虑，这时description是取不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }
}
