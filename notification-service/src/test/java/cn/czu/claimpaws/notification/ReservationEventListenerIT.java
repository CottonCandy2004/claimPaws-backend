package cn.czu.claimpaws.notification;

import cn.czu.claimpaws.common.event.DomainEvent;
import cn.czu.claimpaws.notification.messaging.ReservationEventListener;
import cn.czu.claimpaws.notification.persistence.DeliveryMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class ReservationEventListenerIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8")
            .withDatabaseName("claimpaws_notification");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    @Autowired
    private ReservationEventListener listener;

    @Autowired
    private DeliveryMapper deliveryMapper;

    @Test
    void ignoresDuplicateEventId() {
        var event = new DomainEvent(
                UUID.randomUUID(),
                "reservation.created",
                Instant.now(),
                1L,
                1,
                JsonNodeFactory.instance.objectNode().put("reservationId", "1")
        );

        listener.consume(event);
        listener.consume(event);

        assertThat(deliveryMapper.countByEventId(event.eventId().toString())).isEqualTo(1);
    }
}
