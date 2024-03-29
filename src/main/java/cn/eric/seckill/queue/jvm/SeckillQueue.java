package cn.eric.seckill.queue.jvm;

import cn.eric.seckill.common.entity.SuccessKilled;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: SeckillQueue
 * @Description: TODO
 * @company lsj
 * @date 2019/7/31 15:46
 **/
public class SeckillQueue {

    static int QUEUE_MAX = 1000;

    static BlockingQueue<SuccessKilled> blockingQueue = new LinkedBlockingQueue<>(QUEUE_MAX);

    /*** 私有的构造函数，外界无法直接实例化*/
    private SeckillQueue(){

    }

//
//    private SeckillQueue(int max){
//        QUEUE_MAX = max;
//    };

    private static class SingleTonHolder{
        /**
         * 静态初始化器，由JVM来保证线程安全
         * 静态内部类和非静态内部类一样，都是在被调用时才会被加载
         */
        private static SeckillQueue queue = new SeckillQueue();
    }

    public static SeckillQueue getQueue(){
        return SingleTonHolder.queue;
    }

    /**
     * 生产入队
     * @param kill
     * @throws InterruptedException
     * add(e) 队列未满时，返回true；队列满则抛出IllegalStateException(“Queue full”)异常——AbstractQueue
     * put(e) 队列未满时，直接插入没有返回值；队列满时会阻塞等待，一直等到队列未满时再插入。
     * offer(e) 队列未满时，返回true；队列满时返回false。非阻塞立即返回。
     * offer(e, time, unit) 设定等待的时间，如果在指定时间内还不能往队列中插入数据则返回false，插入成功返回true。
     */
    public Boolean produce(SuccessKilled kill) throws InterruptedException {
        return blockingQueue.offer(kill);
    }

    /**
     * 消费出队
     * poll() 获取并移除队首元素，在指定的时间内去轮询队列看有没有首元素有则返回，否者超时后返回null
     * take() 与带超时时间的poll类似不同在于take时候如果当前队列空了它会一直等待其他线程调用notEmpty.signal()才会被唤醒
     */
    public  SuccessKilled consume() throws InterruptedException {
        return blockingQueue.take();
    }

    // 获取队列大小
    public int size() {
        return blockingQueue.size();
    }
}
