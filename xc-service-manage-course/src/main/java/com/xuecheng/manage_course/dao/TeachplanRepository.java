package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//课程计划
public interface TeachplanRepository extends JpaRepository<Teachplan,String> {
    //根据课程id和父节点id查询节点列表,可以查询根节点
    public List<Teachplan> findByCourseidAndParentid(String courseId,String parentId);
}
