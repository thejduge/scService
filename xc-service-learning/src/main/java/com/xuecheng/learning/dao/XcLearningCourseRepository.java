package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.learning.XcLearningCourse;
import org.springframework.data.jpa.repository.JpaRepository;

//学生选课Dao
public interface XcLearningCourseRepository extends JpaRepository<XcLearningCourse,String> {
    //根据用户id和课程查询选课记录,用于判断是否添加选课
    XcLearningCourse findXcLearningCourseByUserIdAndAndCourseId(String userId ,String courseId);
}
