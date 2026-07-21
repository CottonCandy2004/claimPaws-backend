package cn.czu.claimpaws.notification.persistence;

import org.apache.ibatis.annotations.Param;

public interface ConsumedEventMapper {
    long countByEventId(@Param("eventId") String eventId);

    void insert(@Param("eventId") String eventId);
}
