package cn.eric.seckill.web;

import cn.eric.seckill.common.entity.Result;
import cn.eric.seckill.common.redis.RedisUtil;
import cn.eric.seckill.queue.activemq.ActiveMQSender;
import cn.eric.seckill.service.ISeckillDistributedService;
import cn.eric.seckill.service.ISeckillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: SeckillDistributedController
 * @Description: TODO
 * @company lsj
 * @date 2019/8/2 10:44
 **/
@Api(tags ="分布式秒杀")
@RestController
@RequestMapping("/seckillDistributed")
public class SeckillDistributedController {


    private final static Logger LOGGER = LoggerFactory.getLogger(SeckillDistributedController.class);

    private static int corePoolSize = Runtime.getRuntime().availableProcessors();
    //调整队列数 拒绝服务
    private static ThreadPoolExecutor executor  = new ThreadPoolExecutor(corePoolSize, corePoolSize+1, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(10000));

    @Autowired
    private ISeckillService seckillService;

    @Autowired
    private ISeckillDistributedService seckillDistributedService;
//    @Autowired
//    private RedisSender redisSender;
//    @Autowired
//    private KafkaSender kafkaSender;
    @Autowired
    private ActiveMQSender activeMQSender;

    @Autowired
    private RedisUtil redisUtil;


    @ApiOperation(value="秒杀一(Rediss分布式锁)",nickname="科帮网")
    @PostMapping("/startRedisLock")
    public Result startRedisLock(long seckillId,int number){
        seckillService.deleteSeckill(seckillId);
        final long killId =  seckillId;
        LOGGER.info("开始秒杀一");
        for(int i=0;i<number;i++){
            final long userId = i;
            Runnable task = () -> {
                Result result = seckillDistributedService.startSeckilRedisLock(killId, userId);
                LOGGER.info("用户:{}{}",userId,result.get("msg"));
            };
            try {
                executor.execute(task);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        try {
            Thread.sleep(15000);
            Long  seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @ApiOperation(value="秒杀二(zookeeper分布式锁)",nickname="科帮网")
    @PostMapping("/startZkLock")
    public Result startZkLock(long seckillId){
        seckillService.deleteSeckill(seckillId);
        final long killId =  seckillId;
        LOGGER.info("开始秒杀二");
        for(int i=0;i<10000;i++){
            final long userId = i;
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Result result = seckillDistributedService.startSeckilZksLock(killId, userId);
                    LOGGER.info("用户:{}{}",userId,result.get("msg"));
                }
            };
            executor.execute(task);
        }
        try {
            Thread.sleep(10000);
            Long  seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }




    @ApiOperation(value="秒杀五(ActiveMQ分布式队列)",nickname="科帮网")
    @PostMapping("/startActiveMQQueue")
    public Result startActiveMQQueue(long seckillId,int number){
        seckillService.deleteSeckill(seckillId);
        final long killId =  seckillId;
        LOGGER.info("开始秒杀五");

        for(int i=0;i<number;i++){
            final long userId = i;
            Runnable task = () -> {
                if(redisUtil.getValue(killId+"")==null){
                    String destination = "seckill.queue";
                    //思考如何返回给用户信息ws
                    activeMQSender.sendChannelMessage(destination,killId+";"+userId);
                }else{
                    //秒杀结束
                }
            };
            try {
                executor.execute(task);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        try {
            Thread.sleep(10000);
            redisUtil.cacheValue(killId+"", null);
            Long  seckillCount = seckillService.getSeckillCount(seckillId);
            LOGGER.info("一共秒杀出{}件商品",seckillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }
}
