package cn.czu.claimpaws.reservation.job;

import cn.czu.claimpaws.reservation.domain.OutboxMessage;
import cn.czu.claimpaws.reservation.persistence.OutboxMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublishJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublishJob.class);

    private final OutboxMapper outboxMapper;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublishJob(OutboxMapper outboxMapper, RabbitTemplate rabbitTemplate) {
        this.outboxMapper = outboxMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPending() {
        List<OutboxMessage> pending = outboxMapper.findPending();
        for (OutboxMessage message : pending) {
            try {
                rabbitTemplate.convertAndSend("claimpaws.domain.events", message.eventType(), message.eventId());
                outboxMapper.updateStatus(message.id(), "PUBLISHED");
                log.debug("Published outbox message {}", message.eventId());
            } catch (Exception e) {
                log.error("Failed to publish outbox message {}", message.eventId(), e);
            }
        }
    }
}
