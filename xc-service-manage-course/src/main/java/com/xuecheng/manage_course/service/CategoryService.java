package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.dao.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//课程分类service
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    //查询课程分类
    public CategoryNode findList() {
        CategoryNode categoryList = categoryMapper.findCategoryList();
        //System.out.println(categoryList);
        return categoryList;
    }
}
