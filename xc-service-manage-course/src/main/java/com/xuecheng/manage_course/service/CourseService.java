package com.xuecheng.manage_course.service;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    TeachPlanMapper teachPlanMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CoursePubRepository coursePubRepository;
    @Autowired
    CmsPageClient cmsPageClient;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Value("${course-publish.siteId}")
    private String siteId;
    @Value("${course-publish.templateId}")
    private String templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;
    @Value("${course-publish.pageWebPath}")
    private String pageWebPath;
    @Value("${course-publish.pagePhysicalPath}")
    private String pagePhysicalPath;
    @Value("${course-publish.dataUrlPre}")
    private String dataUrlPre;

    //课程计划查询
    public TeachplanNode findTeachplanList(String courseId) {
        return teachPlanMapper.selectList(courseId);
    }
    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        //1.校验课程计划名称和课程的id
        if (teachplan==null|| StringUtils.isEmpty(teachplan.getPname())||StringUtils.isEmpty(teachplan.getCourseid())){
            ExceptionCast.cast(CommonCode.INVALID_PARAMETER);//非法参数
        }
        //2.判断课程计划属于哪个节点
        //取出课程id
        String courseId = teachplan.getCourseid();
        //取出父节点id
        String parentId = teachplan.getParentid();
        if (StringUtils.isEmpty(parentId)){
            //父节点为空,取出根节点
            parentId = getTeachPlanRoot(courseId);
        }
        //取出父节点信息
        Optional<Teachplan> teachPlanOptional = teachplanRepository.findById(parentId);
        if (!teachPlanOptional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAMETER);//父节点为空
        }
        //转换父节点课程计划对象
        Teachplan teachPlanParent = teachPlanOptional.get();
        //获取父节点等级
        String gradeParent = teachPlanParent.getGrade();
        //设置将父节点加入子节点
        teachplan.setParentid(parentId);
        teachplan.setStatus("0");//发布状态设置为:"0" 未发布
        //判断父节点级别设置子节点级别
        if (gradeParent.equals("1")){//父根节点
            teachplan.setGrade("2");
        }else if (gradeParent.equals("2")){
            teachplan.setGrade("3");
        }
        //设置课程id
        teachplan.setCourseid(teachPlanParent.getCourseid());
        //3.添加课程计划
         teachplanRepository.save(teachplan);
        //返回结果
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //获取根结点id
    private String getTeachPlanRoot(String courseId) {
        //获取父节点id
        //校验课程id
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()){
            return null;
        }
        //课程信息
        CourseBase courseBase = optional.get();
        //取出课程计划根节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList == null || teachplanList.size() == 0){//没有根节点
            //创建根节点
            Teachplan teachplanRoot = new Teachplan();
            teachplanRoot.setCourseid(courseId);
            teachplanRoot.setPname(courseBase.getName());
            teachplanRoot.setParentid("0");
            teachplanRoot.setGrade("1");
            teachplanRoot.setStatus("0");
            //添加课程 即添加根节点
            teachplanRepository.save(teachplanRoot);
            return teachplanRoot.getId();
        }
        Teachplan teachplan = teachplanList.get(0);
        return teachplan.getId();
    }

    /*
       查询我的课程
     */
    public QueryResponseResult<CourseInfo> findCourseList(String companyId,int page, int size, CourseListRequest courseListRequest) {
        if (courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        //企业id
        courseListRequest.setCompanyId(companyId);
        if (page<=0){
            page = 0;
        }
        if (size<=0){
            size = 10;
        }
        PageHelper.startPage(page,size);
        //分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        if (courseListPage.size()==0){
            return new QueryResponseResult<CourseInfo>(CourseCode.COURSE_NOTDOUND,null);
        }
        //总页数
        long total = courseListPage.getTotal();
        //课程集合
        List<CourseInfo> result = courseListPage.getResult();
        //封装数据
        QueryResult<CourseInfo> queryResult = new QueryResult<>();
        queryResult.setTotal(total);
        queryResult.setList(result);
        //返回数据
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS,queryResult);
    }
    //新增课程
    @Transactional
    public ResponseResult addCourseBase(CourseBase courseBase) {
        //校验参数
        if (courseBase==null||StringUtils.isEmpty(courseBase.getName())||StringUtils.isEmpty(courseBase.getMt())){
            ExceptionCast.cast(CommonCode.INVALID_PARAMETER);
        }
        //注意需要添加课程发布状态
        courseBase.setStatus("202001");//未发布

        //添加课程
        courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //查询课程信息
    public CourseBase getCourseBaseById(String courseId) {
        if (courseId == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAMETER);
        }
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
            return null;
        }
        return optional.get();
    }
    //修改课程信息
    @Transactional
    public ResponseResult updateCourseBase(String id, CourseBase courseBase) {
        //根据id查询课程信息
        CourseBase courseBaseAs = this.getCourseBaseById(id);
        if (courseBaseAs!=null){//更新信息
            courseBaseAs.setName(courseBase.getName());
            courseBaseAs.setUsers(courseBase.getUsers());
            courseBaseAs.setMt(courseBase.getMt());
            courseBaseAs.setSt(courseBase.getSt());
            courseBaseAs.setGrade(courseBase.getGrade());
            courseBaseAs.setStudymodel(courseBase.getStudymodel());
            courseBaseAs.setTeachmode(courseBase.getTeachmode());
            courseBaseAs.setDescription(courseBase.getDescription());
            courseBaseAs.setStatus(courseBase.getStatus());
            courseBaseAs.setCompanyId(courseBase.getCompanyId());
            courseBaseAs.setUserId(courseBase.getUserId());
            //执行更新
            courseBaseRepository.save(courseBaseAs);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //更新失败
        return new ResponseResult(CommonCode.FAIL);
    }
    //查询课程营销信息
    public CourseMarket findCourseMarketById(String courseId) {
        if (courseId ==null){
            ExceptionCast.cast(CommonCode.INVALID_PARAMETER);
        }
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if (!optional.isPresent()){
            //ExceptionCast.cast(CommonCode.FAIL);
            return null;
        }
        return optional.get();
    }
    //更新课程营销信息
    public ResponseResult updateCourseMarketById(String id, CourseMarket courseMarket) {
        if (id==null){
            ExceptionCast.cast(CommonCode.INVALID_PARAMETER);
        }
        //查询课程营销信息
        CourseMarket marketById = this.findCourseMarketById(id);
        //判断是否存在
        if (marketById==null){//不存在,执行添加操作
            CourseMarket save = courseMarketRepository.save(courseMarket);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //执行更新操作
        marketById.setCharge(courseMarket.getCharge());
        marketById.setValid(courseMarket.getValid());
        marketById.setQq(courseMarket.getQq());
        marketById.setPrice(courseMarket.getPrice());
        marketById.setPrice_old(courseMarket.getPrice_old());
        marketById.setStartTime(courseMarket.getStartTime());
        marketById.setEndTime(courseMarket.getEndTime());
        //执行更新操作
        CourseMarket save = courseMarketRepository.save(marketById);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //保存课程图片
    @Transactional
    public ResponseResult saveCoursePic(String courseId, String pic) {
        //查询课程图片
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (optional.isPresent()){//课程图片存在
            coursePic = optional.get();
        }
        //课程图片不存在新建对象
        if (coursePic == null){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        //保存课程图片
        coursePicRepository.save(coursePic);
        //System.out.println("图片保存到数据库成功");
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //查询课程图片
    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> byId = coursePicRepository.findById(courseId);
        if (byId.isPresent()){
            CoursePic coursePic = byId.get();
            return coursePic;
        }
        return null;
    }
    //删除课程图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        long result = coursePicRepository.deleteByCourseid(courseId);
        if (result>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }
    //课程视图查询
    public CourseView getCourseview(String courseId) {
        CourseView courseView = new CourseView();
        //查询课程基本信息
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(courseId);
        if (optionalCourseBase.isPresent()){
            CourseBase courseBase = optionalCourseBase.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程营销信息
        Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(courseId);
        if (optionalCourseMarket.isPresent()){
            CourseMarket courseMarket = optionalCourseMarket.get();
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程图片信息
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(courseId);
        if (optionalCoursePic.isPresent()){
            CoursePic coursePic = optionalCoursePic.get();
            courseView.setCoursePic(coursePic);
        }
        //查询课程计划信息
        TeachplanNode teachplanNode = teachPlanMapper.selectList(courseId);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }
    //课程预览
    public CoursePublishResult preview(String id) {
        //根据id查询课程信息
        CourseBase one = this.findCourseBaseById(id);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(siteId);
        //模板
        cmsPage.setTemplateId(templateId);
        //页面名称
        cmsPage.setPageName(id+".html");
        //页面别名
        cmsPage.setPageAliase(one.getName());
        //页面访问路径
        cmsPage.setPageWebPath(pageWebPath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(pagePhysicalPath);
        //数据url
        cmsPage.setDataUrl(dataUrlPre+id);
        //远程请求cms保存页面
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if (!cmsPageResult.isSuccess()){
           return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        //页面url
        String url = previewUrl+pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,url);
    }
    //根据id查询课程信息
    private CourseBase findCourseBaseById(String id) {
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()){
            return optional.get();
        }
        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }

    //课程发布
    @Transactional
    public CoursePublishResult coursepublish(String id) {
        //查询课程信息
        CourseBase one = this.findCourseBaseById(id);
        //发布课程详情页面
        CmsPostPageResult cmsPostPageResult = publish_page(id);
        if (!cmsPostPageResult.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //更新课程状态
        CourseBase courseBase = saveCoursePubState(id);
        //创建课程索引
        //创建课程索引信息
        CoursePub coursePub = createCoursePub(id);
        //向数据库保存课程索引信息
        CoursePub newCoursePub = saveCoursePub(id,coursePub);
        if (newCoursePub == null){
            //创建课程索引信息失败
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_CREATE_INDEX_ERROR);
        }
        //保存课程计划媒资信息到待索引表
        saveTeachplanMediaPub(id);
        //获取页面url
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }
    //保存课程计划媒资信息到待索引表
    private void saveTeachplanMediaPub(String id) {
        //查询课程媒资信息
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(id);
        //将课程计划媒资信息存储到待索引表
        teachplanMediaPubRepository.deleteByCourseId(id);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }
    //向数据库保存课程索引信息
    private CoursePub saveCoursePub(String id, CoursePub coursePub) {
        //参数校验
        if (StringUtils.isEmpty(id)){
            //System.out.println("id:"+id);
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        CoursePub coursePubNew = null;
        Optional<CoursePub> optionalCoursePub = coursePubRepository.findById(id);
        if (optionalCoursePub.isPresent()){
            coursePubNew = optionalCoursePub.get();
        }
        if (coursePubNew == null){
            coursePubNew = new CoursePub();
        }
        BeanUtils.copyProperties(coursePub,coursePubNew);
        //主键
        coursePubNew.setId(id);
        //更新时间戳
        coursePub.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }
    //创建课程索引信息
    private CoursePub createCoursePub(String id) {
        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);

        //基础信息
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(id);
        if (optionalCourseBase.isPresent()){
            CourseBase courseBase = optionalCourseBase.get();
            BeanUtils.copyProperties(courseBase,coursePub);
        }
        //课程图片
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(id);
        if (optionalCoursePic.isPresent()){
            CoursePic coursePic = optionalCoursePic.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(id);
        if (optionalCourseMarket.isPresent()){
            CourseMarket courseMarket = optionalCourseMarket.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }
        //课程计划
        TeachplanNode teachplanNode = teachPlanMapper.selectList(id);
        //将课程计划转换成json
        String teachplanStr = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplanStr);
        return coursePub;
    }

    //更新课程状态
    private CourseBase saveCoursePubState(String id) {
        CourseBase courseBase = this.findCourseBaseById(id);
        //更新发布状态
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }

    //发布课程详情页面
    private CmsPostPageResult publish_page(String id) {
        CourseBase one = this.findCourseBaseById(id);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(siteId);
        //模板
        cmsPage.setTemplateId(templateId);
        //页面名称
        cmsPage.setPageName(id+".html");
        //页面别名
        cmsPage.setPageAliase(one.getName());
        //页面访问路径
        cmsPage.setPageWebPath(pageWebPath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(pagePhysicalPath);
        //数据url
        cmsPage.setDataUrl(dataUrlPre+id);
        //发布页面
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        return cmsPostPageResult;
    }

    //保存媒资信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        //参数校验
        if (teachplanMedia == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAMETER);
        }
        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询课程计划
        Optional<Teachplan> optionalTeachplan = teachplanRepository.findById(teachplanId);
        if (!optionalTeachplan.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        Teachplan teachplan = optionalTeachplan.get();
        //只允许子节点添加课程计划视频
        String grade = teachplan.getGrade();
        if (!grade.equals("3")||StringUtils.isEmpty(grade)){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        TeachplanMedia one = null;
        //判断是否存在课程媒资信息
//        TeachplanMedia optionalTeachplanMedia = teachplanMediaRepository.findByTeachplan_id(teachplanId);
//        if (optionalTeachplanMedia == null){
//            one = new TeachplanMedia();
//        }else {
//            one = optionalTeachplanMedia;
//        }
        Optional<TeachplanMedia> optionalTeachplanMedia = teachplanMediaRepository.findById(teachplanId);
        if (!optionalTeachplanMedia.isPresent()){//不存在
            one = new TeachplanMedia();
        }else {
            one = optionalTeachplanMedia.get();
        }

        //保存媒资信息
        one.setCourseId(teachplanMedia.getCourseId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        one.setTeachplanId(teachplanMedia.getTeachplanId());
        teachplanMediaRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

}
