package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@SpringBootTest //会去找该包下的启动类,继续执行启动类上的注解扫描bean
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {
    @Autowired
    CmsPageRepository cmsPageRepository;

    /**
     * 测试查询所有数据
     */
    @Test
    public void testFindAll(){
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    /**
     * 测试分页查询
     */
    @Test
    public void testFindPage(){
        //分页参数
        int page = 0;//从0开始
        int size = 10;

        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

    //修改
    @Test
    public void testUpdate(){
        //1.查询对象
        Optional<CmsPage> optional = cmsPageRepository.findById("5abefd525b05aa293098fca6");
        if (optional.isPresent()){
            //数据不为空
            CmsPage cmsPage = optional.get();
            //2.设置要修改的值
            cmsPage.setPageAliase("测试更新数据");
            //3.修改
            CmsPage save = cmsPageRepository.save(cmsPage);
            System.out.println(save);
        }
    }

    /*//根据页面名称查询
    @Test
    public void testfindByPageName(){
        CmsPage cmsPage = cmsPageRepository.findByPageName("测试页面");
        System.out.println(cmsPage);
    }*/

    /**
     * 自定义条件查询
     *
     */
    @Test
    public void testFindByExample(){
        //分页参数
        int page = 0;//从0开始
        int size = 10;
        Pageable pageable = PageRequest.of(page,size);

        //条件查询对象
        CmsPage cmsPage = new CmsPage();
        //假如要查询的是 5a754adf6abb500ad05688d9
        //cmsPage.setPageId("5a754adf6abb500ad05688d9");//站点id  精确查询
        //cmsPage.setTemplateId("5a962b52b00ffc514038faf7");//模板id  精确查询

        //别名查询   模糊查询
        cmsPage.setPageAliase("预览");

        //条件匹配器(默认是精确查询条件匹配器)
        //ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        //创建模糊条件查询匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        //定义Example
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);
        //查询数据库
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> content = all.getContent();
        System.out.println(content);
    }
}
