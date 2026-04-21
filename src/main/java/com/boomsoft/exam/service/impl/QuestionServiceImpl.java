package com.boomsoft.exam.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boomsoft.exam.common.CacheConstants;
import com.boomsoft.exam.entity.PaperQuestion;
import com.boomsoft.exam.entity.Question;
import com.boomsoft.exam.entity.QuestionAnswer;
import com.boomsoft.exam.entity.QuestionChoice;
import com.boomsoft.exam.mapper.PaperQuestionMapper;
import com.boomsoft.exam.mapper.QuestionAnswerMapper;
import com.boomsoft.exam.mapper.QuestionChoiceMapper;
import com.boomsoft.exam.mapper.QuestionMapper;
import com.boomsoft.exam.service.QuestionService;
import com.boomsoft.exam.utils.RedisUtils;
import com.boomsoft.exam.vo.QuestionQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private RedisUtils redisUtils;


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

    /**
     * 根据id查询题目信息
     *    题目+答案+选项
     *    方案一：嵌套结构 连表查询 + result [可以使用，没有分页]
     *    方案二：嵌套查询 分步查询实现 【可以使用，没有必要1+n】
     *    方案三：查询+java代码赋值即可
     * @param id
     * @return
     */
    @Override
    public Question queryQuestionById(Long id) {
        //1.查询题目详情对象
        Question question = getById(id);
        if (question == null){
            //log.debug("查询id为{}的题目已经不存在！",id);
            throw new RuntimeException("查询id为%s的题目已经不存在！".formatted(id));
        }
        //2.查询题目对应的答案
        QuestionAnswer questionAnswer = questionAnswerMapper.selectOne(new LambdaQueryWrapper<QuestionAnswer>().eq(QuestionAnswer::getQuestionId, id));
        //3.查询题目对应的选项（选择题才有选项）
        if ("CHOICE".equals(question.getType())){
            List<QuestionChoice> questionChoices = questionChoiceMapper.selectList(new LambdaQueryWrapper<QuestionChoice>().eq(QuestionChoice::getQuestionId, id));
            question.setChoices(questionChoices);
        }
        //4.预留：进行redis的数据缓存zset
        new Thread(() -> {
            incrementQuestionScore(question.getId());
        }).start();
        return question;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestion(Question question) {
        // 1. 先判断不能重复 同一个type类型下（选择题 简答 判断题） title不能重复
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getType, question.getType());
        queryWrapper.eq(Question::getTitle, question.getTitle());

        long count = count(queryWrapper);

        if (count > 0) {
            throw new RuntimeException("在%s类型下，已经存在名为%s的题目信息，保存失败！".formatted(question.getType(), question.getTitle()));
        }
        // 2. 保存题目信息（先保存题目，保存了题目你才有题目的id，才可以进行后续的答案和选项保存）
        save(question);
        // 3. 判断是不是选择题，是，根据选项的正确给答案赋值 同时将选项插入到选项表
        QuestionAnswer answer = question.getAnswer(); // 获取答案对象 如果是选择题 答案属性为""
        answer.setQuestionId(question.getId());
        if ("CHOICE".equals(question.getType())) {
            List<QuestionChoice> questionChoices = question.getChoices();
            StringBuilder sb = new StringBuilder(); // 拼接正确答案 A,D
            for (int i = 0; i < questionChoices.size(); i++) {
                QuestionChoice choice = questionChoices.get(i);
                choice.setSort(i); // 0 1 2 3
                choice.setQuestionId(question.getId());
                // 保存选项
                questionChoiceMapper.insert(choice);
                if (choice.getIsCorrect()) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append((char) ('A' + i));
                }
            }
            // 答案赋值
            answer.setAnswer(sb.toString());
        }
        // 4. 完成答案数据的插入
        questionAnswerMapper.insert(answer);
    }

    /**
     * 修改题目信息
     * @param question
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(Question question) {
       //1.进行判断 不同的题目id的title不能相同
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getTitle, question.getTitle());
        queryWrapper.ne(Question::getId, question.getId());
        long count = count(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("修改完新的title：%s已经被其他题目占用了！更新失败！".formatted(question.getTitle()));
        }
        //2.进行题目信息的更新
        updateById(question);
        //3.进行答案的更新
        QuestionAnswer questionAnswer = question.getAnswer();
        //4.判断是不是选择题，[1.删除原有选项 2.添加新的选项 3.拼接正确答案 4.给答案对象的答案赋值]
        if ("CHOICE".equals(question.getType())){
            List<QuestionChoice> questionChoices = question.getChoices();
            //1.删除原有选项
            questionChoiceMapper.delete(new LambdaQueryWrapper<QuestionChoice>().eq(QuestionChoice::getQuestionId, question.getId()));
            //2.添加新的选项
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < questionChoices.size(); i++) {
                QuestionChoice choice = questionChoices.get(i);
                choice.setId(null);
                choice.setCreateTime(null);
                choice.setUpdateTime(null);
                choice.setSort(i);
                choice.setQuestionId(question.getId());
                questionChoiceMapper.insert(choice);
                if (choice.getIsCorrect()) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append((char) ('A' + i));
                }
            }
            questionAnswer.setAnswer(sb.toString());
        }
        //5.完成答案的更新
        questionAnswerMapper.updateById(questionAnswer);

    }

    /**
     * 删除题目信息
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeQuestion(Long id) {
        //1.检查是否有关联的试卷题目。有删除失败
        LambdaQueryWrapper<PaperQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaperQuestion::getQuestionId, id);
        long count = paperQuestionMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("题目id为:%s已经关联试卷，关联的数量为:%s，删除失败！".formatted(id,count));
        }
        //2.删除本身
        removeById(id);
        //3.删除相关的子数据，选项和答案
        questionChoiceMapper.delete(new LambdaQueryWrapper<QuestionChoice>().eq(QuestionChoice::getQuestionId, id));
        questionAnswerMapper.delete(new LambdaQueryWrapper<QuestionAnswer>().eq(QuestionAnswer::getQuestionId, id));
        //4.添加事务注解
    }


    /**
     * 方法进行题目加分，在排行榜中 被一部调用
     * @param questionId
     */
    private void incrementQuestionScore(Long questionId){
        Double score = redisUtils.zIncrementScore(CacheConstants.POPULAR_QUESTIONS_KEY, questionId, 1);
        log.debug("题目id为{}的题目热榜分数累计，累计后的分数为{}",questionId,score);
    }
}