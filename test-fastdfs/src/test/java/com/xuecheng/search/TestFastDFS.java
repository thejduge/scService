package com.xuecheng.search;

import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

    //上传文件测试
    @Test
    public void upload(){
        try {
            //加载fastdfs配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义一个trackerClient,用于请求trackerServer
            TrackerClient trackerClient = new TrackerClient();
            //连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取stroage服务器
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建stroageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);
            //向stroage服务器上传文件
            //本地文件的路径
            String filePath = "E:/迪丽热巴.jpg";
            //  group1/M00/00/00/wKgZhV58gCSAaDKvACAW52EjSIw150.png
            String fileId = storageClient1.upload_file1(filePath, "jpg", null);
            System.out.println(fileId);//group1/M00/00/00/wKgZhV7LkS6Aan2JAAEd7j4XLvQ496.jpg
            //group1/M00/00/00/wKgZhV7LqraAKIx2AAHhxUREGqU093.jpg
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //下载文件测试
    @Test
    public void testDownload(){
        try {
            //加载配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //创建trackerClient请求trackerServer
            TrackerClient trackerClient = new TrackerClient();
            //连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取stroage服务器
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //创建stroageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);
            //下载文件
            String fileId = "group1/M00/00/00/wKgZhV7LqraAKIx2AAHhxUREGqU093.jpg";
            byte[] bytes = storageClient1.download_file1(fileId);
            //保存文件到本地
            FileOutputStream fileOutputStream = new FileOutputStream(new File("D:/test01.jpg"));
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
