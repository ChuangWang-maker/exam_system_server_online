package com.boomsoft.exam.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boomsoft.exam.entity.Question;
import com.boomsoft.exam.mapper.QuestionMapper;
import com.boomsoft.exam.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 题目Service实现类
 * 实现题目相关的业务逻辑
 */
@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    

} 