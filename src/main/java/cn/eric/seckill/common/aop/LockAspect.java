package cn.eric.seckill.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: LockAspect
 * @Description: TODO
 * @company lsj
 * @date 2019/7/31 13:37
 **/
@Aspect
@Component
@Order(1)
@Scope
public class LockAspect {

    /**
     * service 默认是单例的，并发下lock只有一个实例
     */
    private static Lock lock = new ReentrantLock(true);//互斥锁 参数默认false，不公平锁

    /** Service层切点,先加锁 */
    @Pointcut("@annotation(cn.eric.seckill.common.aop.Servicelock)")
    public void lockAspect() {

    }

    @Around("lockAspect()")
    public  Object around(ProceedingJoinPoint joinPoint) {
        lock.lock();
        Object obj;
        try {
            obj = joinPoint.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException();
        } finally{
            lock.unlock();
        }
        return obj;
    }
}
