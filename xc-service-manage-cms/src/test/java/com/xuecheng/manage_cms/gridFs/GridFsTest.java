package com.xuecheng.manage_cms.gridFs;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTest {
    //注入GridFsTemplate
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    //存文件
    @Test
    public void testGridStore() throws FileNotFoundException {
        File file = new File("D:/develop/IdeaProjects/xcEduService/test-freemarker/src/main/resources/templates/course.ftl");

        FileInputStream fileInputStream = new FileInputStream(file);

        // 5eccc9b2dd6a32467063ca51 : course.ftl
        ObjectId objectId = gridFsTemplate.store(fileInputStream, "course.ftl");
        System.out.println("文件id为:"+objectId);
    }

    //取文件
    @Test
    public void queryFile() throws IOException {
        //根据文件di查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("5e7f15ac7a6e771394f067ca")));
        //打开一个下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建GridFsResource,获取流
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        //从流中获取数据
        String context = IOUtils.toString(gridFsResource.getInputStream());
        System.out.println(context);
    }
}
