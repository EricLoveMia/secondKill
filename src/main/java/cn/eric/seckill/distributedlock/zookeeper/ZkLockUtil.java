package cn.eric.seckill.distributedlock.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

/**
 * zookeeper 分布式锁
 * @author 科帮网 By https://blog.52itstyle.com
 */
public class ZkLockUtil {
	
	private static String address = "localhost:2181";
	
	public static CuratorFramework client;
	
	static{
		try {
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
			client = CuratorFrameworkFactory.newClient(address,retryPolicy);
			client.start();
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
     * 私有的默认构造子，保证外界无法直接实例化
     */
    private ZkLockUtil(){};

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例
     * 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     * 针对一件商品实现，多件商品同时秒杀建议实现一个map
     */
    private static class SingletonHolder{
        /**
         * 静态初始化器，由JVM来保证线程安全
         * 参考：http://ifeve.com/zookeeper-lock/
         * 这里建议 new 一个
         */
    	private static InterProcessMutex mutex = new InterProcessMutex(client, "/lock");
    }

    /** InterProcessMutex基于Zookeeper实现了分布式的公平可重入互斥锁，
	 * 类似于单个JVM进程内的ReentrantLock(fair=true) **/
    public static InterProcessMutex getMutex(){
        return SingletonHolder.mutex;
    }

    //获得了锁
    public static boolean acquire(long time, TimeUnit unit){
    	try {
			return getMutex().acquire(time,unit);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
    }

    //释放锁
    public static void release(){
    	try {
			getMutex().release();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}  
