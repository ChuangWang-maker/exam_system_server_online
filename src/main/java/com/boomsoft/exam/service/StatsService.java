package com.boomsoft.exam.service;


import com.boomsoft.exam.vo.StatsVo;

/**
 * 统计数据服务接口
 */
public interface StatsService {
    
    /**
     * 获取系统统计数据
     * @return 统计数据DTO
     */
    com.boomsoft.exam.vo.StatsVo getSystemStats();
} 