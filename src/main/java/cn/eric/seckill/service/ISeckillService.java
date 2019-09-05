package cn.eric.seckill.service;

import cn.eric.seckill.common.entity.Result;
import cn.eric.seckill.common.entity.Seckill;

import java.util.List;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: ISeckillService
 * @Description: TODO
 * @company lsj
 * @date 2019/7/30 17:43
 **/
public interface ISeckillService {
    /**
     * 查询全部的秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);
    /**
     * 查询秒杀售卖商品
     * @param seckillId
     * @return
     */
    Long getSeckillCount(long seckillId);
    /**
     * 删除秒杀售卖商品记录
     * @param seckillId
     * @return
     */
    void deleteSeckill(long seckillId);

    /**
     * 秒杀 一、会出现数量错误
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckil(long seckillId,long userId);

    /**
     * 秒杀 二、程序锁
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilLock(long seckillId, long userId);
    /**
     * 秒杀 二、程序锁AOP
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilAopLock(long seckillId,long userId);

    /**
     * 秒杀 二、数据库悲观锁
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilDBPCC_ONE(long seckillId,long userId);
    /**
     * 秒杀 三、数据库悲观锁
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilDBPCC_TWO(long seckillId,long userId);
    /**
     * 秒杀 三、数据库乐观锁
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilDBOCC(long seckillId,long userId,long number);

    /**
     * 秒杀 四、事物模板
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilTemplate(long seckillId,long userId,long number);
}
