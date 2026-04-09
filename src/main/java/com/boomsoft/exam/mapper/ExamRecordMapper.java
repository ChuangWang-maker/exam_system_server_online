package com.boomsoft.exam.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boomsoft.exam.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 针对表【exam_record(考试记录表)】的数据库操作Mapper
 * @createDate 2025-06-20 22:37:43
 * @Entity com.boomsoft.exam.entity.ExamRecord
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<com.boomsoft.exam.entity.ExamRecord> {

} 