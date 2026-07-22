package cn.czu.claimpaws.reservation.persistence;

import cn.czu.claimpaws.reservation.domain.Reservation;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

public interface ReservationMapper {
    boolean existsOverlap(
            @Param("resourceId") long resourceId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt
    );

    void insert(Reservation reservation);

    void insertOccupiedSlots(@Param("reservationId") long reservationId, @Param("resourceId") long resourceId,
                             @Param("startAt") Instant startAt, @Param("endAt") Instant endAt,
                             @Param("slotMinutes") int slotMinutes);

    long count();

    Reservation requireById(@Param("id") long id);

    Reservation findById(@Param("id") long id);

    List<Reservation> findPage(@Param("offset") int offset, @Param("limit") int limit,
                               @Param("userId") Long userId, @Param("status") String status,
                               @Param("keyword") String keyword);

    long countFiltered(@Param("userId") Long userId, @Param("status") String status,
               @Param("keyword") String keyword);

    List<Reservation> findPendingApprovals(@Param("offset") int offset, @Param("limit") int limit);

    long countPendingApprovals();

    int updateStatus(@Param("id") long id, @Param("status") String status);
}
