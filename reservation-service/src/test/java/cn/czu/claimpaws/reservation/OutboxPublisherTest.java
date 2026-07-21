package cn.czu.claimpaws.reservation;

import cn.czu.claimpaws.common.event.DomainEvent;
import cn.czu.claimpaws.reservation.domain.OutboxMessage;
import cn.czu.claimpaws.reservation.infrastructure.OutboxPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboxPublisherTest {

    @Test
    void rejectsNackBeforeReturningSuccessToOutboxJob() {
        RabbitTemplate rabbitTemplate = new NackRabbitTemplate();
        OutboxPublisher publisher = new OutboxPublisher(rabbitTemplate, new ObjectMapper());

        assertThatThrownBy(() -> publisher.publish(pendingMessage()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("rejected");
    }

    private static final class NackRabbitTemplate extends RabbitTemplate {
        @Override
        public void convertAndSend(String exchange, String routingKey, Object object, CorrelationData correlationData) {
            correlationData.getFuture().complete(new CorrelationData.Confirm(false, "broker rejected"));
        }
    }

    private static OutboxMessage pendingMessage() {
        return new OutboxMessage(1L, "550e8400-e29b-41d4-a716-446655440000", "reservation.created", 11L, 1,
                "{\"reservationId\":11}", "PENDING", LocalDateTime.now(), LocalDateTime.now());
    }
}
