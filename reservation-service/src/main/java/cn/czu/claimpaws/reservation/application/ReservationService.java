package cn.czu.claimpaws.reservation.application;

import cn.czu.claimpaws.common.exception.BusinessException;
import cn.czu.claimpaws.common.exception.ErrorCode;
import cn.czu.claimpaws.reservation.domain.CreateReservationCommand;
import cn.czu.claimpaws.reservation.domain.OutboxMessage;
import cn.czu.claimpaws.reservation.domain.Reservation;
import cn.czu.claimpaws.reservation.domain.ReservationView;
import cn.czu.claimpaws.reservation.infrastructure.ResourceClient;
import cn.czu.claimpaws.reservation.infrastructure.ReservationSnapshotDTO;
import cn.czu.claimpaws.reservation.persistence.OutboxMapper;
import cn.czu.claimpaws.reservation.persistence.ReservationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ResourceClient resourceClient;
    private final ReservationMapper reservationMapper;
    private final OutboxMapper outboxMapper;
    private final ReservationValidator validator;
    private final IdempotencyService idempotency;
    private final ReservationLock reservationLock;

    public ReservationService(ResourceClient resourceClient, ReservationMapper reservationMapper,
                              OutboxMapper outboxMapper, ReservationValidator validator,
                              IdempotencyService idempotency, ReservationLock reservationLock) {
        this.resourceClient = resourceClient;
        this.reservationMapper = reservationMapper;
        this.outboxMapper = outboxMapper;
        this.validator = validator;
        this.idempotency = idempotency;
        this.reservationLock = reservationLock;
    }

    @Transactional
    public ReservationView create(long userId, String key, CreateReservationCommand command) {
        final ReservationSnapshotDTO snapshot = fetchSnapshot(command.resourceId());
        return idempotency.execute(userId, key, () -> reservationLock.withLock(command, snapshot.policy().slotMinutes(), () -> {
            validator.validate(command, snapshot);
            if (reservationMapper.existsOverlap(command.resourceId(), command.startAt(), command.endAt())) {
                throw new BusinessException(ErrorCode.RESERVATION_TIME_CONFLICT);
            }
            Reservation reservation = Reservation.create(userId, command, snapshot);
            reservationMapper.insert(reservation);
            try {
                reservationMapper.insertOccupiedSlots(reservation.id(), command.resourceId(), command.startAt(), command.endAt(), snapshot.policy().slotMinutes());
            } catch (DuplicateKeyException duplicateKeyException) {
                throw new BusinessException(ErrorCode.RESERVATION_TIME_CONFLICT);
            }
            outboxMapper.insert(OutboxMessage.created(DomainEvents.reservationCreated(reservation)));
            return ReservationView.from(reservation, reservation.id());
        }));
    }

    private ReservationSnapshotDTO fetchSnapshot(long resourceId) {
        try {
            return resourceClient.getSnapshot(resourceId);
        } catch (Exception e) {
            log.warn("Failed to get snapshot for resource {}: {}, using default", resourceId, e.getMessage());
            return defaultSnapshot(resourceId);
        }
    }

    private static ReservationSnapshotDTO defaultSnapshot(long resourceId) {
        return new ReservationSnapshotDTO(
                new ReservationSnapshotDTO.ResourceInfo(resourceId, "default", "ROOM", 0, true),
                new ReservationSnapshotDTO.PolicyInfo(30, 7, 30, 240, false, 0, ""),
                0L);
    }
}
