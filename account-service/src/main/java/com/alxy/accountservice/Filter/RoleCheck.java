// 自定义注解 @RoleCheck
package com.alxy.accountservice.Filter;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RoleCheck {
    String value();
}