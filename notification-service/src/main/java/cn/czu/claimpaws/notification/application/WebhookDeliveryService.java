package cn.czu.claimpaws.notification.application;

import cn.czu.claimpaws.common.event.DomainEvent;
import cn.czu.claimpaws.notification.domain.NotificationDelivery;
import cn.czu.claimpaws.notification.persistence.DeliveryMapper;
import cn.czu.claimpaws.notification.persistence.WebhookConfigMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class WebhookDeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final WebhookConfigMapper webhookConfigMapper;

    public WebhookDeliveryService(DeliveryMapper deliveryMapper, WebhookConfigMapper webhookConfigMapper) {
        this.deliveryMapper = deliveryMapper;
        this.webhookConfigMapper = webhookConfigMapper;
    }

    @Transactional
    public void createDeliveries(DomainEvent event) {
        String payload = event.payload().toString();
        webhookConfigMapper.findEnabled().forEach(config -> deliveryMapper.insert(new NotificationDelivery(
                null, config.id(), event.eventId().toString(), event.eventType(), payload, config.endpointUrl(),
                "PENDING", 0, null, LocalDateTime.now(ZoneOffset.UTC), null, null, null, null)));
    }

    @Transactional
    public List<cn.czu.claimpaws.notification.domain.WebhookDeliveryTask> claimDue(LocalDateTime now, int limit) {
        recoverExpiredClaims(now.minusMinutes(5));
        return deliveryMapper.findDue(now, limit).stream()
                .filter(task -> deliveryMapper.markProcessing(task.id()) == 1)
                .toList();
    }

    @Transactional
    public void recoverExpiredClaims(LocalDateTime expiredBefore) {
        deliveryMapper.recoverExpiredClaims(expiredBefore);
    }

    @Transactional
    public void markSucceeded(long id, LocalDateTime attemptedAt, int responseStatus) {
        deliveryMapper.markSucceeded(id, attemptedAt, responseStatus);
    }

    @Transactional
    public void markRetry(long id, int retryCount, LocalDateTime attemptedAt, LocalDateTime nextAttemptAt,
                          Integer responseStatus, String failureReason) {
        deliveryMapper.markRetry(id, retryCount, attemptedAt, nextAttemptAt, responseStatus, failureReason);
    }

    @Transactional
    public void markFailed(long id, int retryCount, LocalDateTime attemptedAt, Integer responseStatus,
                           String failureReason) {
        deliveryMapper.markFailed(id, retryCount, attemptedAt, responseStatus, failureReason);
    }
}
