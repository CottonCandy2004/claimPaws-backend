package cn.czu.claimpaws.reservation.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.common.exception.BusinessException;
import cn.czu.claimpaws.common.exception.ErrorCode;
import cn.czu.claimpaws.reservation.application.ReservationService;
import cn.czu.claimpaws.reservation.domain.CreateReservationCommand;
import cn.czu.claimpaws.reservation.domain.Reservation;
import cn.czu.claimpaws.reservation.domain.ReservationStatus;
import cn.czu.claimpaws.reservation.domain.ReservationView;
import cn.czu.claimpaws.reservation.persistence.ReservationMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    public ReservationController(ReservationService reservationService, ReservationMapper reservationMapper) {
        this.reservationService = reservationService;
        this.reservationMapper = reservationMapper;
    }

    @GetMapping
    public ApiResponse<PageResponse<ReservationView>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") long userId,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        int offset = (page - 1) * size;
        List<Reservation> reservations = reservationMapper.findPage(offset, size, userId, status, keyword);
        long total = reservationMapper.countFiltered(userId, status, keyword);
        List<ReservationView> views = reservations.stream().map(r -> ReservationView.from(r, 0)).toList();
        return ApiResponse.success(new PageResponse<>(views, page, size, total), requestId);
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationView> get(
            @PathVariable long id,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return ApiResponse.success(ReservationView.from(reservation, 0), requestId);
    }

    @PostMapping
    public ApiResponse<ReservationView> create(
            @RequestBody CreateRequest body,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") long userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        String key = idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString();
        CreateReservationCommand command = new CreateReservationCommand(
                body.resourceId, Instant.parse(body.startTime), Instant.parse(body.endTime));
        return ApiResponse.success(reservationService.create(userId, key, command), requestId);
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<ReservationView> cancel(
            @PathVariable long id,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        reservationMapper.updateStatus(id, ReservationStatus.CANCELLED.name());
        Reservation updated = reservationMapper.findById(id);
        return ApiResponse.success(ReservationView.from(updated, 0), requestId);
    }

    @PutMapping("/{id}/check-in")
    public ApiResponse<ReservationView> checkIn(
            @PathVariable long id,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        reservationMapper.updateStatus(id, ReservationStatus.CHECKED_IN.name());
        Reservation updated = reservationMapper.findById(id);
        return ApiResponse.success(ReservationView.from(updated, 0), requestId);
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.failure(ex.getErrorCode().getCode(), ex.getMessage(), null);
    }

    record CreateRequest(
            long resourceId,
            @JsonProperty("startTime") String startTime,
            @JsonProperty("endTime") String endTime) {}
}
