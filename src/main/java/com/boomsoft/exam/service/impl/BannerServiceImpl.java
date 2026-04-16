package com.boomsoft.exam.service.impl;

import com.boomsoft.exam.entity.Banner;
import com.boomsoft.exam.mapper.BannerMapper;
import com.boomsoft.exam.service.BannerService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boomsoft.exam.service.FileUploadService;
import io.minio.errors.*;
import io.netty.util.internal.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务实现类
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Autowired
    private FileUploadService fileUploadService;

    @Override
    public String uploadBannerImage(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (file.isEmpty()){
            throw new RuntimeException("上传的轮播图文件为空！上传失败！");
        }
        String contentType = file.getContentType();
        if (ObjectUtils.isEmpty(contentType) || !contentType.startsWith("image")) {
            throw new RuntimeException("上传的轮播图文件类型错误！上传失败！");
        }
        String  imageUrl =fileUploadService.uploadFile("banners", file);
        return imageUrl;
    }
}