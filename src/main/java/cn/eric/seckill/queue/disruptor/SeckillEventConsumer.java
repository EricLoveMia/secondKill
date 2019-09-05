package cn.eric.seckill.queue.disruptor;

import cn.eric.seckill.common.config.SpringUtil;
import cn.eric.seckill.service.ISeckillService;
import com.lmax.disruptor.EventHandler;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: SeckillEventComsumer
 * @Description: TODO
 * @company lsj
 * @date 2019/8/1 17:50
 **/
public class SeckillEventConsumer implements EventHandler<SeckillEvent> {

    private ISeckillService seckillService = (ISeckillService) SpringUtil.getBean("seckillService");

    @Override
    public void onEvent(SeckillEvent seckillEvent, long seq, boolean bool) throws Exception {
        seckillService.startSeckil(seckillEvent.getSeckillId(), seckillEvent.getUserId());
    }
}
