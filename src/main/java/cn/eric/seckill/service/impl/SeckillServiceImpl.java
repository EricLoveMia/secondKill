package cn.eric.seckill.service.impl;

import cn.eric.seckill.common.aop.ServiceLimit;
import cn.eric.seckill.common.dynamicquery.DynamicQuery;
import cn.eric.seckill.common.entity.Result;
import cn.eric.seckill.common.entity.Seckill;
import cn.eric.seckill.common.entity.SuccessKilled;
import cn.eric.seckill.common.enums.SeckillStatEnum;
import cn.eric.seckill.repository.SeckillRepository;
import cn.eric.seckill.service.ISeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: SeckillServiceImpl
 * @Description: TODO
 * @company lsj
 * @date 2019/7/30 17:45
 **/
@Service("seckillService")
public class SeckillServiceImpl implements ISeckillService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SeckillServiceImpl.class);

    @Autowired
    private DynamicQuery dynamicQuery;

    @Autowired
    private SeckillRepository seckillRepository;

    /**  公平锁 */
    private Lock lock = new ReentrantLock(true);

    @Override
    public List<Seckill> getSeckillList() {
        return seckillRepository.findAll();
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillRepository.getOne(seckillId);
    }

    @Override
    public Long getSeckillCount(long seckillId) {
        String nativeSql = "SELECT count(*) FROM success_killed WHERE seckill_id=?";
        Object object = dynamicQuery.nativeQueryObject(nativeSql, seckillId);
        return ((Number)object).longValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSeckill(long seckillId) {
        String nativeSql = "DELETE FROM success_killed where seckill_id=?";
        dynamicQuery.nativeExecuteUpdate(nativeSql,new Object[]{seckillId});
        nativeSql = "UPDATE seckill set number=100 where seckill_id=?";
        dynamicQuery.nativeExecuteUpdate(nativeSql,new Object[]{seckillId});
    }

    /**
     * 直接操作数据库，扣减库存，没有使用乐观锁，也没有锁表，只是有一个令牌去限流
     * 可能出现记录10条，但是库存只减了9个的情况
     * @description: TODO
     * @author Eric
     * @date 10:56 2019/7/26
     * @param seckillId
     * @param userId
     * @throws
     * @return com.itstyle.seckill.common.entity.Result
     **/
    @Override
    @ServiceLimit
    @Transactional(rollbackFor = Exception.class)
    public Result startSeckil(long seckillId, long userId) {
        // 检查库存
        String nativeSql = "SELECT number FROM seckill WHERE seckill_id = ?";
        Object object = dynamicQuery.nativeQueryObject(nativeSql, new Object[]{seckillId});
        long number = ((Number) object).longValue();
        if(number > 0){
            // 扣库存
            nativeSql = "UPDATE seckill SET number=number-1 WHERE seckill_id=?";
            dynamicQuery.nativeExecuteUpdate(nativeSql,new Object[]{seckillId});

            // 创建订单
            SuccessKilled killed = new SuccessKilled();
            killed.setSeckillId(seckillId);
            killed.setUserId(userId);
            killed.setState((short)0);
            killed.setCreateTime(new Timestamp(System.currentTimeMillis()));
            dynamicQuery.save(killed);
            //支付
            return Result.ok(SeckillStatEnum.SUCCESS);
        }else{
            return Result.error(SeckillStatEnum.END);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startSeckilLock(long seckillId, long userId) {
        lock.lock();
        try {
            // 检查库存
            String nativeSql = "SELECT number FROM seckill WHERE seckill_id = ?";
            Object object = dynamicQuery.nativeQueryObject(nativeSql, new Object[]{seckillId});
            long number = ((Number) object).longValue();
            if(number > 0){
                // 扣库存
                nativeSql = "UPDATE seckill SET number=number-1 WHERE seckill_id=?";
                dynamicQuery.nativeExecuteUpdate(nativeSql,new Object[]{seckillId});

                // 创建订单
                SuccessKilled killed = new SuccessKilled();
                killed.setSeckillId(seckillId);
                killed.setUserId(userId);
                killed.setState((short)0);
                killed.setCreateTime(new Timestamp(System.currentTimeMillis()));
                dynamicQuery.save(killed);
            }else{
                return Result.error(SeckillStatEnum.END);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        //支付
        return Result.ok(SeckillStatEnum.SUCCESS);
    }

    @Override
    public Result startSeckilAopLock(long seckillId, long userId) {

        String nativeSql = "SELECT number FROM seckill WHERE seckill_id=?";
        Object object = dynamicQuery.nativeQueryObject(nativeSql,new Object[]{seckillId});
        Long number = ((Number) object).longValue();
        if(number > 0){
            // 扣库存
            nativeSql = "UPDATE seckill SET number=number-1 WHERE seckill_id=?";
            dynamicQuery.nativeExecuteUpdate(nativeSql,new Object[]{seckillId});

            // 创建订单
            SuccessKilled killed = new SuccessKilled();
            killed.setSeckillId(seckillId);
            killed.setUserId(userId);
            killed.setState((short)0);
            killed.setCreateTime(new Timestamp(System.currentTimeMillis()));
            dynamicQuery.save(killed);
        }
        else{
            return Result.error(SeckillStatEnum.END);
        }
        return Result.ok(SeckillStatEnum.SUCCESS);
    }

    /**
     * 悲观锁  for update 但是一定要在一个事务里面，不然就不起作用
     * @author Eric
     * @date 13:49 2019/7/31
     * @param seckillId
     * @param userId
     * @throws
     * @return cn.eric.seckill.common.entity.Result
     **/
    @Override
    @Transactional
    //@ServiceLimit
    public Result startSeckilDBPCC_ONE(long seckillId, long userId) {

        String nativeSql = "SELECT number FROM seckill WHERE seckill_id=? FOR UPDATE";
        Object object =  dynamicQuery.nativeQueryObject(nativeSql, new Object[]{seckillId});
        Long number =  ((Number) object).longValue();
        if(number>0){
            nativeSql = "UPDATE seckill SET number=number-1 WHERE seckill_id=?";
            dynamicQuery.nativeExecuteUpdate(nativeSql, new Object[]{seckillId});
            SuccessKilled killed = new SuccessKilled();
            killed.setSeckillId(seckillId);
            killed.setUserId(userId);
            killed.setState((short)0);
            killed.setCreateTime(new Timestamp(System.currentTimeMillis()));
            dynamicQuery.save(killed);
            return Result.ok(SeckillStatEnum.SUCCESS);
        }else{
            return Result.error(SeckillStatEnum.END);
        }
    }

    /**
     * 数据库悲观锁  直接update 同时判断数量
     * @author Eric
     * @date 13:49 2019/7/31
     * @param seckillId
     * @param userId
     * @throws
     * @return cn.eric.seckill.common.entity.Result
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startSeckilDBPCC_TWO(long seckillId, long userId) {
        String nativeSql = "UPDATE seckill set number = number - 1 WHERE seckill_id=? and number > 0";
        int count = dynamicQuery.nativeExecuteUpdate(nativeSql, new Object[]{seckillId});
        // 影响的行数
        if(count>0){
            SuccessKilled killed = new SuccessKilled();
            killed.setSeckillId(seckillId);
            killed.setUserId(userId);
            killed.setState((short)0);
            killed.setCreateTime(new Timestamp(System.currentTimeMillis()));
            dynamicQuery.save(killed);
            return Result.ok(SeckillStatEnum.SUCCESS);
        }else{
            return Result.error(SeckillStatEnum.END);
        }
    }

    /**
     * 数据库乐观锁 TODO 是否会出现少卖的情况
     * @author Eric
     * @date 13:49 2019/7/31
     * @param seckillId
     * @param userId
     * @throws
     * @return cn.eric.seckill.common.entity.Result
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startSeckilDBOCC(long seckillId, long userId, long number) {
        Seckill seckill = seckillRepository.findById(seckillId).get();
        // 这里可以一次秒杀两个 或者多个同一商品
        if(seckill.getNumber() >= number){
            //
            String nativeSql = "UPDATE seckill set number = number - ?,version = version + 1" +
                    " WHERE seckill_id=? and version = ?";

            int count = dynamicQuery.nativeExecuteUpdate(nativeSql, new Object[]{number, seckillId, seckill.getVersion()});
            // 影响的行数
            if(count>0){
                SuccessKilled killed = new SuccessKilled();
                killed.setSeckillId(seckillId);
                killed.setUserId(userId);
                killed.setState((short)0);
                killed.setCreateTime(new Timestamp(System.currentTimeMillis()));
                dynamicQuery.save(killed);
                return Result.ok(SeckillStatEnum.SUCCESS);
            }else{
                return Result.error(SeckillStatEnum.END);
            }
        }
        return Result.error(SeckillStatEnum.END);
    }

    @Override
    public Result startSeckilTemplate(long seckillId, long userId, long number) {
        return null;
    }
}
