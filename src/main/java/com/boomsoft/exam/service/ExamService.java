package com.boomsoft.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boomsoft.exam.entity.ExamRecord;
import com.boomsoft.exam.vo.StartExamVo;
import com.boomsoft.exam.vo.SubmitAnswerVo;

import java.util.List;

/**
 * 考试服务接口
 */
public interface ExamService extends IService<ExamRecord> {

    /**
     * 保存考试记录
     * @param startExamVo 考试信息
     * @return 考试记录
     */
    ExamRecord saveExam(StartExamVo startExamVo);

    /**
     * 获取考试详情的业务
     * @param id 考试记录id
     * @return 考试记录
     */
    ExamRecord getExamRecodeDetail(Integer id);

    /**
     * 提交考试并进行判断调用
     * @param examRecordId 考试记录id
     * @param answers 考试答案
     */
    void submitExam(Integer examRecordId, List<SubmitAnswerVo> answers) throws InterruptedException;

    /**
     * ai智能批改考试
     * @param examRecordId 考试记录id
     * @return 考试记录
     */
    ExamRecord graderExam(Integer examRecordId) throws InterruptedException;
}
 