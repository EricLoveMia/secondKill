package cn.eric.seckill.common.aop;

import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: LimitAspect
 * @Description: TODO
 * @company lsj
 * @date 2019/7/30 17:13
 **/
@Component
@Scope
@Aspect
public class LimitAspect {

    /** 每秒放出5个令牌 RateLimiter 令牌桶算法*/
    private static RateLimiter rateLimiter = RateLimiter.create(5.0);

    @Pointcut("@annotation(cn.eric.seckill.common.aop.ServiceLimit)")
    public void ServiceAspect(){

    }

    @Around("ServiceAspect()")
    public Object around(ProceedingJoinPoint joinPoint){
        boolean flag = rateLimiter.tryAcquire();
        Object obj = null;
        try {
            if(flag) {
                obj = joinPoint.proceed();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return obj;
    }

}
