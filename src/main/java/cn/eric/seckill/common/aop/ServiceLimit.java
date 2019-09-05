package cn.eric.seckill.common.aop;


import java.lang.annotation.*;

/**
 * 自定义注解 限流
 * @author Eric
 * @date 17:11 2019/7/30
 **/
@Target({ElementType.PARAMETER,ElementType.METHOD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceLimit {

    String description() default "";
}
