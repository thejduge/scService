package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

import java.util.Map;

public interface EsCourseService {
    /**
     * 搜索课程
     * @param page
     * @param size
     * @param courseSearchParam
     * @return
     */
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam);
    //根据id查询课程信息
    Map<String, CoursePub> gettall(String id);
    //根据id删除文档
    ResponseResult deleteEsById(String id);

    QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds);
}
