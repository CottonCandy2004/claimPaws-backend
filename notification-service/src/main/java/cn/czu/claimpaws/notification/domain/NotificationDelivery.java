package cn.czu.claimpaws.notification.domain;

import java.time.LocalDateTime;

public record NotificationDelivery(
        Long id,
        Long webhookConfigId,
        String eventId,
        String eventType,
        String payload,
        String endpointUrl,
        String status,
        int retryCount,
        LocalDateTime lastAttemptAt,
        LocalDateTime nextAttemptAt,
        Integer responseStatus,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
