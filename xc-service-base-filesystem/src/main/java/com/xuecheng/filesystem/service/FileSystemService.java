package com.xuecheng.filesystem.service;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface FileSystemService {
    //上传文件
    public UploadFileResult upload(MultipartFile file,
                                   String businesskey,
                                   String filetag,
                                   String metadata);
}
