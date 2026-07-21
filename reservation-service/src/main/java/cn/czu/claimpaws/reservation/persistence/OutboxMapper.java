package cn.czu.claimpaws.reservation.persistence;

import cn.czu.claimpaws.reservation.domain.OutboxMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OutboxMapper {
    void insert(OutboxMessage message);

    long countByType(@Param("eventType") String eventType);

    List<OutboxMessage> findPending();

    void updateStatus(@Param("id") Long id, @Param("status") String status);
}
