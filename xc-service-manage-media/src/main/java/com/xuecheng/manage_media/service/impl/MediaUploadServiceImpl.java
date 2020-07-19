package com.xuecheng.manage_media.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

@Service
public class MediaUploadServiceImpl implements MediaUploadService {
    //private static final Logger LOGGER = LoggerFactory.getLogger();
    @Autowired
    private MediaFileRepository mediaFileRepository;
    //上传文件根目录
    @Value("${xc-service-manage-media.upload-location}")
    String uploadPath;
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routing_key_media_video;
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 得到文件的所属目录
     *
     * @param fileMd5
     * @return
     */
    private String getFileFolderPath(String fileMd5) {
        return uploadPath + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
    }

    /**
     * 得到文件的路径
     *
     * @param fileMd5
     * @param ext
     * @return
     */
    private String getFilePath(String fileMd5, String ext) {
        return uploadPath + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + "." + ext;
    }

    /**
     * 得到块文件所属目录路径
     * @param fileMd5
     * @return
     */
    private String getChunkFileFolderPath(String fileMd5){
        return  uploadPath + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/chunks/";
    }

    /**
     * 上传文件前对文件信息的校验 检查文件是否存在,创建目录
     * 根据文件md5得到文件路径
     * 规则:
     * 一级目录:md5的第一个字符
     * 二级目录:md5的第二个字符
     * 三级目录:md5
     * 文件名:md5 + 文件扩展名
     *
     * @param fileMd5
     * @param name
     * @param size
     * @param type
     * @param ext
     * @return
     */
    @Override
    public ResponseResult register(String fileMd5, String name, long size, String type, String ext) {
        //1. 检查文件在磁盘上存在不存在
        //文件所属目录
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //文件的所属目录
        String filePath = this.getFilePath(fileMd5, ext);
        File file = new File(filePath);
        boolean exists = file.exists();
        //2. 检查文件在mongodb中存在不存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (exists && optional.isPresent()) {
            //文件存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //文件不存在   检查文件的所在目录是否存在,不存在则创建
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            //文件的所在目录不存在
            fileFolder.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 分块检查
     *
     * @param fileMd5   文件的md5
     * @param chunk     分块的下标
     * @param chunkSize 分块稳健的额大小
     * @return
     */
    @Override
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //1. 检查分块是否已经存在   获得分块文件的所在目录
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //获取该分块的文件
        File chunkFile = new File(fileFolderPath + chunk);
        if (chunkFile.exists()) {
            //文件已经存在
            return new CheckChunkResult(CommonCode.SUCCESS, true);
        } else {
            return new CheckChunkResult(CommonCode.SUCCESS, false);
        }
    }

    /**
     * 上传分块
     *
     * @param file
     * @param chunk
     * @param fileMd5
     * @return
     */
    @Override
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
        //检查分块目录是否存在,不存在则创建
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //分块文件目录
        String chunkFilePath = chunkFileFolderPath + chunk;
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            //分块文件的目录不存在,创建目录
            chunkFileFolder.mkdirs();
        }
        //得到上传文件的输入流
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(new File(chunkFilePath));
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
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
        //返回结果
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 合并分块
     *
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    @Override
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //1. 得到分块文件的路径
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //2. 得到分块文件的文件列表
        File chunkFileFolder = new File(chunkFileFolderPath);
        File[] files = chunkFileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);

        //3. 创建一个文件存放合并后的文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);

        //4. 执行分块文件的合并
        mergeFile = this.mergeFile(fileList, mergeFile);
        if (mergeFile == null){
            //文件合并失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        //5. 校验合并后的文件与前端传来的文件md5是否一致
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if (!checkFileMd5){
            //md5校验失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //6. 将文件信息存储到mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件保存的相对路径
        String saveFilePath = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";

        mediaFile.setFilePath(saveFilePath);//问题

        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);

        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);

        //System.out.println("调用发送消息方法...");

        //向MQ发送视频处理消息
        sendProcessVideoMsg(mediaFile.getFileId());

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 校验文件
     * @param mergeFile
     * @param md5
     * @return
     */
    private boolean checkFileMd5(File mergeFile,String md5){
        try {
            //创建文件输入流
            FileInputStream inputStream = new FileInputStream(mergeFile);
            //得到文件的md5值
            String md5Hex = DigestUtils.md5Hex(inputStream);

            //将合并后的文件的md5 值 与前台传来的ma5进行比较
            if (md5.equalsIgnoreCase(md5Hex)){ //md5 不区分大小写
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 合并文件
     *
     * @param chunkFileList
     * @param mergeFile
     * @return
     */
    private File mergeFile(List<File> chunkFileList, File mergeFile) {
        try {
            //若合并的文件已经存在,删除文件,再次合并
            if (mergeFile.exists()) {
                mergeFile.delete();
            } else {
                //创建一个新文件进行分块文件的合并
                mergeFile.createNewFile();
            }
            //将文件列表中的分块文件按照分块名升序排列
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                        return 1;
                    }
                    return -1;
                }
            });
            //创建写对象,将分块文件写到合并后的文件中
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            //缓冲区
            byte[] b = new byte[1024];
            for (File chunkFile : chunkFileList) {
                //创建一个读对象,对分块文件进行读取
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
                int len = -1;
                while ((len = raf_read.read(b)) != -1){
                    //将读取到的文件信息写入到合并文件中
                    raf_write.write(b,0,len);
                }
                //关闭读数据的流
                raf_read.close();
            }
            //关闭写数据的流
            raf_write.close();
            return mergeFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    //向mq发送视频处理消息
    public ResponseResult sendProcessVideoMsg(String mediaId){
        //System.out.println("发送消息方法执行啦..."+mediaId);
        //获取文件信息
        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(mediaId);
        if (!optionalMediaFile.isPresent()){
            return new ResponseResult(CommonCode.FAIL);
        }
        MediaFile mediaFile = optionalMediaFile.get();
        //发送视频处理消息
        Map<String,String> msgMap = new HashMap<>();
        msgMap.put("mediaId",mediaId);
        //发送信息
        String msg = JSON.toJSONString(msgMap);
        try {
            this.rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routing_key_media_video, msg);
        } catch (AmqpException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
