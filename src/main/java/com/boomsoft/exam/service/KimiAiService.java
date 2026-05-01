package com.boomsoft.exam.service;


import com.boomsoft.exam.entity.Question;
import com.boomsoft.exam.vo.AiGenerateRequestVo;

/**
 * Kimi AI服务接口
 * 用于调用Kimi API生成题目
 */
public interface KimiAiService {

    String buildPrompt(AiGenerateRequestVo request);

    /**
     * 封装请求kimi模型的方法
     * @param prompt 提示词
     * @return 模型反馈的结果
     */
    String callKimiAI(String prompt) throws InterruptedException;

    /**
     * 生成判断简答题的提示词的方法
     * @param question
     * @param userAnswer
     * @param maxScore
     * @return
     */
    String buildGradingPrompt(Question question, String userAnswer, Integer maxScore);

    /**
     * 生成考试点评提示词的方法
     * @param totalScore
     * @param maxScore
     * @param questionCount
     * @param correctCount
     * @return
     */
    String buildSummaryPrompt(Integer totalScore, Integer maxScore, Integer questionCount, Integer correctCount);

} 