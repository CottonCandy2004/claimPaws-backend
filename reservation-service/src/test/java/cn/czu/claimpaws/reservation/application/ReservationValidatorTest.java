package cn.czu.claimpaws.reservation.application;

import cn.czu.claimpaws.common.exception.BusinessException;
import cn.czu.claimpaws.reservation.domain.CreateReservationCommand;
import cn.czu.claimpaws.reservation.infrastructure.ReservationSnapshotDTO;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationValidatorTest {
    private final ReservationValidator validator = new ReservationValidator();
    private final ReservationSnapshotDTO snapshot = new ReservationSnapshotDTO(
            new ReservationSnapshotDTO.ResourceInfo(1L, "会议室A", "MEETING_ROOM", 10, true),
            new ReservationSnapshotDTO.PolicyInfo(30, 30, 30, 480, false, 0), 1L);

    @Test
    void acceptsAdjacentReservationsWhenBothBoundariesAlignToThePolicySlot() {
        Instant start = Instant.parse("2026-07-22T08:00:00Z");

        assertThatCode(() -> validator.validate(new CreateReservationCommand(1L, "", start, start.plusSeconds(1800)), snapshot))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validate(new CreateReservationCommand(1L, "", start.plusSeconds(1800), start.plusSeconds(3600)), snapshot))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsTimesThatDoNotAlignToThePolicySlot() {
        Instant start = Instant.parse("2026-07-22T08:05:00Z");

        assertThatThrownBy(() -> validator.validate(new CreateReservationCommand(1L, "", start, start.plusSeconds(1800)), snapshot))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsNonPositiveSlotsBeforeAnyLockSlotCalculation() {
        ReservationSnapshotDTO invalidPolicy = new ReservationSnapshotDTO(
                snapshot.resource(),
                new ReservationSnapshotDTO.PolicyInfo(0, 30, 30, 480, false, 0), 1L);

        assertThatThrownBy(() -> validator.validate(
                new CreateReservationCommand(1L, "", Instant.parse("2026-07-22T08:00:00Z"),
                        Instant.parse("2026-07-22T08:30:00Z")), invalidPolicy))
                .isInstanceOf(BusinessException.class);
    }
}
