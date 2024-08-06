package com.reine.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 计时切面处理类
 *
 * @author reine
 * 2024/7/20 13:23
 */
@Component
@Aspect
@Slf4j
public class TimerHandler {

    @Around("@annotation(com.reine.annotation.Timer)")
    public Object aroundMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long end = System.currentTimeMillis();
        log.info("方法执行时间: {}ms", end - start);
        return result;
    }

}
