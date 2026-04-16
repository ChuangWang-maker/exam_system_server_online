package com.boomsoft.exam.exceptionhandlers;

import com.boomsoft.exam.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * projectName: exam_system_server_online
 *
 * @author: Jon
 * @date: 2026-04-15 18:06
 * description:
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //定义异常处理的handler方法
    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e){
        e.printStackTrace();//打印异常信息
        log.error("代码出现异常，异常信息为：{}",e.getMessage());
        return Result.error(e.getMessage());
    }
}
