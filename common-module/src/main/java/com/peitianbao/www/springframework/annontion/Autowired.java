package com.peitianbao.www.springframework.annontion;

import java.lang.annotation.*;

/**
 * @author leg
 */
//这个注解可以作用在运行期
@Retention(RetentionPolicy.RUNTIME)
//指定该注解可以作用在类上
@Target(ElementType.FIELD)
@Inherited
public @interface Autowired {


}