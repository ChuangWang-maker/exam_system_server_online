package com.boomsoft.exam.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.boomsoft.exam.entity.ExamRecord;
import com.boomsoft.exam.vo.ExamRankingVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @description 针对表【exam_record(考试记录表)】的数据库操作Mapper
 * @createDate 2025-06-20 22:37:43
 * @Entity com.boomsoft.exam.entity.ExamRecord
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<com.boomsoft.exam.entity.ExamRecord> {

    List<ExamRankingVO> queryRank(@Param("paperId") Integer paperId, @Param("limit") Integer limit);
}