package cn.czu.claimpaws.resource.domain;

import java.time.LocalDateTime;

public record ReservationPolicy(
        Long id,
        Long resourceId,
        Integer slotMinutes,
        Integer advanceDays,
        Integer minDurationMinutes,
        Integer maxDurationMinutes,
        Integer cancelDeadlineMinutes,
        Integer checkInWindowMinutes,
        Boolean requiresApproval,
        Integer approvalLevel,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted
) {
}
