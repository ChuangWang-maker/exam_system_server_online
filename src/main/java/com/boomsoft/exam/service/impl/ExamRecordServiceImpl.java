package com.boomsoft.exam.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boomsoft.exam.entity.AnswerRecord;
import com.boomsoft.exam.entity.ExamRecord;
import com.boomsoft.exam.entity.Paper;
import com.boomsoft.exam.mapper.AnswerRecordMapper;
import com.boomsoft.exam.mapper.ExamRecordMapper;
import com.boomsoft.exam.service.ExamRecordService;
import com.boomsoft.exam.service.PaperService;
import com.boomsoft.exam.vo.ExamRankingVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考试记录Service实现类
 * 实现考试记录相关的业务逻辑
 */

@Slf4j
@Service
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {

    @Autowired
    private PaperService paperService;

    @Autowired
    private AnswerRecordMapper answerRecordMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;


    @Override
    public void pageExamRecords(Page<ExamRecord> examRecordPage, String studentName, Integer status, String startDate, String endDate) {
        // 1. 正常进行考试记录的单表的分页查询 （多条件，动态条件）
        LambdaQueryWrapper<ExamRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!ObjectUtils.isEmpty(studentName),ExamRecord::getStudentName,studentName);
        if (!ObjectUtils.isEmpty(status)) {
            String strStatus = switch (status){
                case 0 -> "进行中";
                case 1 -> "已完成";
                case 2 -> "已批阅";
                default -> null;
            };
            queryWrapper.eq(ExamRecord::getStatus,strStatus);
        }
        //开始时间，大于等于传入的开始时间条件
        queryWrapper.ge(!ObjectUtils.isEmpty(startDate),ExamRecord::getStartTime,startDate);
        //结束时间，小于等于传入的结束时间条件
        queryWrapper.le(!ObjectUtils.isEmpty(endDate),ExamRecord::getEndTime,endDate);
        //对考试记录的单表的分页查询
        page(examRecordPage,queryWrapper);

        // 2. 查看考试记录下的所有试卷对象
        if (ObjectUtils.isEmpty(examRecordPage.getRecords())){
            log.debug("考试记录为空，没有必要继续查询对应的试卷信息！");
            return;
        }
        List<Integer> paperIds = examRecordPage.getRecords().stream().map(ExamRecord::getExamId).collect(Collectors.toList());
        List<Paper> papers = paperService.listByIds(paperIds);
        Map<Long, Paper> paperMap = papers.stream().collect(Collectors.toMap(Paper::getId, p -> p));
        // 3. Java代码中将试卷对象一一赋值给考试录对象即可
        // 循环考试记录 -》 list获取对应的paper -> N 2
        // 循环考试记录 -》 list获取对应的paper -> N
        examRecordPage.getRecords().forEach(e -> e.setPaper(paperMap.get(e.getExamId().longValue())));
    }

    @Override
    public void     removeExamRecordById(Integer id) {
        //前置校验： 1. 自身的状态 以及其他的重要数据引用我们，我们无法删除！
        //自身的状态校验，进行中 不能删除！
        ExamRecord examRecord = getById(id);
        if (examRecord == null){
            log.debug("id={}考试记录已经被别人先删除啦！",id);
            return;
        }
        if ("进行中".equals(examRecord.getStatus())){
            throw new RuntimeException("id=%s的考试记录状态为：进行中 无法删除！".formatted(id));
        }
        //删除自身
        removeById(id);
        //删除子数据
        answerRecordMapper.delete(new LambdaQueryWrapper<AnswerRecord>().eq(AnswerRecord::getExamRecordId,id));
    }


    @Override
    public List<ExamRankingVO> rankList(Integer paperId, Integer limit) {
        List<ExamRankingVO> rankingVOS = examRecordMapper.queryRank(paperId,limit);
        return rankingVOS;
    }
}