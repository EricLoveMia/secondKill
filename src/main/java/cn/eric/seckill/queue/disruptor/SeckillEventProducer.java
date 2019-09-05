package cn.eric.seckill.queue.disruptor;

import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.RingBuffer;

/**
 * 使用translator方式生产者
 * @author Eric
 * @version 1.0
 * @ClassName: SeckillEventProducer
 * @Description: TODO
 * @company lsj
 * @date 2019/8/1 17:47
 **/
public class SeckillEventProducer {


    private final RingBuffer<SeckillEvent> ringBuffer;

    public SeckillEventProducer(RingBuffer<SeckillEvent> ringBuffer){
        this.ringBuffer = ringBuffer;
    }

    private final static EventTranslatorVararg<SeckillEvent> translator = new EventTranslatorVararg<SeckillEvent>() {
        @Override
        public void translateTo(SeckillEvent seckillEvent, long seq, Object... objs) {
            seckillEvent.setSeckillId((Long) objs[0]);
            seckillEvent.setUserId((Long) objs[1]);
        }
    };

    public void seckill(long seckillId,long userId){
        this.ringBuffer.publishEvent(translator,seckillId,userId);
    }

}
