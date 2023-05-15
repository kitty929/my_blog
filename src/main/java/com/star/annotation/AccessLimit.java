//Java 注解，名为 AccessLimit。注解作用于方法和类上，表示该方法或类需要进行访问限制控制。
package com.star.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessLimit {
    int seconds();
    int maxCount();
    boolean needLogin()default true;
}
