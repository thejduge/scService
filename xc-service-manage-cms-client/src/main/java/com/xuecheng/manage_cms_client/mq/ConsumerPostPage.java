package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import com.xuecheng.manage_cms_client.service.impl.PageServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * 发布页面的消费客户端，监听 页面发布队列的消息，收到消息后从mongodb下载文件，保存在本地。
 */
@Component
public class ConsumerPostPage {
    //添加日志
    private static final Logger LOGGER = LoggerFactory.getLogger(PageServiceImpl.class);
    @Autowired
    private PageService pageService;
    @Autowired
    private CmsPageRepository cmsPageRepository;
    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void postPage(String msg){
        //1. 解析消息
        Map map = JSON.parseObject(msg, Map.class);
        //2. 获取消息中的页面id
        String pageId = (String) map.get("pageId");
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            LOGGER.error("receive cms post page,cmsPage is null , pageId : {}",pageId);
            return;
        }
        //3. 拿着pageID调用pageService将页面从GridFS下载到服务器
        pageService.savePageToServerPath(pageId);
    }
}
