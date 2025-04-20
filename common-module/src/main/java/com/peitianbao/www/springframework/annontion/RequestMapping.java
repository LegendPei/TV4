package com.peitianbao.www.springframework.annontion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author leg
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    //URL 映射路径
    String value();
    // HTTP 请求方法类型，默认为 GET
    RequestMethod methodType() default RequestMethod.GET;
}