package com.boomsoft.exam.service.impl;

import com.boomsoft.exam.entity.ExamRecord;
import com.boomsoft.exam.mapper.ExamRecordMapper;
import com.boomsoft.exam.service.ExamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * 考试服务实现类
 */
@Service
@Slf4j
public class ExamServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamService {

} 