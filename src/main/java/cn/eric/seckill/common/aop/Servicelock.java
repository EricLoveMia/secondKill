package cn.eric.seckill.common.aop;
import java.lang.annotation.*;

/**
 * 自定义注解 aop锁
 * @author Eric
 * @date 17:11 2019/7/30
 **/
@Target({ElementType.PARAMETER, ElementType.METHOD})    
@Retention(RetentionPolicy.RUNTIME)    
@Documented    
public  @interface Servicelock { 
	 String description()  default "";
}
