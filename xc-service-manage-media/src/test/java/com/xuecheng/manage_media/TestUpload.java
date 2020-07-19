package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestUpload {
    //文件分块
    @Test
    public void testChunks() throws IOException {
        //源文件
        File sourseFile = new File("E:\\Java_Soft\\xczx\\video\\梦想的声音.mp4");
        //块文件目录
        String chunksFileFolder = "E:\\Java_Soft\\xczx\\video\\chunks\\";

        //定义块文件的大小
        long chunksFileSize = 1 * 1024 * 1024;
        //块数
        long chunksNumber = (long) Math.ceil(sourseFile.length() * 1.0 / chunksFileSize);

        //创建一个读文件的对象
        RandomAccessFile raf_read = new RandomAccessFile(sourseFile,"r");
        byte[] b = new byte[1024];
        for (long i = 0; i < chunksNumber; i++) {
            File chunkFile = new File(chunksFileFolder + i);
            //创建一个对象 将读到的文件写到磁盘
            RandomAccessFile raf_write = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = raf_read.read(b)) != -1){
                raf_write.write(b,0,len);
                //块文件的大小达到1m 开始读下一块
                if (chunkFile.length() >= chunksFileSize){
                    break;
                }
            }
            //关闭输出流
            raf_write.close();
        }
        //关闭输入流
        raf_read.close();
    }


    //文件合并
    @Test
    public void testMergeFile() throws IOException {
        //块文件的目录
        String chunksFilePath = "E:\\Java_Soft\\xczx\\video\\chunks\\";
        //块文件目录对象
        File chunksFileFolder = new File(chunksFilePath);
        //块文件列表
        File[] files = chunksFileFolder.listFiles();
        //将块文件按照名称升序排列  确保合并后的文件与源文件相同
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;
            }
        });

        //合并文件
        File mergeFile = new File("E:\\Java_Soft\\xczx\\video\\梦想的声音1.avi");
        //创建新文件
        boolean newFile = mergeFile.createNewFile();
        //创建写对象,向新文件中写入读取到的块文件内容
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        //创建缓冲区
        byte[] b = new byte[1024];
        //遍历fileList
        for (File chunkFile : fileList) {
            //创建读对象,读取块文件的信息
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
            int len = -1;
            while ((len = raf_read.read(b)) != -1){
                //将读到的数据写到新文件中
                raf_write.write(b,0,len);
            }
            //关闭输入流
            raf_read.close();
        }
        //关闭输出流
        raf_write.close();
    }
}
