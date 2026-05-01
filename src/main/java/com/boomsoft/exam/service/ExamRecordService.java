package com.boomsoft.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.boomsoft.exam.entity.ExamRecord;
import com.boomsoft.exam.vo.ExamRankingVO;


import java.util.List;

/**
 * 考试记录Service接口
 * 定义考试记录相关的业务方法
 */
public interface ExamRecordService extends IService<ExamRecord> {

    /**
     * 分页查询考试记录
     * @param examRecordPage 分页参数
     * @param studentName 学生姓名
     * @param status 考试状态
     * @param startDate 开始时间
     * @param endDate 结束时间
     */
    void pageExamRecords(Page<ExamRecord> examRecordPage, String studentName, Integer status, String startDate, String endDate);

    /**
     * 根据ID删除考试记录
     * @param id 考试记录ID
     */
    void removeExamRecordById(Integer id);

    /**
     * 获取考试排行榜
     * @param paperId 试卷ID
     * @param limit 排行榜数量
     * @return 排行榜列表
     */
    List<ExamRankingVO> rankList(Integer paperId, Integer limit);
}