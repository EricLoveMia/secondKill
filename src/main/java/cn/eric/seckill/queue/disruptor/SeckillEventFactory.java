package cn.eric.seckill.queue.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 *
 * 事件生成工厂（用来初始化预分配事件对象）
 * @author Eric
 * @version 1.0
 * @ClassName: SeckillEventFactory
 * @Description: TODO
 * @company lsj
 * @date 2019/8/1 17:45
 **/
public class SeckillEventFactory implements EventFactory<SeckillEvent> {
    @Override
    public SeckillEvent newInstance() {
        return new SeckillEvent();
    }
}
