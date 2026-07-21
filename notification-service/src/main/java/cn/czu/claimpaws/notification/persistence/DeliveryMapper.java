package cn.czu.claimpaws.notification.persistence;

import cn.czu.claimpaws.notification.domain.NotificationDelivery;
import cn.czu.claimpaws.notification.domain.WebhookDeliveryTask;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DeliveryMapper {
    long countByEventId(@Param("eventId") String eventId);

    void insert(NotificationDelivery delivery);

    List<WebhookDeliveryTask> findDue(@Param("now") LocalDateTime now, @Param("limit") int limit);

    int markProcessing(@Param("id") long id);

    int recoverExpiredClaims(@Param("expiredBefore") LocalDateTime expiredBefore);

    void markSucceeded(@Param("id") long id, @Param("attemptedAt") LocalDateTime attemptedAt,
                       @Param("responseStatus") int responseStatus);

    void markRetry(@Param("id") long id, @Param("retryCount") int retryCount,
                   @Param("attemptedAt") LocalDateTime attemptedAt, @Param("nextAttemptAt") LocalDateTime nextAttemptAt,
                   @Param("responseStatus") Integer responseStatus, @Param("failureReason") String failureReason);

    void markFailed(@Param("id") long id, @Param("retryCount") int retryCount,
                    @Param("attemptedAt") LocalDateTime attemptedAt, @Param("responseStatus") Integer responseStatus,
                    @Param("failureReason") String failureReason);
}
