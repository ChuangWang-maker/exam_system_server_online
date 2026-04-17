package com.boomsoft.exam.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boomsoft.exam.entity.Category;
import com.boomsoft.exam.mapper.CategoryMapper;
import com.boomsoft.exam.mapper.QuestionMapper;
import com.boomsoft.exam.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 查询所有分类列表同时获取每个分类下的题目数量
     *
     * @return
     */
    @Override
    public List<Category> findCategoryList() {
        //1.查询所有分类信息集合（单表操作）
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        List<Category> categoryList =  list(queryWrapper);
        //2.QuestionMapper变量查询方法，category_id进行分组，并且统计每个分类下的题目数量count
        List<Map<String,Long>> mapList = questionMapper.selectCategoryQuestionCount();
        //3.题目查询的分类的题目数量赋值给分类集合
        Map<Long, Long> countMap = mapList.stream().collect(Collectors.toMap(k -> k.get("category_id"), v -> v.get("count")));
        for (Category category : categoryList){
            Long id = category.getId();
            category.setCount(countMap.getOrDefault(id, 0L));
        }
        return categoryList;
    }

    /**
     * 获取分类树形结构
     * @return
     */
    @Override
    public List<Category> getCategoryTreeList() {
        //1.查询所有分类信息集合（单表操作）
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        List<Category> categoryList =  list(queryWrapper);
        //2.QuestionMapper变量查询方法，category_id进行分组，并且统计每个分类下的题目数量count
        List<Map<String,Long>> mapList = questionMapper.selectCategoryQuestionCount();
        //3.题目查询的分类的题目数量赋值给分类集合
        Map<Long, Long> countMap = mapList.stream().collect(Collectors.toMap(k -> k.get("category_id"), v -> v.get("count")));
        for (Category category : categoryList){
            Long id = category.getId();
            category.setCount(countMap.getOrDefault(id, 0L));
        }
        //4.分类信息进行分组（parent_uid） stream分组
        //key - parent
        //value - list<子分类>
        Map<Long, List<Category>> longListMap = categoryList.stream().collect(Collectors.groupingBy(Category::getParentId));
        //5.筛选分类信息（获取一级分类）
        List<Category> parentCategoryList = categoryList.stream().filter(c -> c.getParentId() == 0).collect(Collectors.toList());
        //6.给一级分类循环，获取子分类，并计算count（父分类的count + 所有子分类的count）
        for (Category parentCategory : parentCategoryList) {
            List<Category> sonCategoryList = longListMap.getOrDefault(parentCategory.getId(), new ArrayList<>());
            parentCategory.setChildren(sonCategoryList);
            //count
            Long sonCount = sonCategoryList.stream().collect(Collectors.summingLong(Category::getCount));
            parentCategory.setCount(parentCategory.getCount() + sonCount);
        }
        return parentCategoryList;
    }
}