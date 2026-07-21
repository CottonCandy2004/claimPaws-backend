package cn.czu.claimpaws.reservation.domain;

import cn.czu.claimpaws.reservation.infrastructure.ReservationSnapshotDTO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record Reservation(
        long id,
        long userId,
        long resourceId,
        String resourceName,
        String resourceType,
        Instant startAt,
        Instant endAt,
        ReservationStatus status,
        int approvalLevel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Reservation create(long userId, CreateReservationCommand command, ReservationSnapshotDTO snapshot) {
        int approvalLevel = snapshot.policy().requiresApproval() ? snapshot.policy().approvalLevel() : 0;
        return new Reservation(
                0L,
                userId,
                command.resourceId(),
                snapshot.resource().name(),
                snapshot.resource().type(),
                command.startAt(),
                command.endAt(),
                ReservationStatus.PENDING_APPROVAL,
                approvalLevel,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
