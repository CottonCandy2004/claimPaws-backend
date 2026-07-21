package cn.czu.claimpaws.reservation.persistence;

import cn.czu.claimpaws.reservation.domain.OutboxMessage;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxMapper {
    void insert(OutboxMessage message);

    long countByType(@Param("eventType") String eventType);

    OutboxMessage findByAggregateId(@Param("aggregateId") long aggregateId);

    List<OutboxMessage> findClaimCandidates();

    int claim(@Param("id") Long id, @Param("owner") String owner, @Param("lockedUntil") LocalDateTime lockedUntil);

    int updateStatus(@Param("id") Long id, @Param("owner") String owner, @Param("status") String status);

    int releaseClaim(@Param("id") Long id, @Param("owner") String owner);
}
