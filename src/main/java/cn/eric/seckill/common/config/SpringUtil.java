package cn.eric.seckill.common.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: SpringUtil
 * @Description: TODO
 * @company lsj
 * @date 2019/7/30 14:31
 **/
@Component
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(SpringUtil.applicationContext == null){
            SpringUtil.applicationContext = applicationContext;
        }
        System.out.println("Eric SecKill");
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /** 通过name获取 Bean. */
    public static Object getBean(String name){
        return getApplicationContext().getBean(name);
    }

    public static <T> T getBean(Class<T> clasz){
        return getApplicationContext().getBean(clasz);
    }

    public static <T> T getBean(String name,Class<T> clazz){
        return getApplicationContext().getBean(name, clazz);
    }
}
