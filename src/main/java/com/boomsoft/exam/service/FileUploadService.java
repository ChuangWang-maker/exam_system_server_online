package com.boomsoft.exam.service;


import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 文件上传服务
 * 支持MinIO和本地文件存储两种方式
 */

public interface FileUploadService {
    /**
     * 上传文件
     *
     * @param floder 在minio中存储的文件夹 （lunbot： banners 视频：videos）
     * @param file   文件
     * @return 文件访问路径
     */
    String uploadFile(String floder, MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
} 