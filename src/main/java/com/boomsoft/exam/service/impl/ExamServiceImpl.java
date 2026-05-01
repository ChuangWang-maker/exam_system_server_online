package com.boomsoft.exam.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.boomsoft.exam.entity.AnswerRecord;
import com.boomsoft.exam.entity.ExamRecord;
import com.boomsoft.exam.entity.Paper;
import com.boomsoft.exam.entity.Question;
import com.boomsoft.exam.mapper.AnswerRecordMapper;
import com.boomsoft.exam.mapper.ExamRecordMapper;
import com.boomsoft.exam.service.AnswerRecordService;
import com.boomsoft.exam.service.ExamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boomsoft.exam.service.KimiAiService;
import com.boomsoft.exam.service.PaperService;
import com.boomsoft.exam.vo.StartExamVo;
import com.boomsoft.exam.vo.SubmitAnswerVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 考试服务实现类
 */
@Service
@Slf4j
public class ExamServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamService {

    @Autowired
    private PaperService paperService;

    @Autowired
    private AnswerRecordMapper answerRecordMapper;

    @Autowired
    private AnswerRecordService answerRecordService;

    @Autowired
    private KimiAiService kimiAiService;

    /**
     * 保存考试记录
     * @param startExamVo 考试开始信息
     * @return 考试记录
     */
    @Override
    public ExamRecord saveExam(StartExamVo startExamVo) {
        //1. 检验考生在当前选择的试卷是否存在正在进行中的考试，存在，就返回源对象
        LambdaQueryWrapper<ExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamRecord::getStudentName,startExamVo.getStudentName());
        //试卷id
        queryWrapper.eq(ExamRecord::getExamId,startExamVo.getPaperId());
        queryWrapper.eq(ExamRecord::getStatus,  "进行中");
        ExamRecord examRecord = getOne(queryWrapper);
        if (examRecord != null){
            log.debug("考生: {},在paperId:{}试卷中有存在正在进行的考试记录，直接返回对应的考试记录：{}",
                    startExamVo.getStudentName(),startExamVo.getPaperId(),examRecord.getId());
            return examRecord;
        }

        // 2. 补全考试记录对象的属性（进行中 已完成 已批阅）
        examRecord = new ExamRecord();
        examRecord.setExamId(startExamVo.getPaperId()); //试卷id
        examRecord.setStudentName(startExamVo.getStudentName()); //学生姓名
        examRecord.setStatus("进行中");
        examRecord.setStartTime(LocalDateTime.now());
        examRecord.setWindowSwitches(0);

        // 3. 进行考试记录对象保存
        save(examRecord);

        // 4. 返回对应的考试记录
        return examRecord;
    }

    @Override
    public ExamRecord getExamRecodeDetail(Integer id) {
        //根据id查询考试记录对象
        ExamRecord examRecord = getById(id);
        if (examRecord == null){
            throw new RuntimeException("考试记录已经被删除，请重新考试考试！");
        }

        //再根据考试记录对象中的试卷对象id获取试卷对象（试卷 -》 题目 -》 选项 -》 答案 顺序）
        Paper paper = paperService.getPaperById(examRecord.getExamId());
        examRecord.setPaper(paper);

        //根据考试记录id查询对应的答题记录集合
        LambdaQueryWrapper<AnswerRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AnswerRecord::getExamRecordId,id);
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(queryWrapper);

        //注意：答题记录顺序和试卷的题目顺序相同，这样，在展示试卷详情也会按照 选择题 -》 判断 -》 简答题
        if (!ObjectUtils.isEmpty(answerRecords)){
            //1. 按照试卷中的题目的顺序给答题记录排序
            //先获取试卷中题目的id集合！ 【5,2,1,4,3】
            List<Long> questionIds = paper.getQuestions().stream().map(Question::getId).collect(Collectors.toList());
            //根据考试记录对应题目id在 题目集合中的顺序进行排序
            //【1 2 3 4 5】 -> [2,1,4,3,0]
            answerRecords.sort((a1,a2) ->{
                int a1Index = questionIds.indexOf(a1.getQuestionId());
                int a2Index = questionIds.indexOf(a2.getQuestionId());
                return Integer.compare(a1Index, a2Index);
            });

            //2. 装到考试记录对象中
            examRecord.setAnswerRecords(answerRecords);
        }
        return examRecord;
    }

    /**
     * 提交考试
     * @param examRecordId 考试记录id
     * @param answers 考试记录对象
     */
    @Override
    public void submitExam(Integer examRecordId, List<SubmitAnswerVo> answers) throws InterruptedException {
        //1.1 将集合转成AnswerRecode对象集合
        if (!ObjectUtils.isEmpty(answers)){
            List<AnswerRecord> answerRecordList = answers.stream().map(vo -> new AnswerRecord(examRecordId, vo.getQuestionId(), vo.getUserAnswer()))
                    .collect(Collectors.toList());
            // 1.2 进行AnswerRecord集合的批量保存即可
            // 注意：需要自己创建AnswerRecord的业务层，扩展mybatis-plus的功能
            answerRecordService.saveBatch(answerRecordList);
        }
        //1.3 修改考试记录对象 （1.状态改成已完成（进行中 已完成 已批阅）2. endTime时间）
        ExamRecord examRecord = getById(examRecordId);
        examRecord.setEndTime(LocalDateTime.now());
        examRecord.setStatus("已完成");
        updateById(examRecord);

        // 调用判断业务方法完成判卷！
        graderExam(examRecordId);
    }

    /**
     * ai智能判断功能
     * @param examRecordId
     * @return
     */
    @Override
    public ExamRecord graderExam(Integer examRecordId) throws InterruptedException {
        //1. 获取考生的考试信息（考试记录对象 ， 对应考试试卷（正确答案） ， 答题记录集合（学生的答案））
        ExamRecord examRecord = getExamRecodeDetail(examRecordId);
        //2. 校验考试记录对应的试卷是否被删除（正确答案） 【已经被删除，抛出异常即可！试卷状态改为 已批阅 点评：对应试卷被删除无法判卷】
        Paper paper = examRecord.getPaper();
        if (paper == null){
            //已经被删除了试卷
            examRecord.setStatus("已批阅");
            examRecord.setAnswers("考试对应试卷已经被删除，无法判卷！"); //ai评价
            examRecord.setScore(0);
            updateById(examRecord);
            log.warn("考试没有正常判定，原因id={}的考试记录对应的试卷已经被删除！！",examRecordId);
            return examRecord;
        }
        //3. 校验考生提交的考试记录是否为空，为空，直接零分已批阅！
        List<AnswerRecord> answerRecords = examRecord.getAnswerRecords();
        if (ObjectUtils.isEmpty(answerRecords)){
            //没有答题
            examRecord.setStatus("已批阅");
            examRecord.setAnswers("学生没有提交考试记录，直接判领！"); //ai评价
            examRecord.setScore(0);
            updateById(examRecord);
            log.warn("id={}考试因为学生名没有提交考试记录，直接判零分！",examRecordId);
            return examRecord;
        }
        //4. 声明两个变量 记录正确题目数量 以及 总分数
        int correctCount = 0; // 正确的数量
        int totalScore = 0;    // 总分数
        //5. 将试卷中question题目集合 转成 map（questionId,question） 为了方便根据答题记录中questionId快速获取题目对象
        Map<Long, Question> questionMap = paper.getQuestions().stream().collect(Collectors.toMap(Question::getId, q -> q));
        //6. 循环学生的答题记录，在内部进行逐一判题，同时进行正确数量和题目分数的累加
        //建议：容错处理！ 单个题错了，咱们就是这个题0分，不耽误其他的题判断
        for (AnswerRecord answerRecord : answerRecords) {
            //6.1 获取答题记录对应的正确题目
            Question question = questionMap.get(answerRecord.getQuestionId().longValue());
            //答题记录对应题目被删除了，判断下一个题
            if (question ==null) continue;
            //6.2 获取正确答案和学生的答案
            String systemAnswer = question.getAnswer().getAnswer(); //正确答案
            String userAnswer = answerRecord.getUserAnswer(); //学生答案
            //如果判断题，用户提交的答案 T F -> TRUE 和 FALSE
            if ("JUDGE".equals(question.getType())){
                userAnswer = judgeToTrueOrFalse(userAnswer);
            }
            try {
                if (!"TEXT".equals(question.getType())){
                    //1. 非简答题
                    //判断题: 用户答案 TRUE FALSE 正确答案: TRUE FALSE [字符串比较]
                    //选择题: 用户答案 A A,B 正确答案: A A,B
                    if (userAnswer.equalsIgnoreCase(systemAnswer)){
                        //正确了
                        answerRecord.setIsCorrect(1); // 0 错误 1 正确 2 部分正确
                        answerRecord.setScore(question.getPaperScore().intValue()); //设置真实分数
                    }else{
                        //错误了
                        answerRecord.setIsCorrect(0); // 0 错误 1 正确 2 部分正确
                        answerRecord.setScore(0); //设置真实分数
                    }
                }else{
                    //todo:添加简答题的ai功能
                    String promt = kimiAiService.buildGradingPrompt(question, userAnswer, question.getPaperScore().intValue());
                    String result = kimiAiService.callKimiAI(promt);
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    //ai给的分数
                    Integer aiScore = jsonObject.getInteger("score");
                    if (aiScore >= question.getPaperScore().intValue()){
                        //题完成正确
                        answerRecord.setScore(question.getPaperScore().intValue());
                        answerRecord.setIsCorrect(1); //完全正确
                        answerRecord.setAiCorrection(jsonObject.getString("feedback"));
                    }else if (aiScore <= 0){
                        //证明完全错误
                        answerRecord.setScore(0);
                        answerRecord.setIsCorrect(0);
                        answerRecord.setAiCorrection(jsonObject.getString("reason"));
                    }else{
                        //部分正确
                        answerRecord.setScore(aiScore);
                        answerRecord.setIsCorrect(2); //部分正确
                        answerRecord.setAiCorrection(jsonObject.getString("reason"));
                    }

                }
            }catch (Exception e){
                //判断题目错了，给0分
                answerRecord.setIsCorrect(0);
                answerRecord.setScore(0);
                answerRecord.setAiCorrection("判断过程中报错了，直接0分");
            }
            // 进行题目数量的累加和得分累加
            totalScore += answerRecord.getScore();
            if (answerRecord.getIsCorrect() == 1){
                correctCount++;
            }

    }
        //7. 修改每一条学生答题记录（分数，是否正确，简答题的ai评价）
        answerRecordService.updateBatchById(answerRecords); //答题记录的批量更新
        //8. 调用kimi的模型，生成对应的ai调用设置给考试记录对象
        //todo:调用ai模型，生成对应的ai评价
        String summaryPrompt = kimiAiService.buildSummaryPrompt(totalScore, paper.getTotalScore().intValue(), paper.getQuestionCount(), correctCount);
        String summary = kimiAiService.callKimiAI(summaryPrompt);
        //9. 更新考试记录对象即可
        examRecord.setScore(totalScore);
        examRecord.setAnswers(summary);
        examRecord.setStatus("已批阅");
        updateById(examRecord);
        //10. 返回考试记录对象即可
        return examRecord;
    }

    //转化判断题答案 -》 TRUE和FALSE
    private String judgeToTrueOrFalse(String userAnswer){
        userAnswer = userAnswer.toUpperCase();
        switch (userAnswer){
            case "T":
            case "正确":
            case "对":
            case "TRUE":
                return "TRUE";
            case "F":
            case "错误":
            case "不对":
            case "FALSE":
                return "FALSE";
            default:
                return userAnswer;
        }
    }
}