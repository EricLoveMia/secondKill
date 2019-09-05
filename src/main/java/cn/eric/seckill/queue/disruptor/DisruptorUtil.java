package cn.eric.seckill.queue.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: DisruptorUtil
 * @Description: TODO
 * @company lsj
 * @date 2019/8/1 17:50
 **/
@Component
public class DisruptorUtil {
	
	static Disruptor<SeckillEvent> disruptor = null;

	static{
		SeckillEventFactory factory = new SeckillEventFactory();
		int ringBufferSize = 1024;
		ThreadFactory threadFactory = runnable -> new Thread(runnable);
		disruptor = new Disruptor<>(factory, ringBufferSize, threadFactory);
		disruptor.handleEventsWith(new SeckillEventConsumer());
		disruptor.start();
	}
	
	public static void producer(SeckillEvent kill){
		RingBuffer<SeckillEvent> ringBuffer = disruptor.getRingBuffer();
		SeckillEventProducer producer = new SeckillEventProducer(ringBuffer);
		producer.seckill(kill.getSeckillId(),kill.getUserId());
	}
}
