package cn.czu.claimpaws.notification.application;

import cn.czu.claimpaws.notification.persistence.DeliveryMapper;
import cn.czu.claimpaws.notification.domain.NotificationDelivery;
import cn.czu.claimpaws.notification.domain.WebhookDeliveryTask;
import cn.czu.claimpaws.notification.persistence.WebhookConfigMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookDeliveryServiceTest {
    @Test
    void requeuesExpiredProcessingClaimsSoACrashedWorkerDoesNotPermanentlyBlockDelivery() {
        RecordingDeliveryMapper mapper = new RecordingDeliveryMapper();
        WebhookDeliveryService service = new WebhookDeliveryService(mapper, new NoopWebhookConfigMapper());
        LocalDateTime expiredBefore = LocalDateTime.of(2026, 7, 21, 10, 0);

        service.recoverExpiredClaims(expiredBefore);

        assertThat(mapper.expiredBefore).isEqualTo(expiredBefore);
    }

    private static final class RecordingDeliveryMapper implements DeliveryMapper {
        private LocalDateTime expiredBefore;
        @Override public long countByEventId(String eventId) { return 0; }
        @Override public void insert(NotificationDelivery delivery) { }
        @Override public List<WebhookDeliveryTask> findDue(LocalDateTime now, int limit) { return List.of(); }
        @Override public int markProcessing(long id) { return 0; }
        @Override public int recoverExpiredClaims(LocalDateTime expiredBefore) { this.expiredBefore = expiredBefore; return 0; }
        @Override public void markSucceeded(long id, LocalDateTime attemptedAt, int responseStatus) { }
        @Override public void markRetry(long id, int retryCount, LocalDateTime attemptedAt, LocalDateTime nextAttemptAt, Integer responseStatus, String failureReason) { }
        @Override public void markFailed(long id, int retryCount, LocalDateTime attemptedAt, Integer responseStatus, String failureReason) { }
        @Override public int resetForRetry(long id) { return 0; }
        @Override public List<java.util.Map<String, Object>> findAuditPage(int offset, int limit, Long webhookId, String status) { return List.of(); }
        @Override public long countAudits(Long webhookId, String status) { return 0; }
    }

    private static final class NoopWebhookConfigMapper implements WebhookConfigMapper {
        @Override public List<cn.czu.claimpaws.notification.domain.WebhookConfig> findEnabled() { return List.of(); }
        @Override public void insert(String endpointUrl, String encryptedSecret, boolean enabled) { }
        @Override public List<cn.czu.claimpaws.notification.domain.WebhookConfig> findPage(int offset, int limit) { return List.of(); }
        @Override public long count() { return 0; }
        @Override public cn.czu.claimpaws.notification.domain.WebhookConfig findById(long id) { return null; }
        @Override public int update(String endpointUrl, String encryptedSecret, boolean enabled, long id) { return 0; }
        @Override public int deleteById(long id) { return 0; }
    }
}
