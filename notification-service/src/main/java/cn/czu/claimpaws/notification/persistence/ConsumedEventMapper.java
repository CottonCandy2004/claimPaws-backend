package cn.czu.claimpaws.notification.persistence;

import org.apache.ibatis.annotations.Param;

public interface ConsumedEventMapper {
    int insertIfAbsent(@Param("eventId") String eventId);
}
