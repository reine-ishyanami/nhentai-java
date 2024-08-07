package com.reine.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

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

    private final StopWatch stopWatch = new StopWatch();

    @Around("@annotation(com.reine.annotation.Timer)")
    public Object aroundMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        stopWatch.start();
        String className = proceedingJoinPoint.getTarget().getClass().getSimpleName();
        String methodName = proceedingJoinPoint.getSignature().getName();
        Object result = proceedingJoinPoint.proceed();
        stopWatch.stop();
        log.debug("{}::{} 方法执行时间: {}ms", className, methodName, stopWatch.lastTaskInfo().getTimeMillis());
        return result;
    }

}
