package cn.czu.claimpaws.common.event;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record DomainEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        long aggregateId,
        int schemaVersion,
        JsonNode payload) {

    public DomainEvent {
        if (eventId == null || eventType == null || eventType.isBlank() || occurredAt == null || payload == null) {
            throw new IllegalArgumentException("event fields are required");
        }
        payload = payload.deepCopy();
    }

    @Override
    public JsonNode payload() {
        return payload.deepCopy();
    }
}
