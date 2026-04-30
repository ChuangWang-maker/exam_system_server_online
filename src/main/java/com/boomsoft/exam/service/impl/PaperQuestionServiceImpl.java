package com.boomsoft.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boomsoft.exam.entity.PaperQuestion;
import com.boomsoft.exam.mapper.PaperQuestionMapper;
import com.boomsoft.exam.service.PaperQuestionService;
import com.boomsoft.exam.service.PaperService;
import org.springframework.stereotype.Service;

/**
 * projectName: exam_system_server_online
 *
 * @author: 赵伟风
 * description:
 */
@Service
public class PaperQuestionServiceImpl extends ServiceImpl<PaperQuestionMapper, PaperQuestion> implements PaperQuestionService {
}