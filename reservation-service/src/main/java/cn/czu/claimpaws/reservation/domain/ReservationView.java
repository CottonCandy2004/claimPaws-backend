package cn.czu.claimpaws.reservation.domain;

import java.time.Instant;

public record ReservationView(
        long id,
        long resourceId,
        String resourceName,
        String resourceType,
        Instant startAt,
        Instant endAt,
        String status
) {
    public static ReservationView from(Reservation reservation, long generatedId) {
        return new ReservationView(
                generatedId > 0 ? generatedId : reservation.id(),
                reservation.resourceId(),
                reservation.resourceName(),
                reservation.resourceType(),
                reservation.startAt(),
                reservation.endAt(),
                reservation.status().name()
        );
    }
}
