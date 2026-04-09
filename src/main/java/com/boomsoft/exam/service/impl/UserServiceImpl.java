package com.boomsoft.exam.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.boomsoft.exam.entity.User;
import com.boomsoft.exam.mapper.UserMapper;
import com.boomsoft.exam.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户Service实现类
 * 实现用户相关的业务逻辑
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

} 