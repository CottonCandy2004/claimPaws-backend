package cn.czu.claimpaws.reservation;

import cn.czu.claimpaws.reservation.application.ReservationService;
import cn.czu.claimpaws.reservation.domain.CreateReservationCommand;
import cn.czu.claimpaws.reservation.infrastructure.ResourceClient;
import cn.czu.claimpaws.reservation.infrastructure.ReservationSnapshotDTO;
import cn.czu.claimpaws.reservation.persistence.OutboxMapper;
import cn.czu.claimpaws.reservation.persistence.ReservationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@Transactional
class ReservationServiceIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8")
            .withDatabaseName("claimpaws_reservation");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    @Autowired
    private ReservationService reservationService;

    @MockBean
    private ResourceClient resourceClient;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private OutboxMapper outboxMapper;

    @Test
    void createsOneReservationAndOneOutboxEventInSameTransaction() {
        when(resourceClient.getSnapshot(anyLong())).thenReturn(
                new ReservationSnapshotDTO(
                        new ReservationSnapshotDTO.ResourceInfo(1L, "会议室A", "MEETING_ROOM", 10, true),
                        new ReservationSnapshotDTO.PolicyInfo(30, 30, 30, 480, false, 0),
                        System.currentTimeMillis()
                )
        );

        var command = new CreateReservationCommand(1L, Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200));
        reservationService.create(1L, "key-1", command);

        assertThat(reservationMapper.count()).isEqualTo(1);
        assertThat(outboxMapper.countByType("reservation.created")).isEqualTo(1);
    }
}
