package com.boomsoft.exam.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boomsoft.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础的CRUD操作
 */
public interface QuestionMapper extends BaseMapper<com.boomsoft.exam.entity.Question> {

} 