package com.boomsoft.exam.service.impl;

import com.boomsoft.exam.config.properties.MinioProperties;
import com.boomsoft.exam.service.FileUploadService;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * projectName: com.boomsoft.exam.service.impl
 *
 * @author: 赵伟风
 * description:
 */
@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioProperties minioProperties;
    @Override
    public String uploadFile(String folder, MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        //1.连接minio的客户端
        //2.判断桶是否存在
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
        //3.不存在创建桶，并且设置访问权限
        if (!bucketExists) {
            // 1. 定义桶策略 JSON（使用 Java 15+ 的文本块语法 """ ）
            String policy = """
            {
                "Statement" : [ {
                  "Action" : "s3:GetObject",
                  "Effect" : "Allow",
                  "Principal" : "*",
                  "Resource" : "arn:aws:s3:::%s/*"
                } ],
                "Version" : "2012-10-17"
            }
            """.formatted(minioProperties.getBucketName());

            // 2. 创建桶
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());

            // 3. 将策略应用到新创建的桶上
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .config(policy)
                    .build());
        }
        //4.上传文件了
            // 1. 拼接对象名：通过 folder 实现 MinIO 内部的文件夹效果
            String objectName = folder + "/" +
                    new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" +
                    UUID.randomUUID().toString().replaceAll("-", "") +
                    file.getOriginalFilename();

            // 2. 调用 MinIO 客户端执行上传
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName()) // 桶名
                    .contentType(file.getContentType())      // 设置文件类型（如 image/png），方便浏览器识别
                    .object(objectName)                      // 在桶里的完整路径+文件名
                    /*
                      stream 上传文件的输入流数据
                      参数1：file.getInputStream() -> 获取文件流
                      参数2：file.getSize()         -> 文件总大小
                      参数3：-1                    -> 分片大小（partSize），-1 代表由 MinIO 自动决定
                    */
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        //5.拼接回显地址即可
        // url = 端点 + / + 桶 + / + 对象名
        String url = String.join("/", minioProperties.getEndpoint(), minioProperties.getBucketName(), objectName);

        log.info("文件上传成功，回显地址为：{}", url);

        return url;
    }
}
