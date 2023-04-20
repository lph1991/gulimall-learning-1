package io.niceseason.gulimall.ware.listener;

import com.rabbitmq.client.Channel;
import io.niceseason.common.to.mq.OrderTo;
import io.niceseason.common.to.mq.StockLockedTo;
import io.niceseason.gulimall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * 库存服务监听死信队列
 * org.springframework.amqp.core.Message 只能接收简单消息，像字符串、数字这种
 * @RabbitListener(bindings = {
 *             @QueueBinding(value = @Queue(
 *                     value = direct_queue_b),
 *                     exchange = @Exchange(value = DIRECT_EXCHANGE, type = ExchangeTypes.DIRECT),
 *                     key = direct_queue_b)
 *     })
 * ————————————————
 * 版权声明：本文为CSDN博主「will_lam」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
 * 原文链接：https://blog.csdn.net/will_lam/article/details/105395116
 */
/**
 * 库存解锁监听
 *
 * @desc
 * 库存锁定成功发送消息到延时队列 stock.locked(路由key)，超时TTL，消息进入私信路由，然后转发到解锁库存的队列。
 *
 * @author: kaiyi
 * @create: 2020-09-16 19:01
 */
//————————————————
//        版权声明：本文为CSDN博主「我是陈旭原」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//        原文链接：https://blog.csdn.net/suchahaerkang/article/details/109131090

//    上边就是创建订单后锁库存，发消息到延时队列，监听队列，创建的订单出现异常是否来解锁库存，手动确认消息的核心代码逻辑。
@Slf4j
@Component
@RabbitListener(queues = {"stock.release.stock.queue"})
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;
    /**
     * 1、库存自动解锁
     *  下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁
     *
     *  2、订单失败
     *      库存锁定失败
     *
     *   只要解锁库存的消息失败，一定要告诉服务解锁失败
     */
//————————————————
//    版权声明：本文为CSDN博主「我是陈旭原」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//    原文链接：https://blog.csdn.net/suchahaerkang/article/details/109131090
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        log.info("************************收到库存解锁的消息********************************");
        try {
            //当前消息是否被第二次及以后（重新）派发过来了
            // Boolean redelivered = message.getMessageProperties().getRedelivered();

            //解锁库存
            wareSkuService.unlock(stockLockedTo);
            // 手动删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 解锁失败 将消息重新放回队列，让别人消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitHandler
    public void handleStockLockedRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("************************从订单模块收到库存解锁的消息********************************");
        try {
            wareSkuService.unlock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
