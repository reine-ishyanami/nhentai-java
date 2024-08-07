package com.reine.annotation;

import java.lang.annotation.*;

/**
 * @author reine
 * 2024/7/20 13:21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Timer {
}
