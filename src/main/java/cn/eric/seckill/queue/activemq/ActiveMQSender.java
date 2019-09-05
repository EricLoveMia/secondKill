package cn.eric.seckill.queue.activemq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Destination;

/**
 * @author Eric
 * @version 1.0
 * @ClassName: ActiveMQSender
 * @Description: TODO
 * @company lsj
 * @date 2019/8/5 10:28
 **/
@Component
public class ActiveMQSender {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    public void sendChannelMessage(String destination, final String message){

        jmsMessagingTemplate.convertAndSend(destination,message);
    }
}
