package com.boomsoft.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boomsoft.exam.entity.Paper;
import com.boomsoft.exam.vo.AiPaperVo;
import com.boomsoft.exam.vo.PaperVo;

/**
 * 试卷服务接口
 */
public interface PaperService extends IService<Paper> {

    /**
     * 创建试卷
     * @param paperVo
     * @return
     */
    Paper createPaper(PaperVo paperVo);

    /**
     * 智能组卷
     * @param aiPaperVo
     * @return
     */
    Paper aiCreatePaper(AiPaperVo aiPaperVo);
}