package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms.service.PageService;
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
public class PageServiceTest {
    @Autowired
    PageService pageService;

    //测试getPageHtml
    @Test
    public void testGetPageHtml(){
        String pageHtml = pageService.getPageHtml("5e74d0893d523303e4e33b51");
        System.out.println(pageHtml);
    }

    @Test
    public void testFindPageById(){
        CmsPage page = pageService.findById("5e74d0893d523303e4e33b51");
        System.out.println(page);
    }
}
