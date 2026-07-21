package cn.czu.claimpaws.notification.messaging;

import cn.czu.claimpaws.common.event.DomainEvent;
import cn.czu.claimpaws.notification.application.WebhookDeliveryService;
import cn.czu.claimpaws.notification.persistence.ConsumedEventMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReservationEventListener {

    private final ConsumedEventMapper consumedEventMapper;
    private final WebhookDeliveryService webhookDeliveryService;

    public ReservationEventListener(ConsumedEventMapper consumedEventMapper,
                                    WebhookDeliveryService webhookDeliveryService) {
        this.consumedEventMapper = consumedEventMapper;
        this.webhookDeliveryService = webhookDeliveryService;
    }

    @RabbitListener(queues = "notification.reservation.events")
    @Transactional
    public void consume(DomainEvent event) {
        if (consumedEventMapper.countByEventId(event.eventId().toString()) > 0) {
            return;
        }
        consumedEventMapper.insert(event.eventId().toString());
        webhookDeliveryService.createDeliveries(event);
    }
}
