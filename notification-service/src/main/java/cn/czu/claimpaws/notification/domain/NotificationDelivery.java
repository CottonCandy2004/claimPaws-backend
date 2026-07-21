package cn.czu.claimpaws.notification.domain;

import java.time.LocalDateTime;

public record NotificationDelivery(
        Long id,
        String eventId,
        String eventType,
        String payload,
        String signature,
        String status,
        int retryCount,
        LocalDateTime lastAttemptAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
