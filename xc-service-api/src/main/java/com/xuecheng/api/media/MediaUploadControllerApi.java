package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "媒资管理接口",description = "媒资管理接口,提供文件上传、处理等接口")
public interface MediaUploadControllerApi {
    @ApiOperation("文件上传前校验")
    public ResponseResult register(String fileMd5,
                                   String name,
                                   long size,
                                   String type,
                                   String ext);
    @ApiOperation("上传分块前校验是否已经存在")
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize);
    @ApiOperation("上传分块")
    public ResponseResult uploadchunk(MultipartFile file,Integer chunk,String fileMd5);
    @ApiOperation("合并分块")
    public ResponseResult mergechunks(String fileMd5,String fileName,Long fileSize,String mimetype,String fileExt);
}
