package cn.czu.claimpaws.reservation.domain;

import cn.czu.claimpaws.reservation.infrastructure.ReservationSnapshotDTO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class Reservation {
    private long id;
    private final long userId;
    private final long resourceId;
    private final String resourceName;
    private final String resourceType;
    private final Instant startAt;
    private final Instant endAt;
    private final ReservationStatus status;
    private final int approvalLevel;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Reservation(long id, long userId, long resourceId, String resourceName, String resourceType,
                       Instant startAt, Instant endAt, ReservationStatus status, int approvalLevel,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.approvalLevel = approvalLevel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long id() { return id; }
    // MyBatis assigns the database-generated identifier immediately after INSERT.
    public void setId(long id) { this.id = id; }
    public long userId() { return userId; }
    public long resourceId() { return resourceId; }
    public String resourceName() { return resourceName; }
    public String resourceType() { return resourceType; }
    public Instant startAt() { return startAt; }
    public Instant endAt() { return endAt; }
    public ReservationStatus status() { return status; }
    public int approvalLevel() { return approvalLevel; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }

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
