package cn.eric.seckill.queue.disruptor;

import java.io.Serializable;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: SeckillEvent
 * @Description: TODO
 * @company lsj
 * @date 2019/8/1 17:44
 **/
public class SeckillEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    private long seckillId;
    private long userId;

    public SeckillEvent(){

    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

}
