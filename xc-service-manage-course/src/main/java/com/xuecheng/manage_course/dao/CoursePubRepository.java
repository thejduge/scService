package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePub;
import org.springframework.data.jpa.repository.JpaRepository;

//课程发布表管理
public interface CoursePubRepository extends JpaRepository<CoursePub,String> {
}
