package com.xuecheng.search.service.impl;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.search.service.EsCourseService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseServiceImpl implements EsCourseService {
    //注入ES的信息
    @Value("${xuecheng.course.index}")
    private String index;
    @Value("${xuecheng.media.index}")
    private String media_index;
    @Value("${xuecheng.course.type}")
    private String type;
    @Value("${xuecheng.media.type}")
    private String media_type;
    @Value("${xuecheng.course.source_field}")
    private String source_field;
    @Value("${xuecheng.media.source_field}")
    private String media_source_field;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 搜索课程
     *
     * @param page
     * @param size
     * @param courseSearchParam
     * @return
     */
    @Override
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置类型
        searchRequest.types(type);
        //创建搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //设置过滤源字段
        String[] splitArray = source_field.split(",");
        searchSourceBuilder.fetchSource(splitArray, new String[]{});
        //搜索类型
        //根据关键字搜索
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            //System.out.println("courseSearchParam.getKeyword():"+courseSearchParam.getKeyword());
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan");
            multiMatchQueryBuilder.minimumShouldMatch("70%").field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        //过滤
        //根据一级分类
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            //System.out.println("一级分类:"+courseSearchParam.getMt());
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }
        //根据二级分类
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            //System.out.println("二级分类:"+courseSearchParam.getSt());
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }
        //根据等级查询
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }


        //设置分页参数
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 3;
        }
        //起始记录下标
        int start = (page - 1) * size;
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(size);
        //布尔查询
        //设置boolQueryBuilder到searchSourceBuilder
        searchSourceBuilder.query(boolQueryBuilder);

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置哪些字段高亮
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            //执行搜索
            searchResponse = restHighLevelClient.search(searchRequest);

        } catch (IOException e) {
            e.printStackTrace();
            return new QueryResponseResult<>(CommonCode.FAIL,new QueryResult<CoursePub>());
        }
            //获取响应结果
            SearchHits hits = searchResponse.getHits();
            //System.out.println("SearchHits:"+hits.toString());
            //获取搜索到的数据 (匹配度高的)
            SearchHit[] searchHits = hits.getHits();
            //匹配的总条数
            long totalHits = hits.getTotalHits();
            //数据列表
            List<CoursePub> list = new ArrayList<>();
            //获取搜索到的数据
            for (SearchHit searchHit : searchHits) {
                CoursePub coursePub = new CoursePub();
                //将搜索到的数据转为map
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                //取出name  源字段 name的内容
                String name = (String) sourceAsMap.get("name");
                //coursePub.setName(name);  这里直接将院子段的name设置过去,前台将获取不到拼接好高亮的信息
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);
                String mt = (String) sourceAsMap.get("mt");
                coursePub.setMt(mt);
                String st = (String) sourceAsMap.get("st");
                coursePub.setSt(st);
                //取出高亮字段
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                //如果高亮字段不为空,将高亮后的信息进行拼接
                if (highlightFields != null) {
                    //取出高亮字段
                    HighlightField highlightField = highlightFields.get("name");
                    if (highlightField != null) {
                        Text[] fragments = highlightField.getFragments();
                        //拼接添加高亮标签后的内容
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text fragment : fragments) {
                            stringBuffer.append(fragment.string());
                        }
                        //给name重新赋值   拼接好高亮标签的name信息
                        name = stringBuffer.toString();
                        //System.out.println("name:"+name);//------------------------------------------
                    }
                }
                coursePub.setName(name);
                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //价格
                Double price = null;
                try {
                    if (sourceAsMap.get("price") != null) {
                        //Double.parseDouble((String)sourceAsMap.get("price"));
                        price =(Double)sourceAsMap.get("price");
                        //price = (Double);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice(price);
                //将coursePub对象放入list
                //list.add(coursePub);
                //原价价格
                Double price_old = null;
                try {
                    if (sourceAsMap.get("price_old") != null) {
                        Double.parseDouble((String)sourceAsMap.get("price_old"));
                        //price = (Double) ;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice_old(price_old);
                //将coursePub对象放入list
                list.add(coursePub);
            }
        QueryResult<CoursePub> queryResult = new QueryResult<>();
        //System.out.println("list:"+list);
        queryResult.setList(list);
        queryResult.setTotal(totalHits);
        QueryResponseResult<CoursePub> coursePubQueryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
        //返回结果
        //coursePubQueryResult.setList(list);
        return coursePubQueryResponseResult;
    }

    /*
        根据id查询课程信息
     */
    @Override
    public Map<String, CoursePub> gettall(String id) {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置类型
        searchRequest.types(type);
        //创建搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //根据课程id查询
        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));
        //取消source源字段过虑，查询所有字段
        //searchSourceBuilder.fetchSource(new String[]{"name","grade","charge","pic","id","teachplan"},new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        //执行搜索
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        Map<String,CoursePub> map = new HashMap<>();
        for (SearchHit searchHit : searchHits) {
            //String courseId = searchHit.getId();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String courseId = (String)sourceAsMap.get("id");
            String name = (String)sourceAsMap.get("name");
            String grade = (String)sourceAsMap.get("grade");
            String charge = (String)sourceAsMap.get("charge");
            String pic = (String)sourceAsMap.get("pic");
            String description = (String)sourceAsMap.get("description");
            String teachplan = (String)sourceAsMap.get("teachplan");
            CoursePub coursePub = new CoursePub();
            coursePub.setId(courseId);
            coursePub.setName(name);
            coursePub.setGrade(grade);
            coursePub.setPic(pic);
            coursePub.setDescription(description);
            coursePub.setTeachplan(teachplan);
            map.put(courseId,coursePub);
        }
        return map;
    }
    //根据id删除文档
    @Override
    public ResponseResult deleteEsById(String id) {
        //删除索引对象
        DeleteRequest deleteRequest = new DeleteRequest("xuecheng_index", "xc_course", id);
        //相应对象
        DeleteResponse delete = null;
        try {
            delete = restHighLevelClient.delete(deleteRequest);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
        //获取响应结果
        DocWriteResponse.Result result = delete.getResult();
        //System.out.println(result);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询媒资信息
    @Override
    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(media_index);
        //设置类型
        searchRequest.types(media_type);
        //创建搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //取出source源字段过滤
        String[] source_fields = media_source_field.split(",");

        searchSourceBuilder.fetchSource(source_fields, new String[]{});

        //String teachplanId = teachplanIds[0];
        //查询条件,根据课程计划id查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            //执行搜索
             searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取搜索结果
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        Map<String,CoursePub> map = new HashMap<>();
        //数据列表
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //取出课程计划信息
            String courseId = (String)sourceAsMap.get("courseid");
            String media_id = (String)sourceAsMap.get("media_id");
            String media_url = (String)sourceAsMap.get("media_url");
            String teachplan_id = (String)sourceAsMap.get("teachplan_id");
            String media_fileoriginalname = (String)sourceAsMap.get("media_fileoriginalname");
            teachplanMediaPub.setCourseId(courseId);
            teachplanMediaPub.setMediaId(media_id);
            teachplanMediaPub.setMediaUrl(media_url);
            teachplanMediaPub.setTeachplanId(teachplan_id);
            teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
            //将数据加入列表
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        //构建返回课程媒资信息对象
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList);
        QueryResponseResult<TeachplanMediaPub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }
}
