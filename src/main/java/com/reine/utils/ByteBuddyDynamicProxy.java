package com.reine.utils;

import com.reine.aop.TimerHandler;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationTargetException;

/**
 * @author reine
 * 2024/7/20 13:29
 */
public class ByteBuddyDynamicProxy {

    @SuppressWarnings("unchecked")
    public static <T> T proxy(T target) {
        try (DynamicType.Unloaded<?> make = new ByteBuddy()
                .subclass(target.getClass())
                .method(ElementMatchers.any())
                .intercept(InvocationHandlerAdapter.of(new TimerHandler(target)))
                .make()) {
            return (T) make
                    .load(ByteBuddyDynamicProxy.class.getClassLoader())
                    .getLoaded()
                    .getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
