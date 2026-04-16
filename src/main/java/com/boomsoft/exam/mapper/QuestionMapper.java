package com.boomsoft.exam.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boomsoft.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 题目Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础的CRUD操作
 */
public interface QuestionMapper extends BaseMapper<com.boomsoft.exam.entity.Question> {

    /**
     * 查询每个分类下的题目数量
     * @return 分类下的题目数量列表
     */
    @Select("select category_id,count(*) count from questions where is_deleted = 0 group by category_id;")
    List<Map<String,Long>> selectCategoryQuestionCount();

} 