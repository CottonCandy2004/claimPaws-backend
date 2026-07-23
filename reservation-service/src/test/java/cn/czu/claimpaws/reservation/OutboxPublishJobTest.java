package cn.czu.claimpaws.reservation;

import cn.czu.claimpaws.reservation.domain.OutboxMessage;
import cn.czu.claimpaws.reservation.infrastructure.OutboxPublisher;
import cn.czu.claimpaws.reservation.job.OutboxPublishJob;
import cn.czu.claimpaws.reservation.persistence.OutboxMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxPublishJobTest {

    @Test
    void marksMessagePublishedOnlyAfterBrokerConfirmation() {
        FakeMapper mapper = new FakeMapper(1);
        new OutboxPublishJob(mapper, new FakePublisher(false)).publishPending();

        assertThat(mapper.status).isEqualTo("PUBLISHED");
    }

    @Test
    void releasesClaimWhenBrokerDoesNotConfirmPublication() {
        FakeMapper mapper = new FakeMapper(1);
        new OutboxPublishJob(mapper, new FakePublisher(true)).publishPending();

        assertThat(mapper.status).isEqualTo("PENDING");
    }

    @Test
    void doesNotPublishMessageClaimedByAnotherInstance() {
        FakeMapper mapper = new FakeMapper(0);
        FakePublisher publisher = new FakePublisher(false);
        new OutboxPublishJob(mapper, publisher).publishPending();

        assertThat(publisher.published).isFalse();
    }

    private static final class FakeMapper implements OutboxMapper {
        private final int claimResult;
        private String status = "PENDING";

        private FakeMapper(int claimResult) { this.claimResult = claimResult; }
        @Override public void insert(OutboxMessage message) { }
        @Override public long countByType(String eventType) { return 0; }
        @Override public OutboxMessage findByAggregateId(long aggregateId) { return null; }
        @Override public List<OutboxMessage> findClaimCandidates() { return List.of(pendingMessage()); }
        @Override public int claim(Long id, String owner, LocalDateTime lockedUntil) { return claimResult; }
        @Override public int updateStatus(Long id, String owner, String status) { this.status = status; return 1; }
        @Override public int releaseClaim(Long id, String owner) { this.status = "PENDING"; return 1; }
    }

    private static final class FakePublisher extends OutboxPublisher {
        private final boolean reject;
        private boolean published;
        private FakePublisher(boolean reject) { super(null, null); this.reject = reject; }
        @Override public void publish(OutboxMessage message) {
            if (reject) throw new IllegalStateException("broker nack");
            published = true;
        }
    }

    private static OutboxMessage pendingMessage() {
        return new OutboxMessage(1L, "event-1", "reservation.created", 11L, 1,
                "{\"reservationId\":11}", "PENDING", LocalDateTime.now(), LocalDateTime.now());
    }
}
