package cn.czu.claimpaws.reservation;

import cn.czu.claimpaws.reservation.application.ReservationService;
import cn.czu.claimpaws.reservation.domain.CreateReservationCommand;
import cn.czu.claimpaws.reservation.infrastructure.ResourceClient;
import cn.czu.claimpaws.reservation.infrastructure.ReservationSnapshotDTO;
import cn.czu.claimpaws.reservation.persistence.OutboxMapper;
import cn.czu.claimpaws.reservation.persistence.ReservationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class ReservationServiceIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8")
            .withDatabaseName("claimpaws_reservation");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private ReservationService reservationService;

    @MockBean
    private ResourceClient resourceClient;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private OutboxMapper outboxMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clearPersistentState() {
        jdbcTemplate.update("DELETE FROM reservation_occupied_slots");
        jdbcTemplate.update("DELETE FROM outbox_messages");
        jdbcTemplate.update("DELETE FROM reservations");
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void createsOneReservationAndOneOutboxEventInSameTransaction() {
        when(resourceClient.getSnapshot(anyLong())).thenReturn(
                new ReservationSnapshotDTO(
                        new ReservationSnapshotDTO.ResourceInfo(1L, "会议室A", "MEETING_ROOM", 10, true),
                        new ReservationSnapshotDTO.PolicyInfo(30, 30, 30, 480, false, 0, ""),
                        System.currentTimeMillis()
                )
        );

        Instant start = Instant.parse("2026-07-21T08:00:00Z");
        var command = new CreateReservationCommand(1L, "", start, start.plusSeconds(3600));
        var created = reservationService.create(1L, "key-1", command);

        assertThat(reservationMapper.count()).isEqualTo(1);
        assertThat(outboxMapper.countByType("reservation.created")).isEqualTo(1);
        assertThat(outboxMapper.findByAggregateId(created.id()).aggregateId()).isEqualTo(created.id());
    }

    @Test
    void rejectsOverlappingReservationAfterTheFirstReservationOccupiesItsSlots() {
        when(resourceClient.getSnapshot(anyLong())).thenReturn(
                new ReservationSnapshotDTO(
                        new ReservationSnapshotDTO.ResourceInfo(1L, "会议室A", "MEETING_ROOM", 10, true),
                        new ReservationSnapshotDTO.PolicyInfo(30, 30, 30, 480, false, 0, ""),
                        System.currentTimeMillis()
                )
        );
        Instant start = Instant.parse("2026-07-21T08:00:00Z");
        reservationService.create(1L, "key-overlap-a", new CreateReservationCommand(1L, "", start, start.plusSeconds(3600)));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> reservationService.create(
                2L, "key-overlap-b", new CreateReservationCommand(1L, "", start.plusSeconds(1800), start.plusSeconds(5400))))
                .isInstanceOfSatisfying(cn.czu.claimpaws.common.exception.BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(cn.czu.claimpaws.common.exception.ErrorCode.RESERVATION_TIME_CONFLICT));
    }

    @Test
    void returnsTheOriginalResponseWhenTheSameIdempotencyKeyIsRetried() {
        when(resourceClient.getSnapshot(anyLong())).thenReturn(
                new ReservationSnapshotDTO(
                        new ReservationSnapshotDTO.ResourceInfo(1L, "会议室A", "MEETING_ROOM", 10, true),
                        new ReservationSnapshotDTO.PolicyInfo(30, 30, 30, 480, false, 0, ""), System.currentTimeMillis()));
        Instant start = Instant.parse("2026-07-23T08:00:00Z");
        var command = new CreateReservationCommand(1L, "", start, start.plusSeconds(3600));

        var first = reservationService.create(1L, "retryable-key", command);
        var retry = reservationService.create(1L, "retryable-key", command);

        assertThat(retry).isEqualTo(first);
        assertThat(reservationMapper.count()).isEqualTo(1);
    }

    @Test
    void allowsOnlyOneConcurrentReservationForTheSameResourceSlots() throws Exception {
        when(resourceClient.getSnapshot(anyLong())).thenReturn(
                new ReservationSnapshotDTO(
                        new ReservationSnapshotDTO.ResourceInfo(1L, "会议室A", "MEETING_ROOM", 10, true),
                        new ReservationSnapshotDTO.PolicyInfo(30, 30, 30, 480, false, 0, ""),
                        System.currentTimeMillis()));
        Instant start = Instant.parse("2026-07-22T08:00:00Z");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(() -> createAfterBarrier(1L, "key-concurrent-a", start, ready, go));
            Future<Boolean> second = executor.submit(() -> createAfterBarrier(2L, "key-concurrent-b", start, ready, go));
            ready.await();
            go.countDown();
            assertThat((first.get() ? 1 : 0) + (second.get() ? 1 : 0)).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean createAfterBarrier(long userId, String key, Instant start, CountDownLatch ready, CountDownLatch go) throws Exception {
        ready.countDown();
        go.await();
        try {
            reservationService.create(userId, key, new CreateReservationCommand(1L, "", start, start.plusSeconds(3600)));
            return true;
        } catch (cn.czu.claimpaws.common.exception.BusinessException expected) {
            return false;
        }
    }
}
