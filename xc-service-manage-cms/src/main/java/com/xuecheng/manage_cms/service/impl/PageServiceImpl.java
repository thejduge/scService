package com.xuecheng.manage_cms.service.impl;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.framework.utils.CookieUtil;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import com.xuecheng.manage_cms.service.PageService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageServiceImpl implements PageService {
    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private CmsConfigRepository cmsConfigRepository;
    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    /**
     * 页面查询方法 条件查询
     *
     * @param page             页码从1开始计数;
     * @param size
     * @param queryPageRequest
     * @return
     */
    @Override
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        if (queryPageRequest == null) { //避免空指针问题  此处可以不写 queryPageRequest这个对象是框架给我们创建的
            queryPageRequest = new QueryPageRequest();
        }
        //自定义条件匹配器(模糊条件查询)
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //设置查询的值对象
        CmsPage cmsPage = new CmsPage();
        //根据站点id查询
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //根据页面类型查询
        if (StringUtils.isNotEmpty(queryPageRequest.getPageType())){
            cmsPage.setPageType(queryPageRequest.getPageType());
        }
        //根据模板id查询
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }

        //根据别名查询(模糊查询)
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //定义条件对象
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        //分页参数
        if (page <= 0) {
            page = 1;//  默认分页是从0开始;这里定义为1,
        }
        page = page - 1;
        if (size <= 0) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);
        //实现自定义条件查询
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        //将查询到的数据进行封装返回
        QueryResult queryResult = new QueryResult();
        queryResult.setTotal(all.getTotalElements());//数据总记录数
        queryResult.setList(all.getContent());//数据列表
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    /**
     * 新增页面 判断用户要新增的页面是否已经存在;以存在则不能新增  进行异常处理
     * @param cmsPage
     * @return
     */
    @Override
    public CmsPageResult add(CmsPage cmsPage) {
        if (cmsPage == null){
            //抛出异常,非法参数异常
            ExceptionCast.cast(CmsCode.CMS_PARAMETER_INVALID);
        }
        //根据cms_page中的页面名称、站点Id、页面webpath来确定一个唯一的页面
        CmsPage page = cmsPageRepository.findPageByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (page != null) {
            //页面已经存在;抛出异常,异常内容显示页面已存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        cmsPage.setPageId(null);//添加页面主键由spring data 自动生成
        cmsPageRepository.save(cmsPage);
        //返回结果
        return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
    }

    /**
     * 根据id查找页面
     *
     * @param id
     * @return
     */
    @Override
    public CmsPage findById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            //查询到数据
            return optional.get();
        }
        //没有查询到数据
        return null;
    }

    /**
     * 根据id修改页面信息,cmsPage中封装着要修改的信息
     *
     * @param id
     * @param cmsPage
     * @return
     */
    @Override
    public CmsPageResult edit(String id, CmsPage cmsPage) {
        //1.根据id查询到页面 判断页面是否为空 为空直接返回
        CmsPage page = this.findById(id);//使用this调用上边定义的使用id查找页面的方法
        if (page != null) {
            //更新模板id
            page.setTemplateId(cmsPage.getTemplateId());
            //跟新所属站点
            page.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            page.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            page.setPageName(cmsPage.getPageName());
            //更新访问路径
            page.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            page.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新数据url
            page.setDataUrl(cmsPage.getDataUrl());
            //执行更新(提交修改)
            cmsPageRepository.save(page);
            //更新成功,返回
            return new CmsPageResult(CommonCode.SUCCESS, page);
        }
        //没有查询到页面,直接返回
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 根据页面的主键id删除页面
     * @param id
     * @return
     */
    @Override
    public ResponseResult delete(String id) {
        //根据提供的id查询页面是否存在
        CmsPage cmsPage = this.findById(id);
        //判断是否查询到数据
        if (cmsPage != null){
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 根据id查询comconfig
     * @param id
     * @return
     */
    @Override
    public CmsConfig getModelById(String id) {
        //根据id查询配置管理信息
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    /**
     * 页面静态化方法
     * @param pageId
     * @return
     */
    @Override
    public String getPageHtml(String pageId){
        //1.获取数据模型
        Map model = this.getModelByPageId(pageId);
        if (model == null){
            //使用自定义异常类捕捉提示
        }
        //2.获取模板
        String templateContent = this.getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(templateContent)){
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_TEMPLATE_FAIL);
        }
        //3.执行静态化
        String content = this.generateHtml(templateContent, model);
        return content;
    }

    /**
     * 页面发布
     * @param pageId
     * @return
     */
    @Override
    public ResponseResult post(String pageId) {
        //1. 静态化页面
        String pageHtml = this.getPageHtml(pageId);
        //1.1 判断执行静态化数据
        if (StringUtils.isEmpty(pageHtml)){
            // 数据为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //2. 保存静态化的文件
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);
        //3. 向mq发送消息
        this.sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 保存页面 已存在,更新  不存在 添加
     * @param cmsPage
     * @return
     */
    @Override
    public CmsPageResult save(CmsPage cmsPage) {
        //判断页面上会否存在
        CmsPage page = cmsPageRepository.findPageByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (page != null){
            //页面存在,执行更新
            return this.edit(page.getPageId(),cmsPage);
        }
        return this.add(cmsPage);
    }

    /**
     * 一键发布
     * @param cmsPage
     * @return
     */
    @Override
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        if (cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PARAMETER_INVALID);
        }
        //将页面信息存储到cms_page中
        CmsPageResult save = this.save(cmsPage);
        if (!save.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //从save中获取页面id
        CmsPage saveCmsPage = save.getCmsPage();
        String pageId = saveCmsPage.getPageId();
        //执行页面的发布(静态化、保存到GridFS、向MQ发送消息)
        ResponseResult post = this.post(pageId);
        if (!post.isSuccess()){
            ExceptionCast.cast(CmsCode.CMS_POSTPAGE_FAIL);
        }
        //发布成功,拼接pageUrl进行封装返回
        String siteId = saveCmsPage.getSiteId();
        //获得站点信息
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        //拼装pageUrl
        String pageUrl = cmsSite.getSiteDomain() + cmsSite.getSiteWebPath() + saveCmsPage.getPageWebPath() + saveCmsPage.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }

    /**
     * 根据站点id查询站点信息
     * @param siteId
     * @return
     */
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_SITE_FAIL);
        }
        return optional.get();
    }

    /**
     * 向mq发送消息
     * @param pageId
     */
    public void sendPostPage(String pageId){
        //1. 判断该页面是否存在
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            //页面为空
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        //2. 创建容器存储消息
        Map<String,String> msgMap = new HashMap<>();
        msgMap.put("pageId",cmsPage.getPageId());
        // 消息内容转为json
        String msg = JSON.toJSONString(msgMap);
        //3. 获取站点id作为routingKey发布消息
        String siteId = cmsPage.getSiteId();
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,msg);
    }
    /**
     * 保存静态化的文件
     * @param pageId
     * @param content
     * @return
     */
    public CmsPage saveHtml(String pageId,String content){
        //1. 保存文件到GridFS
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            //查询结果为空
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        ObjectId objectId = null;
        try {
            InputStream inputStream = IOUtils.toInputStream(content, "UTF-8");
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //2. 更新保存后的html文件id到CmsPage中
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }


    /**
     * 根据页面id获取页面的数据模型
     * @param pageId
     * @return
     */
    private Map getModelByPageId(String pageId){
        //1.查询页面信息
        CmsPage page = this.findById(pageId);
        if (page == null){
            //页面不存在
            //Excep
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //2.从查询到的页面获取dataurl
        String dataUrl = page.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)){
            //使用自定义异常类捕获进行提示
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }

        //定义请求头
        LinkedMultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        //从Cookie中获取令牌
        //String access_token = getTokenFormCookie();
        //String access_token = "95300ba2-73ea-44db-8e6d-9d1bcd4cb221";
        //从redis中获取令牌
        //String userTokenStr = stringRedisTemplate.opsForValue().get("user_token:" + access_token);
       // AuthToken authToken = new AuthToken();
//        if (userTokenStr !=null ){
//            authToken = JSON.parseObject(userTokenStr, AuthToken.class);
//        }
        //String jwt_token = authToken.getJwt_token();
       // headers.add("Authorization",("Bearer "+jwt_token));
        //定义请求
       // HttpEntity<MultiValueMap<String,String>> multiValueMapHttpEntity = new HttpEntity<>(null,headers);
        //指定restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
//        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
//            @Override
//            public void handleError(ClientHttpResponse response)throws IOException {
//                //当响应的值为400或401时候也要正常响应，不要抛出异常
//                if (response.getRawStatusCode() !=400 && response.getRawStatusCode()!=401){
//                    super.handleError(response);
//                }
//            }
//        });
        //ResponseEntity<Map>  forEntity = restTemplate.exchange(dataUrl, HttpMethod.GET, multiValueMapHttpEntity, Map.class);

        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);

        Map map = forEntity.getBody();
//        Map map = new HashMap();
//        map.put("model",body);
        //返回数据模型
        return map;
    }
    //获取httpbasic的串
    private String httpbasic(String clientId, String clientSecret) {
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”拼接
        String str = clientId+":"+clientSecret;
        //将字符串进行base64编码
        byte[] encode = Base64Utils.encode(str.getBytes());
        return "Bearer "+new String(encode);
    }

    //从cookie中读取访问令牌
    private String getTokenFormCookie() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if (map != null && map.get("uid") != null){
            String uid = map.get("uid");
            return uid;
        }
        return null;
    }



    /**
     * 根据页面id获得模板文件
     * @param pageId
     * @return
     */
    private String getTemplateByPageId(String pageId){
        //1.查询页面信息
        CmsPage page = this.findById(pageId);
        if (page == null){
            //页面不存在
            //使用自定义异常类捕捉异常
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取模板id
        String templateId = page.getTemplateId();
        if (StringUtils.isEmpty(templateId)){
            //id为空
            ExceptionCast.cast(CmsCode.CMS_TEMPLATEID_ISNULL);
        }
        //根据templateId去cms_template表中查询templateFileId
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //在cms_template表中查询templateFileId
            String templateFileId = cmsTemplate.getTemplateFileId();
            //取出模板文件内容
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridFsResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                //System.out.println("根据页面id获得模板文件content:"+content);
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 执行页面静态化
     * @param templateContent
     * @param model
     * @return
     */
    private String generateHtml(String templateContent,Map model){
        //System.out.println(model);
        //创建配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //创建模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateContent);
        //向configuration配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板内容
        try {
            Template template = configuration.getTemplate("template");
            //调用api进行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
