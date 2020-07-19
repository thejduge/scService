package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


public interface PageService {
    /**
     * 页面查询
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    /**
     * 新增页面
     * @param cmsPage
     * @return
     */
    public CmsPageResult add(CmsPage cmsPage);

    //根据id查询页面
    public CmsPage findById(String id);

    //修改页面
    public CmsPageResult edit(String id,CmsPage cmsPage);

    /**
     * 根据页面的主键id删除页面
     * @param id
     * @return
     */
    public ResponseResult delete(String id);

    /**
     * 根据id查询cmsconfig
     * @param id
     * @return
     */
    public CmsConfig getModelById(String id);

    /**
     * 页面静态化方法
     * @param pageId
     * @return
     */
    public String getPageHtml(String pageId);

    /**
     * 页面发布
     * @param pageId
     * @return
     */
    public ResponseResult post(String pageId);

    /**
     * 保存页面
     * @param cmsPage
     * @return
     */
    public CmsPageResult save(@RequestBody CmsPage cmsPage);

    /**
     * 一键发布
     * @param cmsPage
     * @return
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
