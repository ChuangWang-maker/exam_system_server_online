package com.boomsoft.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.boomsoft.exam.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {


    /**
     * 查询所有分类同时查询分类数量
     * @return
     */
    List<Category> findCategoryList();
}