package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

//课程管理
@RestController
@RequestMapping("/course")
public class CourseController extends BaseController implements CourseControllerApi {

    @Autowired
    CourseService courseService;

    //课程计划查询
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable String courseId) {
        //System.out.println("courseId:"+courseId);
        return courseService.findTeachplanList(courseId);
    }

    /**
     * 添加课程计划
     * @param teachplan  form表单
     * @return
     */
    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachplanList(@RequestBody Teachplan teachplan) {
        return courseService.addTeachplan(teachplan);
    }
    /*
        添加课程图片
     */
    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId,@RequestParam("pic") String pic) {
        System.out.println("courseId:"+courseId+"pic:"+pic);
        return courseService.saveCoursePic(courseId,pic);
    }

    /*
        查询课程图片
     */
    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
        return courseService.findCoursePic(courseId);
    }

    /*
        删除课程图片
     */
    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }

    /*
        课程视图查询
     */
    @Override
    @GetMapping("/courseview/{courseId}")
    public CourseView courseview(@PathVariable("courseId") String courseId) {
        return courseService.getCourseview(courseId);
    }
    /*
        课程预览
     */
    @Override
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);
    }

    /*
        课程发布
     */
    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable("id") String id) {
        //System.out.println("课程id为:"+id);
        return courseService.coursepublish(id);
    }

    /**
     * 查询我的课程
     * @param page
     * @param size
     * @param courseListRequest
     * @return
     */
    @Override
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult<CourseInfo> findCourseList(@PathVariable("page") int page,@PathVariable("size") int size, CourseListRequest courseListRequest) {
//        //使用静态数据
//        String companyId = "1";
        //使用工具类获取用户信息
        XcOauth2Util xcOauth2Util = new XcOauth2Util();
        XcOauth2Util.UserJwt userJwtFromHeader = xcOauth2Util.getUserJwtFromHeader(request);
        if (userJwtFromHeader == null){
            ExceptionCast.cast(CommonCode.UNAUTHENTICATED);
        }
        String companyId = userJwtFromHeader.getCompanyId();
        return courseService.findCourseList(companyId,page,size,courseListRequest);
    }
    /**
     *新增课程
     * @param courseBase
     * @return
     */
    @Override
    @PostMapping("/coursebase/add")
    public ResponseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    /*
        查询课程信息
     */
    @Override
    //@PreAuthorize("hasAuthority('course_get_baseinfo')")
    @GetMapping("/coursebase/{courseId}")
    public CourseBase getCourseBaseById(@PathVariable("courseId") String courseId) {
        //System.out.println(courseId);
        return courseService.getCourseBaseById(courseId);
    }

    /*
        修改课程信息
     */
    @Override
    @PutMapping("/coursebase/update/{id}")
    public ResponseResult updateCourseBase(@PathVariable("id") String id, @RequestBody CourseBase courseBase) {
        //System.out.println("id:"+id+"course:"+courseBase);
        return courseService.updateCourseBase(id,courseBase);
    }

    /*
        获取课程营销信息
     */
    @Override
    @GetMapping("/coursemarket/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        //System.out.println("courseId:"+courseId);
        return courseService.findCourseMarketById(courseId);
    }

    /*
        更新课程营销信息
     */
    @Override
    @PutMapping("/coursemarket/update/{id}")
    public ResponseResult updateCourseMarketById(@PathVariable("id") String id, @RequestBody CourseMarket courseMarket) {
        //System.out.println("id:"+id+"CourseMarket:"+courseMarket);
        return courseService.updateCourseMarketById(id,courseMarket);
    }

    /*
        保存媒资信息
     */
    @Override
    @PostMapping("/savemedia")
    public ResponseResult savemedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.savemedia(teachplanMedia);
    }
}
