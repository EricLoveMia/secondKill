package cn.eric.seckill.web;

import cn.eric.seckill.common.entity.Result;
import cn.eric.seckill.common.entity.SuccessKilled;
import cn.eric.seckill.queue.disruptor.DisruptorUtil;
import cn.eric.seckill.queue.disruptor.SeckillEvent;
import cn.eric.seckill.queue.jvm.SeckillQueue;
import cn.eric.seckill.service.ISeckillService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: SeckillController
 * @Description: TODO
 * @company lsj
 * @date 2019/7/30 17:41
 **/
@Api(tags ="秒杀")
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    private final static Logger LOGGER = LoggerFactory.getLogger(SeckillController.class);
    static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("schedule-pool-%d").build();
    /**  本机核心线程数量 */
    private static int corePoolSize = Runtime.getRuntime().availableProcessors();
    /** 创建线程池  调整队列数 拒绝服务 */
//    private static ThreadPoolExecutor executor  = new ThreadPoolExecutor(corePoolSize, corePoolSize+1, 10L, TimeUnit.SECONDS,
//            new LinkedBlockingQueue<Runnable>(1000),threadFactory,new ThreadPoolExecutor.DiscardPolicy());

      private static ThreadPoolExecutor executor  = new ThreadPoolExecutor(corePoolSize, corePoolSize+1, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1000),threadFactory,new ThreadPoolExecutor.AbortPolicy());

//    private static ThreadPoolExecutor executor  = new ThreadPoolExecutor(corePoolSize, corePoolSize+1, 10L, TimeUnit.SECONDS,
//            new LinkedBlockingQueue<Runnable>(1000),threadFactory,new ThreadPoolExecutor.CallerRunsPolicy());


    @Autowired
    private ISeckillService seckillService;

    @ApiOperation(value="秒杀一(最low实现)",nickname="自来客")
    @PostMapping("/single/startOne")
    public Result start(long seckillId,int number){
        int skillNum = number;
        // N个购买者
        final CountDownLatch countDownLatch = new CountDownLatch(skillNum);

        seckillService.deleteSeckill(seckillId);

        LOGGER.info("开始单机秒杀一 ");
        for (int i = 0; i < skillNum; i++) {
            final int userId = i;
            Runnable task = () -> {
                Result result = seckillService.startSeckil(seckillId, userId);
                if(result!=null){
                    LOGGER.info("用户:{}{}",userId,result.get("msg"));
                }else{
                    LOGGER.info("用户:{}{}",userId,"哎呦喂，人也太多了，请稍后！");
                }
                countDownLatch.countDown();
            };
            executor.execute(task);
        }

        try {
            countDownLatch.await();
            Long  seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @ApiOperation(value="秒杀二(程序锁)",nickname="自来客")
    @PostMapping("/single/startLock")
    public Result startLock(long seckillId,int number){
        long begin = System.currentTimeMillis();
        int skillNum = number;
        //N个购买者 不能大于5000 因为有latch的存在 大于5000 会自动抛弃
        final CountDownLatch latch = new CountDownLatch(skillNum);
        seckillService.deleteSeckill(seckillId);
        final long killId =  seckillId;
        LOGGER.info("开始秒杀二(正常)");
        for(int i=0;i<skillNum;i++){
            final long userId = i;
            Runnable task = () -> {
                Result result = seckillService.startSeckilLock(killId, userId);
                LOGGER.info("用户:{}{}",userId,result.get("msg"));
                latch.countDown();
            };
            // 大于5000 会自动抛弃
            // 看看加了trycatch后行不行 拒绝策略要么是重新放回 要么是抛出异常 如果是直接抛弃 好像不会走到 latch.countDown();
            try {
                executor.execute(task);
            } catch (Exception e) {
                // e.printStackTrace();
                latch.countDown();
            } finally {
            }
        }
        try {
            latch.await();// 等待所有人任务结束
            Long seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        LOGGER.info("<-----------------------------------用时------------------------------------>" + (end-begin));
        return Result.ok();
    }


    @ApiOperation(value="秒杀四(数据库悲观锁)",nickname="自来居")
    @PostMapping("/single/startDBPCC_ONE")
    public Result startDBPCC_ONE(long seckillId,int number){
        int skillNum = number;
        // N个购买者
        final CountDownLatch latch = new CountDownLatch(skillNum);
        seckillService.deleteSeckill(seckillId);
        final long killId =  seckillId;
        LOGGER.info("开始秒杀四(正常)");
        for(int i=0;i<skillNum;i++){
            final long userId = i;
            Runnable task = () -> {
                Result result = seckillService.startSeckilDBPCC_ONE(killId, userId);
                if(result != null) {
                    LOGGER.info("用户:{}{}", userId, result.get("msg"));
                }else{
                    LOGGER.info("用户:{}{}", userId, "抢占资源失败");
                }
                latch.countDown();
            };

            // 必须抛出异常 或者返回原始队列 不然countdown出问题
            try {
                executor.execute(task);
            } catch (Exception e) {
                latch.countDown();
            } finally {

            }
        }
        try {
            latch.await();// 等待所有人任务结束
            Long seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }
    
    
    @ApiOperation(value="秒杀五(数据库悲观锁)",nickname="自来居")
    @PostMapping("/single/startDPCC_TWO")
    public Result startDPCC_TWO(long seckillId,int number){
        int skillNum = number;
        final CountDownLatch latch = new CountDownLatch(skillNum);//N个购买者
        seckillService.deleteSeckill(seckillId);
        final long killId =  seckillId;
        LOGGER.info("开始秒杀五(正常、数据库锁最优实现)");
        for(int i=0;i<skillNum;i++){
            final long userId = i;
            Runnable task = () -> {
                Result result = seckillService.startSeckilDBPCC_TWO(killId, userId);
                LOGGER.info("用户:{}{}",userId,result.get("msg"));
                latch.countDown();
            };
            try {
                executor.execute(task);
            } catch (Exception e) {
                latch.countDown();
            } finally {
            }
        }
        try {
            latch.await();// 等待所有人任务结束
            Long  seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }
    
    
    @ApiOperation(value="秒杀六(数据库乐观锁)",nickname="自来居")
    @PostMapping("/single/startDBOCC")
    public Result startDBOCC(long seckillId,int number){
        int skillNum = number;
        // N个购买者
        final CountDownLatch latch = new CountDownLatch(skillNum);
        seckillService.deleteSeckill(seckillId);
        final long killId =  seckillId;
        LOGGER.info("开始秒杀六(正常、数据库锁最优实现)");
        for(int i=0;i<skillNum;i++){
            final long userId = i;
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    //这里使用的乐观锁、可以自定义抢购数量、如果配置的抢购人数比较少、比如120:100(人数:商品) 会出现少买的情况
                    //用户同时进入会出现更新失败的情况
                    Result result = seckillService.startSeckilDBOCC(killId, userId,1);
                    LOGGER.info("用户:{}{}",userId,result.get("msg"));
                    latch.countDown();
                }
            };
            try {
                executor.execute(task);
            } catch (Exception e) {
                latch.countDown();
            } finally {
            }
        }
        try {
            latch.await();// 等待所有人任务结束
            Long  seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @ApiOperation(value="秒杀柒(进程内队列)",nickname="科帮网")
    @PostMapping("/single/startQueue")
    public Result startQueue(long seckillId,int number){

        seckillService.deleteSeckill(seckillId);
        final long killId =  seckillId;
        LOGGER.info("开始秒杀柒(正常)");
        for(int i=0;i<number;i++){
            final long userId = i;
            Runnable task = () -> {
                SuccessKilled kill = new SuccessKilled();
                kill.setSeckillId(killId);
                kill.setUserId(userId);
                try {
                    Boolean flag = SeckillQueue.getQueue().produce(kill);
                    if(flag){
                        LOGGER.info("用户:{}{}",kill.getUserId(),"秒杀成功");
                    }else{
                        LOGGER.info("用户:{}{}",userId,"秒杀失败");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    LOGGER.info("用户:{}{}",userId,"秒杀失败");
                }
            };
            try {
                executor.execute(task);
            } catch (Exception e) {

            } finally {
            }
        }
        try {
            Thread.sleep(10000);
            Long seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @ApiOperation(value="秒杀柒(Disruptor队列)",nickname="科帮网")
    @PostMapping("/single/startDisruptorQueue")
    public Result startDisruptorQueue(long seckillId,int number){
        seckillService.deleteSeckill(seckillId);
        final long killId =  seckillId;
        LOGGER.info("开始秒杀八(正常)");
        for(int i=0;i<number;i++){
            final long userId = i;
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    SeckillEvent kill = new SeckillEvent();
                    kill.setSeckillId(killId);
                    kill.setUserId(userId);
                    DisruptorUtil.producer(kill);
                }
            };
            try {
                executor.execute(task);
            } catch (Exception e) {

            } finally {
            }
        }
        try {
            Thread.sleep(10000);
            Long seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }
}
