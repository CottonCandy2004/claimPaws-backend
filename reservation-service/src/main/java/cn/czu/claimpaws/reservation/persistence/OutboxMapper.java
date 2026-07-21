package cn.czu.claimpaws.reservation.persistence;

import cn.czu.claimpaws.reservation.domain.OutboxMessage;
import org.apache.ibatis.annotations.Param;

public interface OutboxMapper {
    void insert(OutboxMessage message);

    long countByType(@Param("eventType") String eventType);
}
