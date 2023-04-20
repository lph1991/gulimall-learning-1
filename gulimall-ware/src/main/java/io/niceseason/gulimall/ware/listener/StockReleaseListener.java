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
@Slf4j
@Component
@RabbitListener(queues = {"stock.release.stock.queue"})
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        log.info("************************收到库存解锁的消息********************************");
        try {
            wareSkuService.unlock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
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
