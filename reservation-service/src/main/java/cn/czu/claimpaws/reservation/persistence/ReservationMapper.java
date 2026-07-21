package cn.czu.claimpaws.reservation.persistence;

import cn.czu.claimpaws.reservation.domain.Reservation;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;

public interface ReservationMapper {
    boolean existsOverlap(
            @Param("resourceId") long resourceId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt
    );

    void insert(Reservation reservation);

    long lastInsertId();

    long count();

    Reservation requireById(@Param("id") long id);
}
