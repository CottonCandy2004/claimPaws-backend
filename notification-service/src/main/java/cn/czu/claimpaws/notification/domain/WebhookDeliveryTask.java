package cn.czu.claimpaws.notification.domain;

import java.time.LocalDateTime;

public record WebhookDeliveryTask(Long id, String eventId, String eventType, String payload,
                                  String endpointUrl, String encryptedSecret, int retryCount,
                                  LocalDateTime nextAttemptAt) {
}
