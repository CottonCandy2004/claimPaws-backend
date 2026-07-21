package cn.czu.claimpaws.reservation.job;

import cn.czu.claimpaws.reservation.domain.OutboxMessage;
import cn.czu.claimpaws.reservation.persistence.OutboxMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final ObjectMapper objectMapper;

    public OutboxPublishJob(OutboxMapper outboxMapper, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.outboxMapper = outboxMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPending() {
        List<OutboxMessage> messageList = outboxMapper.findPending();
        for (OutboxMessage message : messageList) {
            try {
                ObjectNode eventJson = objectMapper.createObjectNode();
                eventJson.put("eventId", message.eventId());
                eventJson.put("eventType", message.eventType());
                eventJson.put("occurredAt", message.occurredAt().toString());
                eventJson.put("aggregateId", message.aggregateId());
                eventJson.put("schemaVersion", message.schemaVersion());
                JsonNode payloadNode = objectMapper.readTree(message.payload());
                eventJson.set("payload", payloadNode);
                String json = objectMapper.writeValueAsString(eventJson);
                rabbitTemplate.convertAndSend("claimpaws.domain.events", message.eventType(), json);
                outboxMapper.updateStatus(message.id(), "PUBLISHED");
                log.debug("Published outbox message {}", message.eventId());
            } catch (Exception e) {
                log.error("Failed to publish outbox message {}", message.eventId(), e);
            }
        }
    }
}
