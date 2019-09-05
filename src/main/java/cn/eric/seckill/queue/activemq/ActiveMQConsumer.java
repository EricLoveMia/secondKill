package cn.eric.seckill.queue.activemq;

import cn.eric.seckill.common.entity.Result;
import cn.eric.seckill.common.enums.SeckillStatEnum;
import cn.eric.seckill.common.redis.RedisUtil;
import cn.eric.seckill.common.webSocket.WebSocketServer;
import cn.eric.seckill.service.ISeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: ActiveMQRecevier
 * @Description: TODO
 * @company lsj
 * @date 2019/8/5 10:43
 **/
@Component
public class ActiveMQConsumer {

    @Autowired
    private ISeckillService seckillService;
    @Autowired
    private RedisUtil redisUtil;

    // 使用JmsListener配置消费者监听的队列，其中text是接收到的消息
    @JmsListener(destination = "seckill.queue")
    public void receiveQueue(String message){
        // 收到通道的消息之后执行秒杀操作（超卖）
        //收到通道的消息之后执行秒杀操作(超卖)
        System.out.println(message);
        String[] array = message.split(";");
        System.out.println(Arrays.toString(array));
        Result result = seckillService.startSeckilDBPCC_TWO(Long.parseLong(array[0]), Long.parseLong(array[1]));
        if(result.equals(Result.ok(SeckillStatEnum.SUCCESS))){
            WebSocketServer.sendInfo(array[0].toString(), "秒杀成功");//推送给前台
        }else{
            WebSocketServer.sendInfo(array[0].toString(), "秒杀失败");//推送给前台
            redisUtil.cacheValue(array[0], "ok");//秒杀结束
        }
    }

}
