package cn.czu.claimpaws.notification.persistence;

import cn.czu.claimpaws.notification.domain.NotificationDelivery;
import org.apache.ibatis.annotations.Param;

public interface DeliveryMapper {
    long countByEventId(@Param("eventId") String eventId);

    void insert(NotificationDelivery delivery);
}
