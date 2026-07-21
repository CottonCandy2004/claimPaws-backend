package cn.czu.claimpaws.reservation.infrastructure;

import cn.czu.claimpaws.common.event.DomainEvent;
import cn.czu.claimpaws.reservation.domain.OutboxMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

    static final String DOMAIN_EVENTS_EXCHANGE = "claimpaws.domain.events";
    private static final long CONFIRM_TIMEOUT_SECONDS = 5;

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(OutboxMessage message) {
        CorrelationData correlationData = new CorrelationData(message.eventId());
        rabbitTemplate.convertAndSend(DOMAIN_EVENTS_EXCHANGE, message.eventType(), toDomainEvent(message), correlationData);
        try {
            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(CONFIRM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!confirm.isAck()) {
                throw new IllegalStateException("Broker rejected outbox event " + message.eventId() + ": " + confirm.getReason());
            }
            if (correlationData.getReturned() != null) {
                throw new IllegalStateException("Broker returned unroutable outbox event " + message.eventId());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while awaiting broker confirmation", exception);
        } catch (java.util.concurrent.TimeoutException exception) {
            throw new IllegalStateException("Timed out awaiting broker confirmation for " + message.eventId(), exception);
        } catch (java.util.concurrent.ExecutionException exception) {
            throw new IllegalStateException("Failed while awaiting broker confirmation for " + message.eventId(), exception);
        }
    }

    private DomainEvent toDomainEvent(OutboxMessage message) {
        try {
            JsonNode payload = objectMapper.readTree(message.payload());
            return new DomainEvent(UUID.fromString(message.eventId()), message.eventType(),
                    message.occurredAt().toInstant(ZoneOffset.UTC), message.aggregateId(), message.schemaVersion(), payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot deserialize outbox event " + message.eventId(), exception);
        }
    }
}
