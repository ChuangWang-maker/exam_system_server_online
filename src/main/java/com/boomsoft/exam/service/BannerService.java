package com.boomsoft.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boomsoft.exam.entity.Banner;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 轮播图服务接口
 */
public interface BannerService extends IService<Banner> {

    /**
     * 上传轮播图图片
     * @param file 图片文件
     * @return 图片访问URL
     */
    String uploadBannerImage(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
} 