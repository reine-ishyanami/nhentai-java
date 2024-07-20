package com.reine.aop;

import com.reine.annotation.Timer;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 计时切面处理类
 *
 * @author reine
 * 2024/7/20 13:23
 */
@Slf4j
public class TimerHandler implements InvocationHandler {

    private final Object target;

    public TimerHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取方法上的注解
        Timer annotation = method.getAnnotation(Timer.class);
        String methodName = method.getName();
        Object result;
        if (annotation == null) {
            result = method.invoke(target, args);
        } else {
            long start = System.currentTimeMillis();
            result = method.invoke(target, args);
            log.info("{} 方法执行时间：{} 秒", methodName, (System.currentTimeMillis() - start) / 1000);
        }
        return result;
    }
}
