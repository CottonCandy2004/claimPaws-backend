package cn.czu.claimpaws.reservation.domain;

import cn.czu.claimpaws.common.event.DomainEvent;

import java.time.LocalDateTime;

public record OutboxMessage(
        Long id,
        String eventId,
        String eventType,
        long aggregateId,
        int schemaVersion,
        String payload,
        String status,
        LocalDateTime createdAt
) {
    public static OutboxMessage created(DomainEvent event) {
        return new OutboxMessage(
                null,
                event.eventId().toString(),
                event.eventType(),
                event.aggregateId(),
                event.schemaVersion(),
                event.payload().toString(),
                "PENDING",
                LocalDateTime.now()
        );
    }
}
