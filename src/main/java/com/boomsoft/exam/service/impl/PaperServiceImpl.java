package com.boomsoft.exam.service.impl;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boomsoft.exam.entity.Paper;
import com.boomsoft.exam.entity.PaperQuestion;
import com.boomsoft.exam.entity.Question;
import com.boomsoft.exam.mapper.PaperMapper;
import com.boomsoft.exam.mapper.QuestionMapper;
import com.boomsoft.exam.service.PaperQuestionService;
import com.boomsoft.exam.service.PaperService;
import com.boomsoft.exam.vo.AiPaperVo;
import com.boomsoft.exam.vo.PaperVo;
import com.boomsoft.exam.vo.RuleVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



/**
 * 试卷服务实现类
 */
@Slf4j
@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 创建试卷
     * @param paperVo 试卷信息
     * @return 创建的试卷
     */
    @Override
    @Transactional
    public Paper createPaper(PaperVo paperVo) {
        //1. 先完善试卷基本信息（默认状态，总分数，总题目数量）
        Paper paper = new Paper();
        BeanUtils.copyProperties(paperVo, paper); //name description duration
        paper.setStatus("DRAFT"); //设置默认状态
        //检查是否传入题目信息
        if (ObjectUtils.isEmpty(paperVo.getQuestions())){
            paper.setTotalScore(BigDecimal.ZERO);
            paper.setQuestionCount(0);
            //保存题目对象信息,自动主键回显
            save(paper);
            log.warn("当前试卷：{}没有组装题目，只能用于试卷编辑，不能用于考试！！",paper);
            return paper;
        }



        // 有题目，根据题目计算数量和总分数
        //{题目id: 题目真实分数, 题目id: 题目真实分数, 题目id: 题目真实分数}
        //总题目数量
        paper.setQuestionCount(paperVo.getQuestions().size());
        //总分数 map -> value -> value进行累加 (BigDecimal.add(xxx) )
        Optional<BigDecimal> totalScore = paperVo.getQuestions().values().stream().reduce(BigDecimal::add);
        paper.setTotalScore(totalScore.get());
        //paperVo.getQuestions().values().stream().mapToInt(b -> b.intValue()).sum();
        //2. 保存试卷信息对象（试卷的主键）
        save(paper);
        log.debug("当前试卷勾选了题目信息，正常进行计算和保存！试卷对象信息为：{}", paper);

        //3. 判断试卷是否携带了题目集合，携带了，我们进行后续试卷题目中间表处理
        //4. 题目集合的map -> 试卷题目中间表对象集合
        List<PaperQuestion> paperQuestionList = paperVo.getQuestions().entrySet().stream()
                .map(entry -> new PaperQuestion(paper.getId().intValue(), Long.valueOf(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
        //5. 试卷题目中间表的业务批量插入方法完成批量插入即可
        paperQuestionService.saveBatch(paperQuestionList);
        //6. 返回试卷对象信息
        return paper;
    }

    /**
     * 创建智能试卷
     * @param aiPaperVo 智能试卷信息
     * @return 创建的智能试卷
     */
    @Override
    @Transactional
    public Paper aiCreatePaper(AiPaperVo aiPaperVo) {
        // 1. 先完成试卷的基础信息保存，获取试卷回显的id
        Paper paper = new Paper();
        BeanUtils.copyProperties(aiPaperVo,paper); //name description duration
        paper.setStatus("DRAFT"); //设置默认状态
        save(paper); //paper就保存了有主键值

        // 2. 循环每个规则，在规则下随机获取符合条件数量题目
        //总题目数量
        int questionCount = 0;
        //总分数
        BigDecimal totalScore = BigDecimal.ZERO;
        for (RuleVo rule : aiPaperVo.getRules()) {
            //校验，规则需要的题目数量是不是0，是0直接跳出本次循环
            if (rule.getCount() == 0){
                log.debug("下：{}类别规则下，要求题目数量为0，直接跳出，不选题！",rule.getType().name());
                continue;
            }
            //3. 将随机获取的题目集合转成PaperQuestion对象，进行当前规则下的批量保存
            //查询符合条件的所有题目信息 条件： type = type and category_id in categoryIds
            LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Question::getType, rule.getType().name());
            //categoryIds可能为空，没有类别id就选择所有！
            queryWrapper.in(!ObjectUtils.isEmpty(rule.getCategoryIds()),Question::getCategoryId,
                    rule.getCategoryIds());
            //查询符合条件的所有题目集合
            List<Question> questionAllList = questionMapper.selectList(queryWrapper);
            //校验 可能满足规则的题目集合为null，直接跳出本次循环
            if (ObjectUtils.isEmpty(questionAllList)){
                log.debug("下：{}类别规则下，没有满足条件的题目，直接跳出，不选题！",rule.getType().name());
                continue;
            }
            //校验 满足规则题目的数量和规则需要的题目数量要对比，谁小要谁！
            int realNumber = Math.min(rule.getCount(), questionAllList.size());

            //4. 在循环规则中，要计算当前规则真实题目数量和分数，并进行累加统计
            questionCount += realNumber; //进行题目数量累加
            totalScore = totalScore.add(BigDecimal.valueOf((long) realNumber * rule.getScore()));

            //随机选出符合条件的题目集合
            //打乱原有题目集合（洗牌）
            Collections.shuffle(questionAllList);
            //本次选出来的题目的集合
            List<Question> questionList = questionAllList.subList(0, realNumber);
            //题目集合 转成 PaperQuestion对象集合
            List<PaperQuestion> paperQuestionList = questionList.stream().map(q -> new PaperQuestion(paper.getId().intValue(), q.getId(),
                    BigDecimal.valueOf(rule.getScore()))).collect(Collectors.toList());
            //进行批量的数据保存
            paperQuestionService.saveBatch(paperQuestionList);

        }

        // 5. 规则都随机完毕以后，我们进行试卷分数和题目数量的修改
        paper.setTotalScore(totalScore);
        paper.setQuestionCount(questionCount);

        // 6. 修改试卷对象即可
        updateById(paper);

        // 7. 返回试卷结构即可
        return paper;
    }
}