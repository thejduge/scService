package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件系统api
 */
@Api(value = "分布式文件管理接口",description = "提供文件的增、删、改、查")
public interface FileSystemControllerApi {
    //上传文件
    @ApiOperation("文件上传")
    /*@ApiImplicitParams({
            @ApiImplicitParam(name = "multipartFile",value = "上传文件",required = true),
            @ApiImplicitParam(name = "businesskey",value = "businesskey",required = false),
            @ApiImplicitParam(name = "filetag",value = "类型",required = false),
            @ApiImplicitParam(name = "metadata",value = "metadata",required = false),
    })*/
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String businesskey,
                                   String filetag,
                                   String metadata);
}
