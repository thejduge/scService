package com.xuecheng.manage_media.controller;

import com.xuecheng.api.media.MediaUploadControllerApi;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media/upload")
public class MediaUploadController implements MediaUploadControllerApi {
    @Autowired
    private MediaUploadService mediaUploadService;

    /**
     * 上传文件前对文件信息的校验
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    @Override
    @PostMapping("/register")
    public ResponseResult register(@RequestParam("fileMd5") String fileMd5,
                                   @RequestParam("fileName")String fileName,
                                   @RequestParam("fileSize")long fileSize,
                                   @RequestParam("mimetype")String mimetype,
                                   @RequestParam("fileExt")String fileExt) {
        System.out.println("前端数据:fileMd5:"+fileMd5+"fileName:"+fileName+"fileSize:"+fileSize+"mimetype:"+mimetype+"fileExt:"+fileExt);
        return mediaUploadService.register(fileMd5, fileName, fileSize, mimetype, fileExt);
    }

    /**
     * 上传分块前校验分块
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    @Override
    @PostMapping("/checkchunk")
    public CheckChunkResult checkchunk(@RequestParam("fileMd5")String fileMd5,
                                       @RequestParam("chunk")Integer chunk,
                                       @RequestParam("chunkSize")Integer chunkSize) {
        return mediaUploadService.checkchunk(fileMd5, chunk, chunkSize);
    }

    /**
     * 上传分块数据
     * @param file
     * @param chunk
     * @param fileMd5
     * @return
     */
    @Override
    @PostMapping("/uploadchunk")
    public ResponseResult uploadchunk(@RequestParam("file")MultipartFile file,
                                      @RequestParam("chunk")Integer chunk,
                                      @RequestParam("fileMd5")String fileMd5) {
        return mediaUploadService.uploadchunk(file, chunk, fileMd5);
    }

    /**
     * 将分块进行合并
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    @Override
    @PostMapping("/mergechunks")
    public ResponseResult mergechunks(@RequestParam("fileMd5")String fileMd5,
                                      @RequestParam("fileName")String fileName,
                                      @RequestParam("fileSize")Long fileSize,
                                      @RequestParam("mimetype")String mimetype,
                                      @RequestParam("fileExt")String fileExt) {
        return mediaUploadService.mergechunks(fileMd5, fileName, fileSize, mimetype, fileExt);
    }
}
