package cn.czu.claimpaws.reservation.application;

import cn.czu.claimpaws.common.exception.BusinessException;
import cn.czu.claimpaws.common.exception.ErrorCode;
import cn.czu.claimpaws.reservation.domain.CreateReservationCommand;
import cn.czu.claimpaws.reservation.infrastructure.ReservationSnapshotDTO;
import org.springframework.stereotype.Component;

@Component
public class ReservationValidator {
    public void validate(CreateReservationCommand command, ReservationSnapshotDTO snapshot) {
        if (command.startAt() == null || command.endAt() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        if (!command.startAt().isBefore(command.endAt())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        if (!snapshot.resource().active()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
