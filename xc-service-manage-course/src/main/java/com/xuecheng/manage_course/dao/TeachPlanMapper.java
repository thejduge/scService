package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by Administrator.
 */
@Mapper
public interface TeachPlanMapper {
   //课程计划查询
   public TeachplanNode selectList(String courseId);
}
