package cn.eric.seckill.common.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: RedisUtil
 * @Description: TODO
 * @company lsj
 * @date 2019/8/2 10:45
 **/
@Component
public class RedisUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    @Resource
    private RedisTemplate<Serializable,Serializable> redisTemplate;


    /** 前缀 */
    public static final String KEY_PREFIX_VALUE = "eric:seckill:value";

    /**
     * set值  单位秒
     * @author Eric
     * @date 11:00 2019/8/2
     * @param k
     * @param value
     * @param time 单位秒
     * @throws
     * @return boolean
     **/
    public boolean cacheValue(String k,Serializable value,long time){
        String key = KEY_PREFIX_VALUE + k;
        ValueOperations<Serializable, Serializable> serializableSerializableValueOperations = redisTemplate.opsForValue();

        try {
            serializableSerializableValueOperations.set(key,value);
            if(time > 0){
                redisTemplate.expire(key, time , TimeUnit.SECONDS);
            }
            return true;
        } catch (Throwable t) {
            logger.error("缓存[{}] 失败，value[{}]",key,value,t);
        }
        return false;
    }

    /**
     * set 自定义单位
     * @author Eric
     * @date 11:01 2019/8/2
     * @param k
     * @param v
     * @param time
     * @param unit
     * @throws
     * @return boolean
     **/
    public  boolean cacheValue(String k, Serializable v, long time,TimeUnit unit) {
        String key = KEY_PREFIX_VALUE + k;
        try {
            ValueOperations<Serializable, Serializable> valueOps =  redisTemplate.opsForValue();
            valueOps.set(key, v);
            if (time > 0) {
                redisTemplate.expire(key, time, unit);
            }
            return true;
        } catch (Throwable t) {
            logger.error("缓存[{}]失败, value[{}]",key,v,t);
        }
        return false;
    }


    /**
     * 缓存value操作
     * @param k
     * @param v
     * @return
     */
    public  boolean cacheValue(String k, Serializable v) {
        return cacheValue(k, v, -1);
    }

    /**
     * 判断缓存是否存在
     * @param k
     * @return
     */
    public  boolean containsValueKey(String k) {
        String key = KEY_PREFIX_VALUE + k;
        try {
            return redisTemplate.hasKey(key);
        } catch (Throwable t) {
            logger.error("判断缓存存在失败key[" + key + ", error[" + t + "]");
        }
        return false;
    }


    /**
     * 获取缓存
     * @param k
     * @return
     */
    public  Serializable getValue(String k) {
        try {
            ValueOperations<Serializable, Serializable> valueOps =  redisTemplate.opsForValue();
            return valueOps.get(KEY_PREFIX_VALUE + k);
        } catch (Throwable t) {
            logger.error("获取缓存失败key[" + KEY_PREFIX_VALUE + k + ", error[" + t + "]");
        }
        return null;
    }
    /**
     * 移除缓存
     * @param k
     * @return
     */
    public  boolean removeValue(String k) {
        String key = KEY_PREFIX_VALUE + k;
        try {
            redisTemplate.delete(key);
            return true;
        } catch (Throwable t) {
            logger.error("获取缓存失败key[" + key + ", error[" + t + "]");
        }
        return false;
    }

}
