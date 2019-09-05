package cn.eric.seckill.queue.jvm;

import cn.eric.seckill.common.entity.SuccessKilled;
import cn.eric.seckill.service.ISeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 消费秒杀队列
 * ApplicationRunner CommandLineRunner
   应用服务启动时，加载一些数据和执行一些应用的初始化动作。如：删除临时文件，清除缓存信息，读取配置文件信息，数据库连接等
 */
@Component
public class TaskRunner implements ApplicationRunner {
	
	@Autowired
	private ISeckillService seckillService;
	
	@Override
    public void run(ApplicationArguments var) throws Exception{
		while(true){
			//进程内队列
			SuccessKilled kill = SeckillQueue.getQueue().consume();
			if(kill!=null){
				seckillService.startSeckil(kill.getSeckillId(), kill.getUserId());
			}
		}
    }
}