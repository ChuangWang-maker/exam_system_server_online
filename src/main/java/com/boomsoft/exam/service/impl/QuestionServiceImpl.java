package com.boomsoft.exam.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boomsoft.exam.entity.Question;
import com.boomsoft.exam.mapper.QuestionMapper;
import com.boomsoft.exam.service.QuestionService;
import com.boomsoft.exam.vo.QuestionQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 题目Service实现类
 * 实现题目相关的业务逻辑
 */
@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 查询题目列表（分页） 方案二：进行分步查询
     *
     * @param questionPage 分页参数
     * @param questionQueryVo 查询参数
     */
    @Override
    public void queryQuestionListByPage(Page<Question> questionPage, QuestionQueryVo questionQueryVo) {
        questionMapper.selectQuestionPage(questionPage, questionQueryVo);
    }
}