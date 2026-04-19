package com.boomsoft.exam.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boomsoft.exam.entity.Question;
import com.boomsoft.exam.entity.QuestionAnswer;
import com.boomsoft.exam.entity.QuestionChoice;
import com.boomsoft.exam.mapper.QuestionAnswerMapper;
import com.boomsoft.exam.mapper.QuestionChoiceMapper;
import com.boomsoft.exam.mapper.QuestionMapper;
import com.boomsoft.exam.service.QuestionService;
import com.boomsoft.exam.vo.QuestionQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目Service实现类
 * 实现题目相关的业务逻辑
 */
@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionChoiceMapper questionChoiceMapper;

    @Autowired
    private QuestionAnswerMapper questionAnswerMapper;

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

    /**
     * 查询题目列表（分页） 方案三：java代码处理
     * @param questionPage
     * @param questionQueryVo
     */
    @Override
    public void queryQuestionListByStream(Page<Question> questionPage, QuestionQueryVo questionQueryVo) {
        //1.题目单表的分页+动态条件查询（mybatis-plus）
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(questionQueryVo.getCategoryId() != null,Question::getCategoryId,questionQueryVo.getCategoryId());
        queryWrapper.eq(!ObjectUtils.isEmpty(questionQueryVo.getDifficulty()),Question::getDifficulty,questionQueryVo.getDifficulty());
        queryWrapper.eq(!ObjectUtils.isEmpty(questionQueryVo.getType()),Question::getType,questionQueryVo.getType());
        queryWrapper.like(!ObjectUtils.isEmpty(questionQueryVo.getKeyword()),Question::getTitle,questionQueryVo.getKeyword());
        queryWrapper.orderByDesc(Question::getCreateTime);
        page(questionPage, queryWrapper);
        //简单的判断，如果没有满足的题目信息！后续没有必要进行了！
        if( ObjectUtils.isEmpty(questionPage.getRecords()) ){
            log.info("没有符合条件的题目信息，后续可以终止！直接返回结果！");
            return;
        }
        //2.查询题目对应的所有的选项和所有的答案（mybatis-plus）
        //我们不循环 题目集合 questionPage.getRecords() 我们一次查询题目所有的答案和选项！进行Java代码处理!
        //todo: 我们避免1+n的问题
        //获取所有的题目id
        List<Long> questionIds = questionPage.getRecords().stream().map(Question::getId).collect(Collectors.toList());
        //查询所有选项
        LambdaQueryWrapper<QuestionChoice> questionChoiceQueryWrapper = new LambdaQueryWrapper<>();
        questionChoiceQueryWrapper.in(QuestionChoice::getQuestionId,questionIds);
        List<QuestionChoice> questionChoices = questionChoiceMapper.selectList(questionChoiceQueryWrapper);
        //查询所有答案
        LambdaQueryWrapper<QuestionAnswer> questionAnswerQueryWrapper = new LambdaQueryWrapper<>();
        questionAnswerQueryWrapper.in(QuestionAnswer::getQuestionId,questionIds);
        List<QuestionAnswer> questionAnswers = questionAnswerMapper.selectList(questionAnswerQueryWrapper);

        //3.题目的选项和答案集合转成map格式map（key => 题目id,题目对应的选项合计 | 题目对应的答案对象）
        //题目答案转成map
        Map<Long, QuestionAnswer> questionAnswerMap = questionAnswers.stream().collect(Collectors.toMap(QuestionAnswer::getQuestionId, a -> a));
        //题目选项转成map
        Map<Long, List<QuestionChoice>> questionChoiceMap = questionChoices.stream().collect(Collectors.groupingBy(QuestionChoice::getQuestionId));


        //4.循环题目列表，进行题目的选项和方案赋值工作
        questionPage.getRecords().forEach(question -> {
            //给题目答案赋值(题目一定有答案)
            question.setAnswer(questionAnswerMap.get(question.getId()));
            //给题目选项赋值（只有选择题有选项！选择题的tpye = CHOICE）
            if("CHOICE".equals(question.getType())){
                //只要是选项的操作，一定要考虑排序的问题sort
                List<QuestionChoice> qc = questionChoiceMap.get(question.getId());
                //字段进行排序,从小到大正序
                if (!ObjectUtils.isEmpty(qc)){
                    qc.sort(Comparator.comparing(QuestionChoice::getSort));
                    question.setChoices(qc);
                }
            }
        });
    }
}