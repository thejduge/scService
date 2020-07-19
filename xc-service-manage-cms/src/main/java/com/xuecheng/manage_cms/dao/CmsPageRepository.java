package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
    //根据cms_page中的页面名称、站点Id、页面webpath来确定一个唯一的页面
    public CmsPage findPageByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String pageWebPath);
}
