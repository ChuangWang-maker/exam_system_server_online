package com.boomsoft.exam.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.boomsoft.exam.entity.Question;
import com.boomsoft.exam.vo.QuestionQueryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 题目Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础的CRUD操作
 */
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 查询每个分类下的题目数量
     * @return 分类下的题目数量列表
     */
    @Select("select category_id,count(*) count from questions where is_deleted = 0 group by category_id;")
    List<Map<String,Long>> selectCategoryQuestionCount();

    //定义一个方法，还想使用mybatis-plus分页插件
    //方法规则：返回值必须是IPaeg方法名（第一个参数必须是IPage【分页数据第几页，每页显示几条】，其他数据）
    IPage<Question> selectQuestionPage(IPage<Question> page, @Param("queryVo") QuestionQueryVo questionVo);

} 