package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 *页面管理Api
 */
@Api(value = "cms页面管理接口",description = "cms页面管理接口,提供页面的增、删、改、查")
public interface CmsPageControllerApi {
    @ApiOperation("分页查询页面列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true,paramType = "path",dataType = "int"),
            @ApiImplicitParam(name = "size",value = "每页记录数",required = true,paramType = "path",dataType = "int")
    })
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    @ApiOperation("新增页面")
    public CmsPageResult add(CmsPage cmsPage);

    @ApiOperation("根据id查询页面")
    @ApiImplicitParam(name = "id",value = "被查询页面的主键id",required = true,paramType = "path",dataType = "string")
    public CmsPage findById(String id);

    @ApiOperation("修改页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "被查询页面的主键id",required = true,paramType = "path",dataType = "string"),
            @ApiImplicitParam(name = "cmsPage",value = "修改的页面数据",required = true)
    })
    public CmsPageResult edit(String id,CmsPage cmsPage);

    @ApiOperation("删除页面")
    @ApiImplicitParam(name = "id",value = "被删除页面的主键id",required = true,paramType = "path",dataType = "string")
    public ResponseResult delete(String id);

    @ApiOperation("页面发布")
    @ApiImplicitParam(name = "pageId",value = "要发布的页面id",required = true,paramType = "path",dataType = "string")
    public ResponseResult post(String pageId);

    @ApiOperation("保存页面")
    public CmsPageResult save(CmsPage cmsPage);

    @ApiOperation("一键发布页面")
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
