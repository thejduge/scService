package com.xuecheng.manage_cms_client.service.impl;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.Optional;

@Service
@Repository
public class PageServiceImpl implements PageService {
    //添加日志
    private static final Logger LOGGER = LoggerFactory.getLogger(PageServiceImpl.class);
    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private CmsSiteRepository cmsSiteRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    /**
     * 保存HTML文件到服务器上(服务器的物理路径)
     * @param pageId
     */
    @Override
    public void savePageToServerPath(String pageId) {

        //1. 从GridFS中获取html文件
        //1.0 根据pageId查询页面信息;再从cms_page集合中得到html的文件id htmlFileId
        CmsPage cmsPage = this.findCmsPageById(pageId);
        String htmlFileId = cmsPage.getHtmlFileId();
        //1.1 根据 htmlFileId从GridFS中获取文件内容
        InputStream inputStream = this.getHemlFileById(htmlFileId);
        if (inputStream == null){
            LOGGER.error("getHemlFileById InputStream is null , htmlFileId : {}",htmlFileId);
            return;
        }
        //2. 保存HTML文件到服务器上(服务器的物理路径)
        //2.0 根据站点id获得站点信息;获取站点的物理路径
        String siteId = cmsPage.getSiteId();
        CmsSite cmsSite = this.findSiteById(siteId);
        String sitePhysicalPath = cmsSite.getSitePhysicalPath();
        if (StringUtils.isEmpty(sitePhysicalPath)){
            LOGGER.error("sitePhysicalPath is null !");
            return;
        }
        //2.1 获取页面的物理路径,页面名称
        String pagePhysicalPath = cmsPage.getPagePhysicalPath();
        String pageName = cmsPage.getPageName();
        //2.2 往服务器上存储的路径是
        String pagePath = sitePhysicalPath + pagePhysicalPath + pageName;
        //2.3 保存文件到服务器上
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new File(pagePath));
            IOUtils.copy(inputStream,outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //根据站点id获得站点信息
    public CmsSite findSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (optional.isPresent()){
            CmsSite cmsSite = optional.get();
            return cmsSite;
        }
        return null;
    }
    //根据 htmlFileId从GridFS中获取文件内容  fileId = htmlFileId : 文件id
    public InputStream getHemlFileById(String fileId) {
        //根据文件id获取文件对象
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //定义GridFsResource
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        //返回下载流
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    //根据pageId查询页面信息
    public CmsPage findCmsPageById(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            LOGGER.error("findCmsPageById return null !");
        }
        CmsPage cmsPage = optional.get();
        return cmsPage;
    }
}
