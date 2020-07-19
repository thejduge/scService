package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.manage_course.client.CmsPageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    @Autowired
    TeachPlanMapper teachPlanMapper;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
//    @Autowired
//    CourseMapper courseMapper;
//    @Test
//    public void testCourseBaseRepository(){
//        Optional<CourseBase> optional = courseBaseRepository.findById("402885816240d276016240f7e5000002");
//        if(optional.isPresent()){
//            CourseBase courseBase = optional.get();
//            System.out.println(courseBase);
//        }
//
//    }
//
//    @Test
//    public void testCourseMapper(){
//        CourseBase courseBase = courseMapper.findCourseBaseById("402885816240d276016240f7e5000002");
//        System.out.println(courseBase);
//
//    }
    @Test
    public void testTeachPlan(){
        TeachplanNode teachplanNode = teachPlanMapper.selectList("402885816243d2dd016243f24c030002");
        System.out.println("获取的课程计划是"+teachplanNode);
    }

    @Test
    public void feignTest(){
        CmsPage byId = cmsPageClient.findById("5b3469f794db44269cb2bff1");
        System.out.println("CmsPage:"+byId);
    }

    @Test
    public void findTeachplanMedia(){
        Optional<TeachplanMedia> byId = teachplanMediaRepository.findById("4028e58161bd3b380161bd3fe9220008");
        TeachplanMedia teachplanMedia = byId.get();
        System.out.println("teachplanMedia:"+teachplanMedia);
    }
}
