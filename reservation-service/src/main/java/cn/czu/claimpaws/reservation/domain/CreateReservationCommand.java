package cn.czu.claimpaws.reservation.domain;

import java.time.Instant;

public record CreateReservationCommand(long resourceId, String title, Instant startAt, Instant endAt) {
}
