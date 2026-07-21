package cn.czu.claimpaws.notification.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationEventListenerTest {
    @Test
    void usesTheListenerFactoryThatRejectsMessagesAfterBoundedRetries() throws Exception {
        RabbitListener listener = ReservationEventListener.class
                .getMethod("consume", cn.czu.claimpaws.common.event.DomainEvent.class)
                .getAnnotation(RabbitListener.class);

        assertThat(listener.containerFactory()).isEqualTo("notificationRabbitListenerContainerFactory");
    }
}
