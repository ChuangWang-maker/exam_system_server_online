package com.boomsoft.exam.service.impl;

import com.boomsoft.exam.entity.Banner;
import com.boomsoft.exam.mapper.BannerMapper;
import com.boomsoft.exam.service.BannerService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务实现类
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

} 