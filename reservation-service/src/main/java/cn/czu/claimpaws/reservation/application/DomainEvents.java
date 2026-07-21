package cn.czu.claimpaws.reservation.application;

import cn.czu.claimpaws.common.event.DomainEvent;
import cn.czu.claimpaws.reservation.domain.Reservation;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.UUID;

public class DomainEvents {
    public static DomainEvent reservationCreated(Reservation reservation) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("reservationId", reservation.id());
        payload.put("userId", reservation.userId());
        payload.put("resourceId", reservation.resourceId());
        payload.put("resourceName", reservation.resourceName());
        payload.put("startAt", reservation.startAt().toString());
        payload.put("endAt", reservation.endAt().toString());
        payload.put("status", reservation.status().name());

        return new DomainEvent(
                UUID.randomUUID(),
                "reservation.created",
                Instant.now(),
                reservation.id(),
                1,
                payload
        );
    }
}
