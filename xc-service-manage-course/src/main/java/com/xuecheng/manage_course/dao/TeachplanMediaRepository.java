package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//课程媒资信息
public interface TeachplanMediaRepository extends JpaRepository<TeachplanMedia,String> {
   // public TeachplanMedia findByTeachplan_id(String teachplan_id);
    List<TeachplanMedia> findByCourseId(String courseId);
}
