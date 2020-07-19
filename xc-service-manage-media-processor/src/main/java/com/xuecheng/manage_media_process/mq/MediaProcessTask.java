package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//监听视频处理队列，并进行视频处理
@Component
public class MediaProcessTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessTask.class);
    //ffmpeg绝对路径
    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpeg_path;
    //上传文件根目录
    @Value("${xc-service-manage-media.video-location}")
    String serverPath;
    @Autowired
    MediaFileRepository mediaFileRepository;

    //视频处理成MP4
    @RabbitListener(queues = {"${xc-service-manage-media.mq.queue-media-video-processor}"},containerFactory="customContainerFactory")
    public void receiveMediaProcessTask (String msg) throws IOException{
        System.out.println("开始处理视频");
        //将json数据转换成map集合
        Map msgMap = JSON.parseObject(msg, Map.class);
        LOGGER.info("receive media process task msg:{}",msgMap);
        //解析消息
        //媒资文件id
        String mediaId = (String) msgMap.get("mediaId");
        //获取媒资文件信息
        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(mediaId);
        if (!optionalMediaFile.isPresent()){
            return;
        }
        MediaFile mediaFile = optionalMediaFile.get();
        //获取媒资文件类型
        String fileType = mediaFile.getFileType();
        if (fileType == null || !fileType.equals("avi")){//只处理avi格式
            mediaFile.setProcessStatus("303004");//处理状态为无需处理
            String video_path = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();//视频路径
            //生成m3u8
            String m3u8url = savem3u8(mediaFile,video_path);
            mediaFile.setFileUrl(m3u8url);
            mediaFileRepository.save(mediaFile);
            return;
        }
        mediaFile.setProcessStatus("303001");//处理状态为未处理
        mediaFileRepository.save(mediaFile);
        //生成MP4
        String video_path = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();//视频路径
        String mp4_name = mediaFile.getFileId()+".mp4";//文件名称
        String mp4folder_path = serverPath + mediaFile.getFilePath();
        //使用工具类生成MP4文件
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4folder_path);
        String result = mp4VideoUtil.generateMp4();
        if (result == null || !result.equals("success")){//失败
            //操作失败写入日志
            mediaFile.setProcessStatus("303003");//处理状态为失败
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        //mp4视频文件路径
        String mp4_video_path = serverPath + mediaFile.getFilePath() + mp4_name;
        //生成m3u8
        String m3u8url = savem3u8(mediaFile,mp4_video_path);
        mediaFile.setFileUrl(m3u8url);
        mediaFileRepository.save(mediaFile);
    }

    //生成m3u8方法
    public String savem3u8 (MediaFile mediaFile,String video_path){
        //生成m3u8
        //String video_path = serverPath + mediaFile.getFilePath();//MP4视频路径
        String m3u8_name = mediaFile.getFileId()+".m3u8";//文件名称
        String m3u8folder_path = serverPath + mediaFile.getFilePath()+ "hls/";
        //使用工具类
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path,video_path,m3u8_name,m3u8folder_path);
        String res = hlsVideoUtil.generateM3u8();
        if (res == null || !res.equals("success")){//失败
            //操作失败写入日志
            mediaFile.setProcessStatus("303003");//处理状态为失败
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(res);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return null;
        }
        //获取m3u8列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        //更新处理状态为成功
        mediaFile.setProcessStatus("303002");//处理状态为成功
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        //m3u8 url
        String m3u8url = mediaFile.getFilePath()+"hls/"+m3u8_name;
        return m3u8url;
    }
}
