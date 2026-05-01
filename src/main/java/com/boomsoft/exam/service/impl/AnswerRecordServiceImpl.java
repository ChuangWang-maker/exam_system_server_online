package com.boomsoft.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boomsoft.exam.entity.AnswerRecord;
import com.boomsoft.exam.mapper.AnswerRecordMapper;
import com.boomsoft.exam.service.AnswerRecordService;
import org.springframework.stereotype.Service;

@Service
public class AnswerRecordServiceImpl extends ServiceImpl<AnswerRecordMapper, AnswerRecord>
    implements AnswerRecordService {
}